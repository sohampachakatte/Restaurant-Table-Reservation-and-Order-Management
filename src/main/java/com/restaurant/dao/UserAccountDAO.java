package com.restaurant.dao;

import com.restaurant.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserAccountDAO {

    public String authenticate(String email, String password)
            throws SQLException {

        String sql = """
                SELECT Role
                FROM User_Account
                WHERE Email = ? AND Password_Hash = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, email);
            statement.setString(2, password);

            ResultSet result = statement.executeQuery();

            if (result.next()) {
                return result.getString("Role");
            }
        }

        return null;
    }
}