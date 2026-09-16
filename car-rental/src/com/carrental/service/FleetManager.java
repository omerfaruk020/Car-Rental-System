package com.carrental.service;

import com.carrental.domain.*;
import com.carrental.exception.*;
import com.carrental.repository.VehicleRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FleetManager {

    private static FleetManager instance;
    private final VehicleRepository vehicleRepository;

    private FleetManager(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public static synchronized FleetManager getInstance(VehicleRepository vehicleRepository) {
        if (instance == null) {
            instance = new FleetManager(vehicleRepository);
        } else if (instance.vehicleRepository != vehicleRepository) {
            // Previously this was silently ignored: a second call with a different
            // repository kept handing back the fleet bound to the first one.
            System.err.println("⚠ FleetManager is already initialized; the new VehicleRepository was ignored.");
        }
        return instance;
    }

    public void addVehicle(String plate, String brand, String model, int year,
                           VehicleSegment segment, double dailyPrice) {
        if (vehicleRepository.existsByPlate(plate)) {
            throw new DuplicateLicensePlateException(plate);
        }
        Vehicle v = new Vehicle(plate, brand, model, year, segment, dailyPrice, VehicleStatus.AVAILABLE);
        vehicleRepository.add(v);
        System.out.println("✓ Vehicle added successfully: " + v);
    }

    public void updateVehicle(String plate, String brand, String model, int year,
                              VehicleSegment segment, double dailyPrice) {
        Vehicle v = vehicleRepository.findByPlate(plate)
                .orElseThrow(() -> new RuntimeException("Vehicle not found: " + plate));
        v.setBrand(brand);
        v.setModel(model);
        v.setYear(year);
        v.setSegment(segment);
        v.setDailyPrice(dailyPrice);
        vehicleRepository.update(v);
        System.out.println("✓ Vehicle updated: " + v);
    }

    public void deleteVehicle(String plate) {
        Vehicle v = vehicleRepository.findByPlate(plate)
                .orElseThrow(() -> new RuntimeException("Vehicle not found: " + plate));
        if (v.getStatus() != VehicleStatus.AVAILABLE) {
            throw new RuntimeException("Only vehicles in 'AVAILABLE' status can be deleted.");
        }
        vehicleRepository.delete(plate);
        System.out.println("✓ Vehicle removed from the fleet: " + plate);
    }

    public List<Vehicle> searchAvailable(VehicleSegment segment) {
        return vehicleRepository.findAll().stream()
                .filter(v -> v.getStatus() == VehicleStatus.AVAILABLE)
                .filter(v -> segment == null || v.getSegment() == segment)
                .collect(Collectors.toList());
    }

    public List<Vehicle> findAll() {
        return vehicleRepository.findAll();
    }

    public Optional<Vehicle> findByPlate(String plate) {
        return vehicleRepository.findByPlate(plate);
    }

    public void setStatus(String plate, VehicleStatus status) {
        Vehicle v = vehicleRepository.findByPlate(plate)
                .orElseThrow(() -> new RuntimeException("Vehicle not found: " + plate));
        v.updateStatus(status);
        vehicleRepository.update(v);
    }

    public synchronized void lockVehicle(String plate) {
        Vehicle v = vehicleRepository.findByPlate(plate)
                .orElseThrow(() -> new RuntimeException("Vehicle not found: " + plate));
        if (v.getStatus() != VehicleStatus.AVAILABLE) {
            throw new VehicleUnavailableException(plate);
        }
        v.updateStatus(VehicleStatus.RESERVED);
        vehicleRepository.update(v);
    }

    public void releaseVehicle(String plate) {
        setStatus(plate, VehicleStatus.AVAILABLE);
    }
}
