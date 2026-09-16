package com.carrental.repository;

import com.carrental.domain.*;
import com.carrental.util.CsvUtil;

import java.io.IOException;
import java.util.*;

public class UserRepository {

    private static final String CSV_HEADER = "userId,email,password,role,phoneNumber,isCorporate,taxId";
    private final String filePath;
    private final Map<Integer, User> users = new LinkedHashMap<>();
    private int nextId = 1;

    public UserRepository(String filePath) {
        this.filePath = filePath;
        load();
    }

    private void load() {
        try {
            List<String[]> rows = CsvUtil.readAll(filePath);
            int lineNo = 1;
            for (String[] cols : rows) {
                lineNo++;
                if (cols.length < 7) {
                    System.err.println("⚠ User CSV line " + lineNo + " has too few columns, skipped.");
                    continue;
                }
                try {
                    int id          = Integer.parseInt(cols[0].trim());
                    String email    = cols[1].trim();
                    String password = cols[2].trim();
                    UserRole role   = UserRole.valueOf(cols[3].trim());
                    String phone    = cols[4].trim();
                    boolean isCorp  = Boolean.parseBoolean(cols[5].trim());
                    String taxId    = cols[6].trim();
                    // An empty cell used to become "", while registration stores null.
                    if (taxId.isEmpty()) taxId = null;
                    if (phone.isEmpty()) phone = null;

                    User user;
                    if (role == UserRole.ADMIN) {
                        user = new Admin(id, email, password);
                    } else {
                        user = new Customer(id, email, password, phone, isCorp, taxId);
                    }
                    users.put(id, user);
                    if (id >= nextId) nextId = id + 1;
                } catch (RuntimeException ex) {
                    System.err.println("⚠ User CSV line " + lineNo + " could not be read, skipped: " + ex.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load user CSV: " + e.getMessage());
        }
    }

    private void save() {
        List<String> lines = new ArrayList<>();
        for (User u : users.values()) {
            String phone = "", taxId = "";
            boolean isCorp = false;

            if (u instanceof Customer c) {
                phone = c.getPhoneNumber() != null ? c.getPhoneNumber() : "";
                isCorp = c.isCorporate();
                taxId = c.getTaxId() != null ? c.getTaxId() : "";
            }

            lines.add(CsvUtil.toLine(
                    String.valueOf(u.getUserId()),
                    u.getEmail(),
                    u.getPassword(),
                    u.getRole().name(),
                    phone,
                    String.valueOf(isCorp),
                    taxId));
        }
        try {
            CsvUtil.writeAll(filePath, CSV_HEADER, lines);
        } catch (IOException e) {
            System.err.println("Failed to save user CSV: " + e.getMessage());
        }
    }

    public void add(User user) {
        users.put(user.getUserId(), user);
        save();
    }

    public Optional<User> findByEmail(String email) {
        return users.values().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    public Optional<User> findById(int id) {
        return Optional.ofNullable(users.get(id));
    }

    public boolean existsByEmail(String email) {
        return users.values().stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(email));
    }

    public int generateNextId() {
        return nextId++;
    }
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }
}