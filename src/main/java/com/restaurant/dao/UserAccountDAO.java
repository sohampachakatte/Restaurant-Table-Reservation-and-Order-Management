package com.restaurant.dao;

import com.restaurant.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserAccountDAO {

    // Returns CUSTOMER, STAFF, ADMIN, or null when login details are incorrect.
    public String authenticate(String email, String password) throws SQLException {

        String sql = """
                SELECT Role
                FROM User_Account
                WHERE Email = ? AND Password_Hash = ?
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, email);
            statement.setString(2, password);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getString("Role");
                }
            }
        }

        return null;
    }

    // Used after a customer logs in.
    public int getCustomerIdByEmail(String email) throws SQLException {

        String sql = """
                SELECT Customer_ID
                FROM User_Account
                WHERE Email = ? AND Role = 'CUSTOMER'
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, email);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    int customerId = result.getInt("Customer_ID");
                    return result.wasNull() ? -1 : customerId;
                }
            }
        }

        return -1;
    }

    // Used after a staff member logs in.
    public int getStaffIdByEmail(String email) throws SQLException {

        String sql = """
                SELECT Staff_ID
                FROM User_Account
                WHERE Email = ? AND Role = 'STAFF'
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, email);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    int staffId = result.getInt("Staff_ID");
                    return result.wasNull() ? -1 : staffId;
                }
            }
        }

        return -1;
    }

    // Admin may also be linked to a Staff record.
    public int getAdminStaffIdByEmail(String email) throws SQLException {

        String sql = """
                SELECT Staff_ID
                FROM User_Account
                WHERE Email = ? AND Role = 'ADMIN'
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, email);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    int staffId = result.getInt("Staff_ID");
                    return result.wasNull() ? -1 : staffId;
                }
            }
        }

        return -1;
    }
}