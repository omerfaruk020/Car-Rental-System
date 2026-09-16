package com.carrental.domain;

public abstract class User {
    private int userId;
    private String email;
    private String password;
    private UserRole role;

    public User(int userId, String email, String password, UserRole role) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public int getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public UserRole getRole() { return role; }

    public abstract String getDisplayInfo();
}