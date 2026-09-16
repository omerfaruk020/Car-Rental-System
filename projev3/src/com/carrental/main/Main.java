package com.carrental.main;

import com.carrental.repository.*;
import com.carrental.service.*;
import com.carrental.domain.*;

import java.nio.file.*;
import java.util.Scanner;

public class Main {

    private static final String DATA_DIR        = "src/";
    private static final String VEHICLES_CSV    = DATA_DIR + "vehicles.csv";
    private static final String USERS_CSV       = DATA_DIR + "users.csv";
    private static final String RESERVATIONS_CSV= DATA_DIR + "reservations.csv";

    public static void main(String[] args) {
        ensureDataDirectory();
        printBanner();

        //Infrastructure Layer
        VehicleRepository     vehicleRepo     = new VehicleRepository(VEHICLES_CSV);
        UserRepository        userRepo        = new UserRepository(USERS_CSV);
        ReservationRepository reservationRepo = new ReservationRepository(RESERVATIONS_CSV);

        //Service Layer
        FleetManager       fleetManager       = FleetManager.getInstance(vehicleRepo);
        UserManager        userManager        = new UserManager(userRepo);
        ReservationManager reservationManager = new ReservationManager(reservationRepo, fleetManager);

        //Presentation Layer
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n╔══════════════════════════════════════╗");
            System.out.println("║          CAR RENTAL SYSTEM           ║");
            System.out.println("╠══════════════════════════════════════╣");
            System.out.println("║  1. Log In                           ║");
            System.out.println("║  2. Register (Individual)            ║");
            System.out.println("║  3. Register (Corporate)             ║");
            System.out.println("║  0. Exit Application                 ║");
            System.out.println("╚══════════════════════════════════════╝");
            System.out.print("Your Choice: ");

            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> login(scanner, userManager, fleetManager, reservationManager);
                    case "2" -> registerIndividual(scanner, userManager);
                    case "3" -> registerCorporate(scanner, userManager);
                    case "0" -> {
                        running = false;
                        System.out.println("\nClosing application. Have a good day!");
                    }
                    default  -> System.out.println("⚠ Invalid choice.");
                }
            } catch (Exception e) {
                System.out.println("✗ Error: " + e.getMessage());
            }
        }

        scanner.close();
    }

    private static void login(Scanner scanner, UserManager userManager,
                              FleetManager fleetManager,
                              ReservationManager reservationManager) {
        System.out.print("Email    : ");
        String email = scanner.nextLine().trim();
        System.out.print("Password : ");
        String password = scanner.nextLine().trim();

        User user = userManager.authenticate(email, password);
        System.out.println("\n✓ Welcome, " + user.getDisplayInfo());

        if (user instanceof Admin admin) {
            new AdminMenu(scanner, fleetManager, reservationManager, userManager).show();
        } else if (user instanceof Customer customer) {
            new CustomerMenu(scanner, fleetManager, reservationManager, customer).show();
        }
    }

    private static void registerIndividual(Scanner scanner, UserManager userManager) {
        System.out.print("Email    : "); String email = scanner.nextLine().trim();
        System.out.print("Password : "); String pass  = scanner.nextLine().trim();
        System.out.print("Phone    : "); String phone = scanner.nextLine().trim();
        userManager.registerCustomer(email, pass, phone);
    }

    private static void registerCorporate(Scanner scanner, UserManager userManager) {
        System.out.print("Email    : "); String email = scanner.nextLine().trim();
        System.out.print("Password : "); String pass  = scanner.nextLine().trim();
        System.out.print("Phone    : "); String phone = scanner.nextLine().trim();
        System.out.print("Tax ID   : "); String taxId = scanner.nextLine().trim();
        userManager.registerCorporateCustomer(email, pass, phone, taxId);
    }

    private static void ensureDataDirectory() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
        } catch (Exception e) {
            System.err.println("Failed to create the data directory: " + e.getMessage());
        }
    }

    private static void printBanner() {
        System.out.println();
        System.out.println("  ╔══════════════════════════════════════════════╗");
        System.out.println("  ║              CAR RENTAL SYSTEM               ║");
        System.out.println("  ╚══════════════════════════════════════════════╝");
        System.out.println();
    }
}