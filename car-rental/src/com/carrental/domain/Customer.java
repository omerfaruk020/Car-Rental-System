package com.carrental.domain;

public class Customer extends User {
    private String phoneNumber;
    private boolean isCorporate;
    private String taxId;

    public Customer(int userId, String email, String password, String phoneNumber, boolean isCorporate, String taxId) {
        super(userId, email, password, UserRole.CUSTOMER);
        this.phoneNumber = phoneNumber;
        this.isCorporate = isCorporate;
        this.taxId = taxId;
    }

    public String getPhoneNumber() { return phoneNumber; }
    public boolean isCorporate() { return isCorporate; }
    public String getTaxId() { return taxId; }

    @Override
    public String getDisplayInfo() {
        return getEmail() + (isCorporate ? " (Corporate Customer)" : " (Customer)");
    }
}