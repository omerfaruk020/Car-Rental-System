package com.carrental.main;

import com.carrental.domain.*;
import com.carrental.service.*;
import java.util.Scanner;

public class AdminMenu {
    private Scanner scanner;
    private FleetManager fleetManager;
    private ReservationManager reservationManager;
    private UserManager userManager;

    public AdminMenu(Scanner scanner, FleetManager fleetManager, ReservationManager reservationManager, UserManager userManager) {
        this.scanner = scanner;
        this.fleetManager = fleetManager;
        this.reservationManager = reservationManager;
        this.userManager = userManager;
    }

    public void show() {
        boolean running = true;
        while (running) {
            System.out.println("\n╔══════════════════════════════════════╗");
            System.out.println("║              ADMIN MENU              ║");
            System.out.println("╠══════════════════════════════════════╣");
            System.out.println("║ 1. List All Vehicles                 ║");
            System.out.println("║ 2. Add New Vehicle                   ║");
            System.out.println("║ 3. Update Vehicle Information        ║");
            System.out.println("║ 4. Remove Vehicle from Fleet         ║");
            System.out.println("║ 5. View All Reservations             ║");
            System.out.println("║ 6. Update Vehicle Status             ║");
            System.out.println("║ 7. List All Users                    ║");
            System.out.println("║ 0. Log Out                           ║");
            System.out.println("╚══════════════════════════════════════╝");
            System.out.print("Your Choice: ");

            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> fleetManager.findAll().forEach(System.out::println);
                    case "2" -> addNewVehicle();
                    case "3" -> updateVehicle();
                    case "4" -> deleteVehicle();
                    case "5" -> reservationManager.getAllReservations().forEach(System.out::println);
                    case "6" -> updateVehicleStatus();
                    case "7" -> listAllUsers();
                    case "0" -> running = false;
                    default  -> System.out.println("⚠ Invalid choice!");
                }
            } catch (Exception e) {
                System.out.println("✗ Operation failed: " + e.getMessage());
            }
        }
    }

    private void updateVehicle() {
        System.out.print("License plate of the vehicle to update: ");
        String licensePlate = scanner.nextLine().trim();

        fleetManager.findByPlate(licensePlate).ifPresentOrElse(vehicle -> {
            System.out.println("\nCurrent Vehicle Information: " + vehicle);
            System.out.println("Please enter the new values:");

            System.out.print("New Brand: "); String brand = scanner.nextLine().trim();
            System.out.print("New Model: "); String model = scanner.nextLine().trim();
            System.out.print("New Year: "); int year = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("New Segment (ECONOMY/SEDAN/HATCHBACK/SUV/LUXURY): ");
            VehicleSegment segment = VehicleSegment.valueOf(scanner.nextLine().trim().toUpperCase());
            System.out.print("New Daily Price: "); double dailyPrice = Double.parseDouble(scanner.nextLine().trim());

            // We are performing the main update operation through FleetManager.
            fleetManager.updateVehicle(licensePlate, brand, model, year, segment, dailyPrice);
        }, () -> System.out.println("⚠ Error: No vehicle found with this license plate."));
    }

    private void addNewVehicle() {
        System.out.print("License Plate: "); String licensePlate = scanner.nextLine().trim();
        System.out.print("Brand: "); String brand = scanner.nextLine().trim();
        System.out.print("Model: "); String model = scanner.nextLine().trim();
        System.out.print("Year: "); int year = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Segment (ECONOMY/SEDAN/HATCHBACK/SUV/LUXURY): ");
        VehicleSegment segment = VehicleSegment.valueOf(scanner.nextLine().trim().toUpperCase());
        System.out.print("Daily Price: "); double dailyPrice = Double.parseDouble(scanner.nextLine().trim());
        fleetManager.addVehicle(licensePlate, brand, model, year, segment, dailyPrice);
    }

    private void deleteVehicle() {
        System.out.print("License plate of the vehicle to be deleted: ");
        String licensePlate = scanner.nextLine().trim();
        fleetManager.deleteVehicle(licensePlate);
    }

    private void updateVehicleStatus() {
        System.out.print("License Plate: ");
        String licensePlate = scanner.nextLine().trim();

        Vehicle vehicle = fleetManager.findByPlate(licensePlate)
                .orElseThrow(() -> new RuntimeException("No vehicle found with this license plate: " + licensePlate));

        System.out.println("Current status: " + vehicle.getStatus());
        System.out.println("1. AVAILABLE, 2. MAINTENANCE");
        String statusOption = scanner.nextLine().trim();

        // Previously any input other than "1" silently meant MAINTENANCE.
        if (!statusOption.equals("1") && !statusOption.equals("2")) {
            System.out.println("⚠ Invalid choice. Operation cancelled.");
            return;
        }
        VehicleStatus status = statusOption.equals("1") ? VehicleStatus.AVAILABLE : VehicleStatus.MAINTENANCE;

        if (status == vehicle.getStatus()) {
            System.out.println("ℹ The vehicle is already in " + status + " status.");
            return;
        }

        // A vehicle that is held by an open reservation must not be freed by accident.
        boolean hasOpenReservation = reservationManager.getAllReservations().stream()
                .anyMatch(r -> r.getLicensePlate().equals(licensePlate)
                        && (r.getStatus() == ReservationStatus.CONFIRMED
                         || r.getStatus() == ReservationStatus.ACTIVE));

        if (hasOpenReservation) {
            System.out.println("⚠ WARNING: There is an open (CONFIRMED/ACTIVE) reservation for this vehicle.");
            System.out.print("Do you still want to change the status? (Y/N): ");
            if (!scanner.nextLine().trim().equalsIgnoreCase("Y")) {
                System.out.println("Operation cancelled.");
                return;
            }
        }

        fleetManager.setStatus(licensePlate, status);
        System.out.println("✓ Vehicle status updated: " + licensePlate + " -> " + status);
    }

    private void listAllUsers() {
        System.out.println("\n--- REGISTERED USER LIST ---");
        for (User user : userManager.findAllUsers()) {
            System.out.println(user.getUserId() + " | " + user.getEmail() + " | " + user.getRole());
        }
    }
}