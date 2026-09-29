package com.restaurant.dao;

import com.restaurant.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StaffOrderDAO {

    public void updateOrderStatus(
            int orderId,
            int staffId,
            String status
    ) throws SQLException {

        String sql = """
                UPDATE Orders
                SET Order_Status = ?, Staff_ID = ?
                WHERE Order_ID = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, status);
            statement.setInt(2, staffId);
            statement.setInt(3, orderId);

            statement.executeUpdate();
        }
    }
}