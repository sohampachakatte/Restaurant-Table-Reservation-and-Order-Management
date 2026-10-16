package com.restaurant.dao;

import com.restaurant.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class RestaurantSettingsDAO {

    private static final String MAIN_IMAGE_KEY = "MAIN_IMAGE_PATH";

    public RestaurantSettingsDAO() {
        try {
            createSettingsTableIfNeeded();
        } catch (SQLException exception) {
            System.out.println("Could not prepare restaurant settings: "
                    + exception.getMessage());
        }
    }

    private void createSettingsTableIfNeeded() throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS Restaurant_Settings (
                    Setting_Key VARCHAR(100) PRIMARY KEY,
                    Setting_Value VARCHAR(500) NULL
                )
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                Statement statement = connection.createStatement()
        ) {
            statement.execute(sql);
        }
    }

    public String getMainImagePath() throws SQLException {
        String sql = """
                SELECT Setting_Value
                FROM Restaurant_Settings
                WHERE Setting_Key = ?
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, MAIN_IMAGE_KEY);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getString("Setting_Value");
                }
            }
        }

        return null;
    }

    public void saveMainImagePath(String imagePath) throws SQLException {
        String sql = """
                INSERT INTO Restaurant_Settings
                (Setting_Key, Setting_Value)
                VALUES (?, ?)
                ON DUPLICATE KEY UPDATE
                    Setting_Value = VALUES(Setting_Value)
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, MAIN_IMAGE_KEY);
            statement.setString(2, imagePath);
            statement.executeUpdate();
        }
    }
}
