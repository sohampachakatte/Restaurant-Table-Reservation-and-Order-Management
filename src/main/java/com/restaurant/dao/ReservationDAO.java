package com.restaurant.dao;

import com.restaurant.db.DatabaseConnection;
import com.restaurant.model.Reservation;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {

    public int createReservation(
            int customerId,
            int tableId,
            LocalDate reservationDate,
            LocalTime reservationTime,
            int guests,
            int durationMinutes,
            String specialRequest
    ) throws SQLException {

        String tableCheckSql = """
                SELECT Capacity, Status
                FROM RestaurantTable
                WHERE Table_ID = ?
                """;

        String reservationSql = """
                INSERT INTO Reservation
                (Reservation_Date, Reservation_Time, Number_Of_Guests,
                 Duration, Customer_ID, Special_Request)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        String reservationTableSql = """
                INSERT INTO Reservation_Table
                (Reservation_ID, Table_ID)
                VALUES (?, ?)
                """;

        String updateTableSql = """
                UPDATE RestaurantTable
                SET Status = 'RESERVED'
                WHERE Table_ID = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                try (PreparedStatement statement =
                             connection.prepareStatement(tableCheckSql)) {

                    statement.setInt(1, tableId);

                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) {
                            throw new SQLException("Restaurant table was not found.");
                        }

                        if (!"AVAILABLE".equals(
                                result.getString("Status")
                        )) {
                            throw new SQLException(
                                    "This table is not available."
                            );
                        }

                        if (guests > result.getInt("Capacity")) {
                            throw new SQLException(
                                    "This table does not have enough seats."
                            );
                        }
                    }
                }

                int reservationId;

                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     reservationSql,
                                     Statement.RETURN_GENERATED_KEYS
                             )) {

                    statement.setDate(1, Date.valueOf(reservationDate));
                    statement.setTime(2, Time.valueOf(reservationTime));
                    statement.setInt(3, guests);
                    statement.setInt(4, durationMinutes);
                    statement.setInt(5, customerId);
                    statement.setString(6, specialRequest);

                    statement.executeUpdate();

                    try (ResultSet result = statement.getGeneratedKeys()) {
                        if (!result.next()) {
                            throw new SQLException(
                                    "Could not create reservation."
                            );
                        }

                        reservationId = result.getInt(1);
                    }
                }

                try (PreparedStatement statement =
                             connection.prepareStatement(reservationTableSql)) {

                    statement.setInt(1, reservationId);
                    statement.setInt(2, tableId);
                    statement.executeUpdate();
                }

                try (PreparedStatement statement =
                             connection.prepareStatement(updateTableSql)) {

                    statement.setInt(1, tableId);
                    statement.executeUpdate();
                }

                connection.commit();
                return reservationId;

            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    public List<Reservation> getReservationsByCustomerId(
            int customerId
    ) throws SQLException {

        String sql = """
                SELECT
                    r.Reservation_ID,
                    r.Reservation_Date,
                    r.Reservation_Time,
                    r.Number_Of_Guests,
                    r.Duration,
                    r.Customer_ID,
                    r.Special_Request,
                    rt.Table_ID,
                    rt.Location,
                    CONCAT(c.First_Name, ' ', c.Last_Name) AS Customer_Name

                FROM Reservation r

                JOIN Customer c
                    ON r.Customer_ID = c.Customer_ID

                JOIN Reservation_Table rtb
                    ON r.Reservation_ID = rtb.Reservation_ID

                JOIN RestaurantTable rt
                    ON rtb.Table_ID = rt.Table_ID

                WHERE r.Customer_ID = ?

                ORDER BY r.Reservation_Date DESC, r.Reservation_Time DESC
                """;

        List<Reservation> reservations = new ArrayList<>();

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, customerId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    reservations.add(new Reservation(
                            result.getInt("Reservation_ID"),
                            result.getDate("Reservation_Date").toLocalDate(),
                            result.getTime("Reservation_Time").toLocalTime(),
                            result.getInt("Number_Of_Guests"),
                            result.getInt("Duration"),
                            result.getInt("Customer_ID"),
                            result.getInt("Table_ID"),
                            result.getString("Location"),
                            result.getString("Customer_Name"),
                            result.getString("Special_Request")
                    ));
                }
            }
        }

        return reservations;
    }

    public List<Reservation> getAllReservations()
            throws SQLException {

        String sql = """
                SELECT
                    r.Reservation_ID,
                    r.Reservation_Date,
                    r.Reservation_Time,
                    r.Number_Of_Guests,
                    r.Duration,
                    r.Customer_ID,
                    r.Special_Request,
                    rt.Table_ID,
                    rt.Location,
                    CONCAT(c.First_Name, ' ', c.Last_Name) AS Customer_Name

                FROM Reservation r

                JOIN Customer c
                    ON r.Customer_ID = c.Customer_ID

                JOIN Reservation_Table rtb
                    ON r.Reservation_ID = rtb.Reservation_ID

                JOIN RestaurantTable rt
                    ON rtb.Table_ID = rt.Table_ID

                ORDER BY r.Reservation_Date DESC, r.Reservation_Time DESC
                """;

        List<Reservation> reservations = new ArrayList<>();

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()
        ) {
            while (result.next()) {
                reservations.add(new Reservation(
                        result.getInt("Reservation_ID"),
                        result.getDate("Reservation_Date").toLocalDate(),
                        result.getTime("Reservation_Time").toLocalTime(),
                        result.getInt("Number_Of_Guests"),
                        result.getInt("Duration"),
                        result.getInt("Customer_ID"),
                        result.getInt("Table_ID"),
                        result.getString("Location"),
                        result.getString("Customer_Name"),
                        result.getString("Special_Request")
                ));
            }
        }

        return reservations;
    }

    public void cancelReservation(int reservationId)
            throws SQLException {

        String tableIdSql = """
                SELECT Table_ID
                FROM Reservation_Table
                WHERE Reservation_ID = ?
                """;

        String deleteLinkSql = """
                DELETE FROM Reservation_Table
                WHERE Reservation_ID = ?
                """;

        String deleteReservationSql = """
                DELETE FROM Reservation
                WHERE Reservation_ID = ?
                """;

        String updateTableSql = """
                UPDATE RestaurantTable
                SET Status = 'AVAILABLE'
                WHERE Table_ID = ?
                  AND Status = 'RESERVED'
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                int tableId;

                try (PreparedStatement statement =
                             connection.prepareStatement(tableIdSql)) {

                    statement.setInt(1, reservationId);

                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) {
                            throw new SQLException("Reservation was not found.");
                        }

                        tableId = result.getInt("Table_ID");
                    }
                }

                try (PreparedStatement statement =
                             connection.prepareStatement(deleteLinkSql)) {

                    statement.setInt(1, reservationId);
                    statement.executeUpdate();
                }

                try (PreparedStatement statement =
                             connection.prepareStatement(deleteReservationSql)) {

                    statement.setInt(1, reservationId);
                    statement.executeUpdate();
                }

                try (PreparedStatement statement =
                             connection.prepareStatement(updateTableSql)) {

                    statement.setInt(1, tableId);
                    statement.executeUpdate();
                }

                connection.commit();

            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }
}