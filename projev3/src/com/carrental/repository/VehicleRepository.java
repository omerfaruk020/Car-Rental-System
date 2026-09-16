package com.carrental.repository;

import com.carrental.domain.*;
import com.carrental.util.CsvUtil;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class VehicleRepository {

    private static final String CSV_HEADER = "licensePlate,brand,model,year,segment,dailyPrice,status";
    private final String filePath;
    private final Map<String, Vehicle> vehicles = new LinkedHashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public VehicleRepository(String filePath) {
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
                    System.err.println("⚠ Vehicle CSV line " + lineNo + " has too few columns, skipped.");
                    continue;
                }
                try {
                    String plate     = cols[0].trim();
                    String brand     = cols[1].trim();
                    String model     = cols[2].trim();
                    int year         = Integer.parseInt(cols[3].trim());
                    VehicleSegment seg = VehicleSegment.valueOf(cols[4].trim());
                    double price     = Double.parseDouble(cols[5].trim());
                    VehicleStatus st = VehicleStatus.valueOf(cols[6].trim());
                    vehicles.put(plate, new Vehicle(plate, brand, model, year, seg, price, st));
                } catch (RuntimeException ex) {
                    System.err.println("⚠ Vehicle CSV line " + lineNo + " could not be read, skipped: " + ex.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load vehicle CSV: " + e.getMessage());
        }
    }

    private void save() {
        lock.readLock().lock();
        try {
            List<String> lines = new ArrayList<>();
            for (Vehicle v : vehicles.values()) {
                lines.add(CsvUtil.toLine(
                        v.getLicensePlate(), v.getBrand(), v.getModel(),
                        String.valueOf(v.getYear()), v.getSegment().name(),
                        String.valueOf(v.getDailyPrice()), v.getStatus().name()));
            }
            CsvUtil.writeAll(filePath, CSV_HEADER, lines);
        } catch (IOException e) {
            System.err.println("Failed to save vehicle CSV: " + e.getMessage());
        } finally {
            lock.readLock().unlock();
        }
    }

    public void add(Vehicle vehicle) {
        lock.writeLock().lock();
        try {
            vehicles.put(vehicle.getLicensePlate(), vehicle);
            save();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Optional<Vehicle> findByPlate(String plate) {
        lock.readLock().lock();
        try {
            return Optional.ofNullable(vehicles.get(plate));
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Vehicle> findAll() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(vehicles.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean existsByPlate(String plate) {
        lock.readLock().lock();
        try {
            return vehicles.containsKey(plate);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void update(Vehicle vehicle) {
        lock.writeLock().lock();
        try {
            vehicles.put(vehicle.getLicensePlate(), vehicle);
            save();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean delete(String plate) {
        lock.writeLock().lock();
        try {
            boolean removed = vehicles.remove(plate) != null;
            if (removed) save();
            return removed;
        } finally {
            lock.writeLock().unlock();
        }
    }
}
