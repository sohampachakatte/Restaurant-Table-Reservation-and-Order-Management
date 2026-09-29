package com.restaurant.model;

public class RestaurantTable {

    private int tableId;
    private String location;
    private int capacity;
    private String status;

    public RestaurantTable(
            int tableId,
            String location,
            int capacity,
            String status
    ) {
        this.tableId = tableId;
        this.location = location;
        this.capacity = capacity;
        this.status = status;
    }

    public int getTableId() {
        return tableId;
    }

    public String getLocation() {
        return location;
    }

    public int getCapacity() {
        return capacity;
    }

    public String getStatus() {
        return status;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Table " + tableId
                + " · " + capacity + " Guests"
                + " · " + location;
    }
}