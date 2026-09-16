package com.carrental.service;

import com.carrental.domain.*;
import com.carrental.exception.*;
import com.carrental.repository.ReservationRepository;
import com.carrental.util.NotificationService;
import com.carrental.util.PaymentGateway;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

public class ReservationManager {

    private static final double DAMAGE_BASE_FEE        = 500.0;
    private static final double LATE_FEE_MULTIPLIER    = 1.5;   // 150% of the daily price
    private static final double CORPORATE_DISCOUNT     = 0.15;  // 15%

    private final ReservationRepository reservationRepository;
    private final FleetManager fleetManager;
    private final NotificationService notificationService;
    private final PaymentGateway paymentGateway;

    public ReservationManager(ReservationRepository reservationRepository,
                              FleetManager fleetManager) {
        this.reservationRepository = reservationRepository;
        this.fleetManager = fleetManager;
        this.notificationService = NotificationService.getInstance();
        this.paymentGateway = PaymentGateway.getInstance();
    }

    // ------------------------------------------------------------------
    // Make reservation
    // ------------------------------------------------------------------
    public Reservation makeReservation(Customer customer, String licensePlate,
                                       LocalDate startDate, LocalDate endDate,
                                       boolean requestCorporateInvoice) {

        Vehicle vehicle = fleetManager.findByPlate(licensePlate)
                .orElseThrow(() -> new RuntimeException("Vehicle not found: " + licensePlate));

        // --- Date validation ---
        if (startDate == null || endDate == null) {
            throw new RuntimeException("The start and end dates are required.");
        }
        if (startDate.isBefore(LocalDate.now())) {
            throw new RuntimeException("The start date cannot be in the past: " + startDate);
        }
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        if (days <= 0) {
            throw new RuntimeException("Invalid date range: the end date must be after the start date.");
        }

        // --- Availability check BEFORE any money is touched ---
        if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
            throw new VehicleUnavailableException(licensePlate);
        }

        // --- Cost calculation ---
        double totalCost = calculateDynamicRentalCost(days, vehicle.getDailyPrice());

        if (customer.isCorporate()) {
            double corporateDiscount = totalCost * CORPORATE_DISCOUNT;
            totalCost -= corporateDiscount;
            System.out.printf(">>> Corporate Customer Advantage: %.2f TL tax discount applied.%n", corporateDiscount);
        }

        System.out.printf("%nVehicle: %s | %d days | Daily: %.2f TL | Total: %.2f TL%n",
                licensePlate, days, vehicle.getDailyPrice(), totalCost);

        boolean corpInvoice = requestCorporateInvoice && customer.isCorporate();
        if (requestCorporateInvoice && !customer.isCorporate()) {
            System.out.println("⚠ A corporate invoice can only be issued to a corporate customer. An individual invoice will be issued.");
        }

        // --- 1) Hold the vehicle FIRST, then charge. ---
        // If the vehicle cannot be held we must never have taken the money.
        fleetManager.lockVehicle(licensePlate);

        Reservation reservation;
        try {
            boolean paid = paymentGateway.processPayment(customer.getEmail(), totalCost);
            if (!paid) {
                throw new RuntimeException("Payment failed. Reservation could not be created.");
            }

            int id = reservationRepository.generateNextId();
            reservation = new Reservation(id, customer.getUserId(), licensePlate,
                    startDate, endDate, totalCost, ReservationStatus.CONFIRMED, corpInvoice, 0.0);
            reservationRepository.add(reservation);
        } catch (RuntimeException e) {
            // Roll the hold back so the vehicle does not stay stuck on RESERVED.
            fleetManager.releaseVehicle(licensePlate);
            throw e;
        }

        notificationService.sendReservationConfirmation(
                customer.getEmail(), reservation.getReservationId(), licensePlate);

