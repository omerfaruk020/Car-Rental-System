package com.carrental.exception;

public class ReservationNotFoundException extends RuntimeException {
    public ReservationNotFoundException(int id) { super("Reservation not found: #" + id); }
}