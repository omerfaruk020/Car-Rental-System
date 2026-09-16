package com.carrental.domain;

public class Admin extends User {
    public Admin(int userId, String email, String password) {
        super(userId, email, password, UserRole.ADMIN);
    }

    @Override
    public String getDisplayInfo() {
        return getEmail() + " (Admin)";
    }
}