        return reservation;
    }

    // ------------------------------------------------------------------
    // Cancel reservation
    // ------------------------------------------------------------------
    public void cancelReservation(Customer customer, int reservationId) {
        Reservation r = findAndValidateOwnership(customer, reservationId);

        if (r.getStatus() != ReservationStatus.CONFIRMED && r.getStatus() != ReservationStatus.PENDING) {
            throw new RuntimeException("At this stage, the reservation cannot be cancelled. Status: " + r.getStatus());
        }

        r.updateStatus(ReservationStatus.CANCELLED);
        reservationRepository.update(r);

        // Release ONLY the hold that belongs to this reservation.
        // If the vehicle has meanwhile been sent to MAINTENANCE, do not make it available again.
        fleetManager.findByPlate(r.getLicensePlate()).ifPresent(v -> {
            if (v.getStatus() == VehicleStatus.RESERVED) {
                fleetManager.releaseVehicle(r.getLicensePlate());
            } else {
                System.out.println("ℹ The vehicle is in " + v.getStatus()
                        + " status, so it was not set back to AVAILABLE.");
            }
        });

        notificationService.sendCancellationConfirmation(customer.getEmail(), reservationId);
        System.out.println("✓ The reservation was successfully cancelled.");
    }

    // ------------------------------------------------------------------
    // Pick up vehicle
    // ------------------------------------------------------------------
    public void pickUpVehicle(Customer customer, int reservationId) {
        Reservation r = findAndValidateOwnership(customer, reservationId);

        if (r.getStatus() != ReservationStatus.CONFIRMED) {
            throw new RuntimeException("Only a CONFIRMED reservation can be picked up. Status: " + r.getStatus());
        }
        if (LocalDate.now().isBefore(r.getStartDate())) {
            throw new RuntimeException("The vehicle cannot be picked up before the start date: " + r.getStartDate());
        }

        r.updateStatus(ReservationStatus.ACTIVE);
        reservationRepository.update(r);
        fleetManager.setStatus(r.getLicensePlate(), VehicleStatus.IN_USE);
        System.out.println("✓ Vehicle delivered. Reservation moved to ACTIVE status: #" + reservationId);
    }

    // ------------------------------------------------------------------
    // Return vehicle
    // ------------------------------------------------------------------
    public void returnVehicle(Customer customer, int reservationId,
                              LocalDate actualReturnDate, boolean isDamaged) {

        Reservation r = findAndValidateOwnership(customer, reservationId);

        Vehicle vehicle = fleetManager.findByPlate(r.getLicensePlate())
                .orElseThrow(() -> new RuntimeException("Vehicle not found: " + r.getLicensePlate()));

        // Only a vehicle that was actually picked up can be returned.
        if (r.getStatus() != ReservationStatus.ACTIVE) {
            throw new RuntimeException("Only a picked-up (ACTIVE) reservation can be returned. Status: "
                    + r.getStatus() + ". If it has not been picked up yet, use 'Cancel Reservation'.");
        }

        double penalty = 0.0;

        // --- Late return ---
        if (actualReturnDate.isAfter(r.getEndDate())) {
            long lateDays = ChronoUnit.DAYS.between(r.getEndDate(), actualReturnDate);
            double lateFee = calculateLateFee(lateDays, vehicle.getDailyPrice());
            penalty += lateFee;
            System.out.printf("⚠ Delay: %d days | Daily penalty (%.0f%% of the daily price): %.2f TL | Total delay penalty: %.2f TL%n",
                    lateDays,
                    LATE_FEE_MULTIPLIER * 100,
                    vehicle.getDailyPrice() * LATE_FEE_MULTIPLIER,
                    lateFee);
        }

        // --- Damage ---
        if (isDamaged) {
            penalty += DAMAGE_BASE_FEE;
            System.out.printf("⚠ Damage detected. Damage fee: %.2f TL%n", DAMAGE_BASE_FEE);
        }

        // The penalty is actually collected, not only recorded.
        if (penalty > 0) {
            boolean paid = paymentGateway.processPayment(customer.getEmail(), penalty);
            if (!paid) {
                throw new RuntimeException("The penalty could not be collected, so the return was not completed.");
            }
            r.addPenalty(penalty);
        }

        r.updateStatus(ReservationStatus.COMPLETED);
        reservationRepository.update(r);

        VehicleStatus newVehicleStatus = isDamaged ? VehicleStatus.MAINTENANCE : VehicleStatus.AVAILABLE;
        fleetManager.setStatus(r.getLicensePlate(), newVehicleStatus);

        notificationService.sendReturnConfirmation(customer.getEmail(), reservationId, r.getTotalCost());
    }

    // ------------------------------------------------------------------
    // Queries
    // ------------------------------------------------------------------
    public List<Reservation> getCustomerReservations(Customer customer) {
        return reservationRepository.findByCustomerId(customer.getUserId());
    }

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    public Optional<Reservation> findById(int id) {
        return reservationRepository.findById(id);
    }

    // ------------------------------------------------------------------
    // Calculations
    // ------------------------------------------------------------------
    private double calculateDynamicRentalCost(long days, double dailyPrice) {
        // Discount: 10% for 7+ days, 15% for 14+ days
        double total = days * dailyPrice;
        if (days >= 14) total *= 0.85;
        else if (days >= 7) total *= 0.90;
        return total;
    }

    private double calculateLateFee(long lateDays, double dailyPrice) {
        return lateDays * dailyPrice * LATE_FEE_MULTIPLIER;
    }

    private Reservation findAndValidateOwnership(Customer customer, int reservationId) {
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));
        if (r.getCustomerId() != customer.getUserId()) {
            throw new RuntimeException("This reservation does not belong to you.");
        }
        return r;
    }

    // ------------------------------------------------------------------
    // Invoice
    // ------------------------------------------------------------------
    public void printInvoice(int reservationId) {
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));

        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║                 INVOICE                  ║");
        System.out.println("╠══════════════════════════════════════════╣");
        System.out.printf("║ Reservation No : #%-22d ║%n", r.getReservationId());
        System.out.printf("║ Vehicle        : %-23s ║%n", r.getLicensePlate());
        System.out.printf("║ Start          : %-23s ║%n", r.getStartDate());
        System.out.printf("║ End            : %-23s ║%n", r.getEndDate());
        System.out.printf("║ Invoice Type   : %-23s ║%n", r.isCorporateInvoice() ? "CORPORATE" : "INDIVIDUAL");
        System.out.printf("║ Rent Cost      : %20.2f TL ║%n", r.getTotalCost() - r.getPenaltyAmount());
        if (r.getPenaltyAmount() > 0) {
            System.out.printf("║ Fine/Damage    : %20.2f TL ║%n", r.getPenaltyAmount());
        }
        System.out.printf("║ TOTAL          : %20.2f TL ║%n", r.getTotalCost());
        System.out.printf("║ Status         : %-23s ║%n", r.getStatus());
        System.out.println("╚══════════════════════════════════════════╝");
    }
}
