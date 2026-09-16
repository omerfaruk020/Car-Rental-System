package com.carrental.util;

/**
 * Mock payment gateway. Always approves the charge; a real implementation would
 * call an external provider and could also fail.
 */
public class PaymentGateway {
    private static PaymentGateway instance;

    private PaymentGateway() {}

    public static synchronized PaymentGateway getInstance() {
        if (instance == null) instance = new PaymentGateway();
        return instance;
    }

    public boolean processPayment(String email, double amount) {
        System.out.printf(">>> PAYMENT GATEWAY: %.2f TL charged to %s. (Approved)%n", amount, email);
        return true;
    }
}
