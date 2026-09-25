package com.restaurant.dao;

import com.restaurant.db.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StaffDAO {

    // 1. Get all staff members with active order count
    public List<Object[]> getAllStaffWithOrderCounts() {
        List<Object[]> staffList = new ArrayList<>();
        String sql = "SELECT s.Staff_ID, CONCAT(s.First_Name, ' ', s.Last_Name) AS StaffName, s.Role, " +
                "COUNT(CASE WHEN o.Order_Status != 'COMPLETED' THEN 1 END) AS ActiveOrders " +
                "FROM Staff s " +
                "LEFT JOIN Orders o ON s.Staff_ID = o.Staff_ID " +
                "GROUP BY s.Staff_ID, s.First_Name, s.Last_Name, s.Role " +
                "ORDER BY s.Staff_ID";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                staffList.add(new Object[]{
                        rs.getInt("Staff_ID"),
                        rs.getString("StaffName"),
                        rs.getString("Role"),
                        rs.getInt("ActiveOrders")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return staffList;
    }

    // 2. Get orders assigned to a specific staff member (or all active orders if staffId == -1)
    public List<Object[]> getOrdersByStaff(int staffId) {
        List<Object[]> orders = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT o.Order_ID, CONCAT(c.First_Name, ' ', c.Last_Name) AS CustomerName, " +
                        "CONCAT(s.First_Name, ' ', s.Last_Name) AS StaffName, o.Order_Status, o.Total_Amount, " +
                        "(o.Order_Status = 'COMPLETED') AS IsCompleted " +
                        "FROM Orders o " +
                        "JOIN Customer c ON o.Customer_ID = c.Customer_ID " +
                        "LEFT JOIN Staff s ON o.Staff_ID = s.Staff_ID "
        );

        if (staffId != -1) {
            sql.append("WHERE o.Staff_ID = ? ");
        }
        sql.append("ORDER BY o.Order_ID DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            if (staffId != -1) {
                stmt.setInt(1, staffId);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                orders.add(new Object[]{
                        rs.getInt("Order_ID"),
                        rs.getString("CustomerName"),
                        rs.getString("StaffName") != null ? rs.getString("StaffName") : "Unassigned",
                        rs.getString("Order_Status"),
                        rs.getBigDecimal("Total_Amount"),
                        rs.getBoolean("IsCompleted") // Boolean maps to JTable Checkbox
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    // 3. Complete order and optionally create payment record
    public boolean markOrderCompleted(int orderId) {
        String sql = "UPDATE Orders SET Order_Status = 'COMPLETED' WHERE Order_ID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 4. Assign an order to a staff member
    public boolean assignOrderToStaff(int orderId, int staffId) {
        String sql = "UPDATE Orders SET Staff_ID = ? WHERE Order_ID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, staffId);
            stmt.setInt(2, orderId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
