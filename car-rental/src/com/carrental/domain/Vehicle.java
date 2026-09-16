package com.carrental.domain;

public class Vehicle {
    private String licensePlate;
    private String brand;
    private String model;
    private int year;
    private VehicleSegment segment;
    private double dailyPrice;
    private VehicleStatus status;

    public Vehicle(String licensePlate, String brand, String model, int year, VehicleSegment segment, double dailyPrice, VehicleStatus status) {
        this.licensePlate = licensePlate; this.brand = brand; this.model = model;
        this.year = year; this.segment = segment; this.dailyPrice = dailyPrice; this.status = status;
    }

    public String getLicensePlate() { return licensePlate; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public VehicleSegment getSegment() { return segment; }
    public void setSegment(VehicleSegment segment) { this.segment = segment; }
    public double getDailyPrice() { return dailyPrice; }
    public void setDailyPrice(double dailyPrice) { this.dailyPrice = dailyPrice; }
    public VehicleStatus getStatus() { return status; }

    public void updateStatus(VehicleStatus status) { this.status = status; }

    @Override
    public String toString() {
        return brand + " " + model + " (" + year + ") - License Plate: " + licensePlate + " [" + segment + "] - Status: " + status;
    }
}