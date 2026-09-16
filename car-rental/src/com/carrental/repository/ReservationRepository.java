package com.carrental.repository;

import com.carrental.domain.*;
import com.carrental.util.CsvUtil;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class ReservationRepository {

    private static final String CSV_HEADER =
            "reservationId,customerId,licensePlate,startDate,endDate,totalCost,status,isCorporateInvoice,penaltyAmount";
    private final String filePath;
    private final Map<Integer, Reservation> reservations = new LinkedHashMap<>();
    private int nextId = 1;

    public ReservationRepository(String filePath) {
        this.filePath = filePath;
        load();
    }

    private void load() {
        try {
            List<String[]> rows = CsvUtil.readAll(filePath);
            int lineNo = 1;
            for (String[] cols : rows) {
                lineNo++;
                if (cols.length < 9) {
                    System.err.println("⚠ Reservation CSV line " + lineNo + " has too few columns, skipped.");
                    continue;
                }
                // A single malformed row used to bring the whole application down,
                // because the exception escaped from the constructor.
                try {
                    int id           = Integer.parseInt(cols[0].trim());
                    int custId       = Integer.parseInt(cols[1].trim());
                    String plate     = cols[2].trim();
                    LocalDate start  = LocalDate.parse(cols[3].trim());
                    LocalDate end    = LocalDate.parse(cols[4].trim());
                    double cost      = Double.parseDouble(cols[5].trim());
                    ReservationStatus st = ReservationStatus.valueOf(cols[6].trim());
                    boolean isCorpInv   = Boolean.parseBoolean(cols[7].trim());
                    double penalty   = Double.parseDouble(cols[8].trim());

                    reservations.put(id, new Reservation(id, custId, plate, start, end, cost, st, isCorpInv, penalty));
                    if (id >= nextId) nextId = id + 1;
                } catch (RuntimeException ex) {
                    System.err.println("⚠ Reservation CSV line " + lineNo + " could not be read, skipped: " + ex.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load reservation CSV: " + e.getMessage());
        }
    }

    public void save() {
        List<String> lines = new ArrayList<>();
        for (Reservation r : reservations.values()) {
            lines.add(CsvUtil.toLine(
                    String.valueOf(r.getReservationId()),
                    String.valueOf(r.getCustomerId()),
                    r.getLicensePlate(),
                    r.getStartDate().toString(),
                    r.getEndDate().toString(),
                    String.valueOf(r.getTotalCost()),
                    r.getStatus().name(),
                    String.valueOf(r.isCorporateInvoice()),
                    String.valueOf(r.getPenaltyAmount())));
        }
        try {
            CsvUtil.writeAll(filePath, CSV_HEADER, lines);
        } catch (IOException e) {
            System.err.println("Failed to save reservation CSV: " + e.getMessage());
        }
    }

    public void add(Reservation reservation) {
        reservations.put(reservation.getReservationId(), reservation);
        save();
    }

    public Optional<Reservation> findById(int id) {
        return Optional.ofNullable(reservations.get(id));
    }

    public List<Reservation> findByCustomerId(int customerId) {
        List<Reservation> result = new ArrayList<>();
        for (Reservation r : reservations.values()) {
            if (r.getCustomerId() == customerId) result.add(r);
        }
        return result;
    }

    public List<Reservation> findAll() {
        return new ArrayList<>(reservations.values());
    }

    public void update(Reservation reservation) {
        reservations.put(reservation.getReservationId(), reservation);
        save();
    }

    public int generateNextId() {
        return nextId++;
    }
}
