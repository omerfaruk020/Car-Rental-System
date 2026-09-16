package com.carrental.service;

import com.carrental.domain.*;
import com.carrental.exception.AuthenticationException;
import com.carrental.repository.UserRepository;
import java.util.Optional;
import java.util.List;

public class UserManager {

    private final UserRepository userRepository;

    public UserManager(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User authenticate(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new AuthenticationException();
        }
        User user = userOpt.get();


        if (!password.equals(user.getPassword())) {
            throw new AuthenticationException();
        }
        return user;
    }

    public Customer registerCustomer(String email, String password, String phone) {
        if (userRepository.existsByEmail(email)) {
            throw new AuthenticationException("This email address is already registered: " + email);
        }
        int id = userRepository.generateNextId();
        Customer customer = new Customer(id, email, password, phone, false, null);
        userRepository.add(customer);
        System.out.println("✓ Registration successful. Welcome, " + email);
        return customer;
    }

    public Customer registerCorporateCustomer(String email, String password, String phone, String taxId) {
        if (userRepository.existsByEmail(email)) {
            throw new AuthenticationException("This email address is already registered: " + email);
        }
        int id = userRepository.generateNextId();
        Customer customer = new Customer(id, email, password, phone, true, taxId);
        userRepository.add(customer);
        System.out.println("✓ Corporate registration successful. Welcome, " + email);
        return customer;
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }
}