package com.restaurant.model;

public class UserSession {
    private final int userId;
    private final String email;
    private final String role;
    private final Integer customerId;
    private final Integer staffId;

    public UserSession(
            int userId,
            String email,
            String role,
            Integer customerId,
            Integer staffId
    ) {
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.customerId = customerId;
        this.staffId = staffId;
    }

    public int getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public Integer getStaffId() {
        return staffId;
    }
}