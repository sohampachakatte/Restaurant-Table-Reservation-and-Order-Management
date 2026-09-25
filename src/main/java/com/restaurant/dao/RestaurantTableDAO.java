package com.restaurant.dao;

import com.restaurant.db.DatabaseConnection;
import com.restaurant.model.RestaurantTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RestaurantTableDAO {

    private static final Set<String> VALID_STATUSES = Set.of(
            "AVAILABLE",
            "RESERVED",
            "OCCUPIED",
            "UNAVAILABLE"
    );

    /*
     * Customer Dashboard:
     * show only available restaurant tables.
     */
    public List<RestaurantTable> getAvailableTables()
            throws SQLException {

        String sql = """
                SELECT Table_ID, Location, Capacity, Status
                FROM RestaurantTable
                WHERE Status = 'AVAILABLE'
                ORDER BY Capacity, Table_ID
                """;

        return getTableList(sql, null);
    }

    /*
     * Admin Dashboard:
     * show every restaurant table.
     */
    public List<RestaurantTable> getAllTables()
            throws SQLException {

        String sql = """
                SELECT Table_ID, Location, Capacity, Status
                FROM RestaurantTable
                ORDER BY Table_ID
                """;

        return getTableList(sql, null);
    }

    public RestaurantTable getTableById(int tableId)
            throws SQLException {

        String sql = """
                SELECT Table_ID, Location, Capacity, Status
                FROM RestaurantTable
                WHERE Table_ID = ?
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, tableId);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return new RestaurantTable(
                            result.getInt("Table_ID"),
                            result.getString("Location"),
                            result.getInt("Capacity"),
                            result.getString("Status")
                    );
                }
            }
        }

        return null;
    }

    /*
     * Admin adds a new table.
     */
    public int addTable(
            String location,
            int capacity
    ) throws SQLException {

        if (capacity <= 0) {
            throw new SQLException("Table capacity must be greater than zero.");
        }

        String sql = """
                INSERT INTO RestaurantTable
                (Location, Capacity, Status)
                VALUES (?, ?, 'AVAILABLE')
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(
                                sql,
                                Statement.RETURN_GENERATED_KEYS
                        )
        ) {
            statement.setString(1, location);
            statement.setInt(2, capacity);
            statement.executeUpdate();

            try (ResultSet result = statement.getGeneratedKeys()) {
                if (result.next()) {
                    return result.getInt(1);
                }
            }
        }

        throw new SQLException("Could not create restaurant table.");
    }

    /*
     * Admin updates table location and capacity.
     */
    public void updateTable(
            int tableId,
            String location,
            int capacity
    ) throws SQLException {

        if (capacity <= 0) {
            throw new SQLException("Table capacity must be greater than zero.");
        }

        String sql = """
                UPDATE RestaurantTable
                SET Location = ?,
                    Capacity = ?
                WHERE Table_ID = ?
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, location);
            statement.setInt(2, capacity);
            statement.setInt(3, tableId);

            int rows = statement.executeUpdate();

            if (rows == 0) {
                throw new SQLException("Restaurant table was not found.");
            }
        }
    }

    /*
     * Used by Customer, Staff, and Admin workflow.
     */
    public void updateTableStatus(
            int tableId,
            String status
    ) throws SQLException {

        status = status.trim().toUpperCase();

        if (!VALID_STATUSES.contains(status)) {
            throw new SQLException("Invalid table status: " + status);
        }

        String sql = """
                UPDATE RestaurantTable
                SET Status = ?
                WHERE Table_ID = ?
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, status);
            statement.setInt(2, tableId);

            int rows = statement.executeUpdate();

            if (rows == 0) {
                throw new SQLException("Restaurant table was not found.");
            }
        }
    }

    /*
     * Admin can delete a table only if no reservation/order uses it.
     */
    public void deleteTable(int tableId)
            throws SQLException {

        String sql = """
                DELETE FROM RestaurantTable
                WHERE Table_ID = ?
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, tableId);

            int rows = statement.executeUpdate();

            if (rows == 0) {
                throw new SQLException("Restaurant table was not found.");
            }
        }
    }

    private List<RestaurantTable> getTableList(
            String sql,
            Integer unusedParameter
    ) throws SQLException {

        List<RestaurantTable> tables = new ArrayList<>();

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()
        ) {
            while (result.next()) {
                tables.add(new RestaurantTable(
                        result.getInt("Table_ID"),
                        result.getString("Location"),
                        result.getInt("Capacity"),
                        result.getString("Status")
                ));
            }
        }

        return tables;
    }
}