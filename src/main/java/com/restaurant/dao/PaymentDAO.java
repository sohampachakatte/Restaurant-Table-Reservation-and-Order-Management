package com.restaurant.dao;

import com.restaurant.db.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PaymentDAO {

    /*
     * Create or retry a pending payment.
     * The order is NOT completed yet.
     */
    public BigDecimal createPendingPayment(
            int orderId,
            String paymentMethod
    ) throws SQLException {

        String orderSql = """
                SELECT Total_Amount, Order_Status
                FROM Orders
                WHERE Order_ID = ?
                """;

        String paymentCheckSql = """
                SELECT Payment_Status
                FROM Payment
                WHERE Order_ID = ?
                """;

        String insertPaymentSql = """
                INSERT INTO Payment
                (Amount, Payment_Method, Payment_Date, Order_ID,
                 Payment_Status, Transaction_Reference)
                VALUES (?, ?, CURDATE(), ?, 'PENDING', NULL)
                """;

        String updatePendingPaymentSql = """
                UPDATE Payment
                SET Amount = ?,
                    Payment_Method = ?,
                    Payment_Date = CURDATE(),
                    Payment_Status = 'PENDING',
                    Transaction_Reference = NULL
                WHERE Order_ID = ?
                """;

        String updateOrderSql = """
                UPDATE Orders
                SET Order_Status = 'AWAITING_PAYMENT'
                WHERE Order_ID = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                BigDecimal amount;

                try (PreparedStatement statement =
                             connection.prepareStatement(orderSql)) {

                    statement.setInt(1, orderId);

                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) {
                            throw new SQLException("Order was not found.");
                        }

                        amount = result.getBigDecimal("Total_Amount");
                    }
                }

                boolean paymentExists = false;
                String currentPaymentStatus = null;

                try (PreparedStatement statement =
                             connection.prepareStatement(paymentCheckSql)) {

                    statement.setInt(1, orderId);

                    try (ResultSet result = statement.executeQuery()) {
                        if (result.next()) {
                            paymentExists = true;
                            currentPaymentStatus =
                                    result.getString("Payment_Status");
                        }
                    }
                }

                if ("PAID".equals(currentPaymentStatus)) {
                    throw new SQLException("This order is already paid.");
                }

                if (paymentExists) {
                    try (PreparedStatement statement =
                                 connection.prepareStatement(
                                         updatePendingPaymentSql
                                 )) {

                        statement.setBigDecimal(1, amount);
                        statement.setString(2, paymentMethod);
                        statement.setInt(3, orderId);
                        statement.executeUpdate();
                    }

                } else {
                    try (PreparedStatement statement =
                                 connection.prepareStatement(
                                         insertPaymentSql
                                 )) {

                        statement.setBigDecimal(1, amount);
                        statement.setString(2, paymentMethod);
                        statement.setInt(3, orderId);
                        statement.executeUpdate();
                    }
                }

                try (PreparedStatement statement =
                             connection.prepareStatement(updateOrderSql)) {

                    statement.setInt(1, orderId);
                    statement.executeUpdate();
                }

                connection.commit();
                return amount;

            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    /*
     * Call this only after the payment gateway confirms success.
     * It marks the payment PAID and the order COMPLETED.
     */
    public void markPaymentAsPaid(
            int orderId,
            String transactionReference
    ) throws SQLException {

        String updatePaymentSql = """
                UPDATE Payment
                SET Payment_Status = 'PAID',
                    Transaction_Reference = ?
                WHERE Order_ID = ?
                  AND Payment_Status = 'PENDING'
                """;

        String updateOrderSql = """
                UPDATE Orders
                SET Order_Status = 'COMPLETED'
                WHERE Order_ID = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try (
                    PreparedStatement paymentStatement =
                            connection.prepareStatement(updatePaymentSql);

                    PreparedStatement orderStatement =
                            connection.prepareStatement(updateOrderSql)
            ) {
                paymentStatement.setString(1, transactionReference);
                paymentStatement.setInt(2, orderId);

                int updatedRows = paymentStatement.executeUpdate();

                if (updatedRows == 0) {
                    throw new SQLException(
                            "Payment is not pending or order was not found."
                    );
                }

                orderStatement.setInt(1, orderId);
                orderStatement.executeUpdate();

                connection.commit();

            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    public String getPaymentStatus(int orderId) throws SQLException {

        String sql = """
                SELECT Payment_Status
                FROM Payment
                WHERE Order_ID = ?
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, orderId);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getString("Payment_Status");
                }
            }
        }

        return "NOT_CREATED";
    }

    public BigDecimal getOrderAmount(int orderId) throws SQLException {

        String sql = """
                SELECT Total_Amount
                FROM Orders
                WHERE Order_ID = ?
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, orderId);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getBigDecimal("Total_Amount");
                }
            }
        }

        return BigDecimal.ZERO;
    }
}