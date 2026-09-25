package com.restaurant.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Reservation {

    private final int reservationId;
    private final LocalDate reservationDate;
    private final LocalTime reservationTime;
    private final int numberOfGuests;
    private final int duration;
    private final int customerId;
    private final int tableId;
    private final String tableLocation;
    private final String customerName;
    private final String specialRequest;

    public Reservation(
            int reservationId,
            LocalDate reservationDate,
            LocalTime reservationTime,
            int numberOfGuests,
            int duration,
            int customerId,
            int tableId,
            String tableLocation,
            String customerName,
            String specialRequest
    ) {
        this.reservationId = reservationId;
        this.reservationDate = reservationDate;
        this.reservationTime = reservationTime;
        this.numberOfGuests = numberOfGuests;
        this.duration = duration;
        this.customerId = customerId;
        this.tableId = tableId;
        this.tableLocation = tableLocation;
        this.customerName = customerName;
        this.specialRequest = specialRequest;
    }

    public int getReservationId() {
        return reservationId;
    }

    public LocalDate getReservationDate() {
        return reservationDate;
    }

    public LocalTime getReservationTime() {
        return reservationTime;
    }

    public int getNumberOfGuests() {
        return numberOfGuests;
    }

    public int getDuration() {
        return duration;
    }

    public int getCustomerId() {
        return customerId;
    }

    public int getTableId() {
        return tableId;
    }

    public String getTableLocation() {
        return tableLocation;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getSpecialRequest() {
        return specialRequest;
    }
}