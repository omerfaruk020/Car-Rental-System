package com.carrental.main;

import com.carrental.domain.Customer;
import com.carrental.service.*;
import java.time.LocalDate;
import java.util.Scanner;

public class CustomerMenu {
    private Scanner scanner;
    private FleetManager fleetManager;
    private ReservationManager reservationManager;
    private Customer customer;

    public CustomerMenu(Scanner scanner, FleetManager fleetManager, ReservationManager reservationManager, Customer customer) {
        this.scanner = scanner;
        this.fleetManager = fleetManager;
        this.reservationManager = reservationManager;
        this.customer = customer;
    }

    public void show() {
        boolean running = true;
        while (running) {
            System.out.println("\n╔══════════════════════════════════════╗");
            System.out.println("║            CUSTOMER MENU             ║");
            System.out.println("╠══════════════════════════════════════╣");
            System.out.println("║ 1. List Available Vehicles           ║");
            System.out.println("║ 2. Rent a Vehicle (Make Reservation) ║");
            System.out.println("║ 3. View My Reservations              ║");
            System.out.println("║ 4. Cancel Reservation                ║");
            System.out.println("║ 5. Pick-Up Vehicle                   ║");
            System.out.println("║ 6. Return Vehicle                    ║");
            System.out.println("║ 0. Log Out                           ║");
            System.out.println("╚══════════════════════════════════════╝");
            System.out.print("Your Choice: ");

            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> fleetManager.searchAvailable(null).forEach(v ->
                            System.out.println(v + " - Daily Price: " + v.getDailyPrice() + " TL"));
                    case "2" -> makeReservation();
                    case "3" -> reservationManager.getCustomerReservations(customer).forEach(System.out::println);
                    case "4" -> cancelReservation();
                    case "5" -> pickUpVehicle();
                    case "6" -> returnVehicle();
                    case "0" -> running = false;
                    default  -> System.out.println("⚠ Invalid choice!");
                }
            } catch (Exception e) {
                System.out.println("✗ Error: " + e.getMessage());
            }
        }
    }

    private void cancelReservation() {
        System.out.print("Reservation ID to cancel: ");
        int resId = readInt();
        if (resId <= 0) { System.out.println("⚠ Invalid ID format."); return; }
        reservationManager.cancelReservation(customer, resId);
    }

    private void makeReservation() {
        System.out.print("License plate of the vehicle you want to rent: ");
        String licensePlate = scanner.nextLine().trim();

        System.out.print("How many days will you rent it for?: ");
        int days = readInt();
        if (days <= 0) {
            System.out.println("⚠ The number of days must be at least 1.");
            return;
        }

        System.out.print("Do you want a corporate invoice? (Y/N): ");
        boolean isCorporate = scanner.nextLine().trim().equalsIgnoreCase("Y");

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(days);

        reservationManager.makeReservation(customer, licensePlate, startDate, endDate, isCorporate);
    }

    private void pickUpVehicle() {
        System.out.print("Reservation ID to activate (pick-up): ");
        int resId = readInt();
        if (resId <= 0) { System.out.println("⚠ Invalid ID format."); return; }
        reservationManager.pickUpVehicle(customer, resId);
    }

    private void returnVehicle() {
        System.out.print("Reservation ID to return: ");
        int resId = readInt();
        if (resId <= 0) { System.out.println("⚠ Invalid ID format."); return; }

        System.out.print("Is there any new damage to the vehicle? (Y/N): ");
        boolean isDamaged = scanner.nextLine().trim().equalsIgnoreCase("Y");

        reservationManager.returnVehicle(customer, resId, LocalDate.now(), isDamaged);
        reservationManager.printInvoice(resId);
    }

    /** Returns -1 instead of throwing when the input is not a number. */
    private int readInt() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}