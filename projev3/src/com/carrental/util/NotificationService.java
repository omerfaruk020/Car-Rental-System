package com.carrental.util;

/**
 * Mock notification service. In a real system this would send an actual e-mail;
 * here it only prints to the console.
 */
public class NotificationService {
    private static NotificationService instance;

    private NotificationService() {}

    public static synchronized NotificationService getInstance() {
        if (instance == null) instance = new NotificationService();
        return instance;
    }

    public void sendReservationConfirmation(String email, int reservationId, String licensePlate) {
        System.out.println(">>> EMAIL to " + email + ": Reservation #" + reservationId
                + " for vehicle " + licensePlate + " has been confirmed.");
    }

    public void sendCancellationConfirmation(String email, int reservationId) {
        System.out.println(">>> EMAIL to " + email + ": Reservation #" + reservationId
                + " has been cancelled.");
    }

    public void sendReturnConfirmation(String email, int reservationId, double totalCost) {
        System.out.printf(">>> EMAIL to %s: Return completed for reservation #%d. Final amount: %.2f TL%n",
                email, reservationId, totalCost);
    }
}
