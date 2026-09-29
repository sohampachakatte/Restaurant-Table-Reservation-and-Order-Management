package com.restaurant.dao;

import com.restaurant.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CustomerRegistrationDAO {

    public void registerCustomer(
            String firstName,
            String middleName,
            String lastName,
            String email,
            String password
    ) throws SQLException {

        String customerSql = """
                INSERT INTO customer
                (First_Name, Middle_Name, Last_Name, Email)
                VALUES (?, ?, ?, ?)
                """;

        String accountSql = """
                INSERT INTO User_Account
                (Email, Password_Hash, Role, Customer_ID)
                VALUES (?, ?, 'CUSTOMER', ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                int customerId;

                try (PreparedStatement customerStatement =
                             connection.prepareStatement(
                                     customerSql,
                                     Statement.RETURN_GENERATED_KEYS
                             )) {

                    customerStatement.setString(1, firstName);
                    customerStatement.setString(2,
                            middleName.isEmpty() ? null : middleName);
                    customerStatement.setString(3, lastName);
                    customerStatement.setString(4, email);

                    customerStatement.executeUpdate();

                    ResultSet keys = customerStatement.getGeneratedKeys();

                    if (!keys.next()) {
                        throw new SQLException("Could not create Customer ID.");
                    }

                    customerId = keys.getInt(1);
                }

                try (PreparedStatement accountStatement =
                             connection.prepareStatement(accountSql)) {

                    accountStatement.setString(1, email);
                    accountStatement.setString(2, password);
                    accountStatement.setInt(3, customerId);

                    accountStatement.executeUpdate();
                }

                connection.commit();

            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }
}