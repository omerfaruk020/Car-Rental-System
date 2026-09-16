package com.carrental.exception;

public class VehicleUnavailableException extends RuntimeException {
    public VehicleUnavailableException(String plate) { super("The vehicle is currently unavailable: " + plate); }
}