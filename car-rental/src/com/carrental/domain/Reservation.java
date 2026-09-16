package com.carrental.domain;

import java.time.LocalDate;

public class Reservation {
    private int reservationId;
    private int customerId;
    private String licensePlate;
    private LocalDate startDate;
    private LocalDate endDate;
    private double totalCost;
    private ReservationStatus status;
    private boolean isCorporateInvoice;
    private double penaltyAmount;

    public Reservation(int id, int customerId, String licensePlate, LocalDate startDate, LocalDate endDate,
                       double totalCost, ReservationStatus status, boolean isCorporateInvoice, double penaltyAmount) {
        this.reservationId = id; this.customerId = customerId; this.licensePlate = licensePlate;
        this.startDate = startDate; this.endDate = endDate; this.totalCost = totalCost;
        this.status = status; this.isCorporateInvoice = isCorporateInvoice; this.penaltyAmount = penaltyAmount;
    }

    public int getReservationId() { return reservationId; }
    public int getCustomerId() { return customerId; }
    public String getLicensePlate() { return licensePlate; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public double getTotalCost() { return totalCost; }
    public ReservationStatus getStatus() { return status; }
    public boolean isCorporateInvoice() { return isCorporateInvoice; }
    public double getPenaltyAmount() { return penaltyAmount; }

    public void updateStatus(ReservationStatus status) { this.status = status; }
    public void addPenalty(double penalty) {
        this.penaltyAmount += penalty;
        this.totalCost += penalty;
    }

    @Override
    public String toString() {
        return "Reservation #" + reservationId + " | Vehicle: " + licensePlate + " | Date: " + startDate + " to " + endDate + " | Status: " + status;
    }
}