package com.restaurant.dao;

import com.restaurant.db.DatabaseConnection;
import com.restaurant.model.MenuItem;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MenuDAO {

    public List<MenuItem> getAllMenuItems() throws SQLException {

        String sql = """
                SELECT Item_ID, Item_Name, Price, Category
                FROM Menu_Item
                ORDER BY Category, Item_Name
                """;

        List<MenuItem> menuItems = new ArrayList<>();

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()
        ) {
            while (result.next()) {
                menuItems.add(new MenuItem(
                        result.getInt("Item_ID"),
                        result.getString("Item_Name"),
                        result.getBigDecimal("Price"),
                        result.getString("Category")
                ));
            }
        }

        return menuItems;
    }

    public MenuItem getMenuItemById(int itemId) throws SQLException {

        String sql = """
                SELECT Item_ID, Item_Name, Price, Category
                FROM Menu_Item
                WHERE Item_ID = ?
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, itemId);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return new MenuItem(
                            result.getInt("Item_ID"),
                            result.getString("Item_Name"),
                            result.getBigDecimal("Price"),
                            result.getString("Category")
                    );
                }
            }
        }

        return null;
    }

    public List<MenuItem> searchMenuItems(String searchText)
            throws SQLException {

        String sql = """
                SELECT Item_ID, Item_Name, Price, Category
                FROM Menu_Item
                WHERE Item_Name LIKE ?
                   OR Category LIKE ?
                ORDER BY Category, Item_Name
                """;

        List<MenuItem> menuItems = new ArrayList<>();
        String keyword = "%" + searchText.trim() + "%";

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, keyword);
            statement.setString(2, keyword);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    menuItems.add(new MenuItem(
                            result.getInt("Item_ID"),
                            result.getString("Item_Name"),
                            result.getBigDecimal("Price"),
                            result.getString("Category")
                    ));
                }
            }
        }
        return menuItems;
    }

    public List<MenuItem> getMenuItemsByCategory(String category)
            throws SQLException {

        String sql = """
                SELECT Item_ID, Item_Name, Price, Category
                FROM Menu_Item
                WHERE Category = ?
                ORDER BY Item_Name
                """;

        List<MenuItem> menuItems = new ArrayList<>();

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, category);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    menuItems.add(new MenuItem(
                            result.getInt("Item_ID"),
                            result.getString("Item_Name"),
                            result.getBigDecimal("Price"),
                            result.getString("Category")
                    ));
                }
            }
        }

        return menuItems;
    }

    /*
     * Admin uses this to add a menu item.
     */
    public int addMenuItem(
            String itemName,
            BigDecimal price,
            String category
    ) throws SQLException {

        String sql = """
                INSERT INTO Menu_Item
                (Item_Name, Price, Category)
                VALUES (?, ?, ?)
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(
                                sql,
                                Statement.RETURN_GENERATED_KEYS
                        )
        ) {
            statement.setString(1, itemName);
            statement.setBigDecimal(2, price);
            statement.setString(3, category);

            statement.executeUpdate();

            try (ResultSet result = statement.getGeneratedKeys()) {
                if (result.next()) {
                    return result.getInt(1);
                }
            }
        }

        throw new SQLException("Could not add menu item.");
    }

    /*
     * Admin uses this to edit a menu item.
     */
    public void updateMenuItem(
            int itemId,
            String itemName,
            BigDecimal price,
            String category
    ) throws SQLException {

        String sql = """
                UPDATE Menu_Item
                SET Item_Name = ?,
                    Price = ?,
                    Category = ?
                WHERE Item_ID = ?
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, itemName);
            statement.setBigDecimal(2, price);
            statement.setString(3, category);
            statement.setInt(4, itemId);

            int rows = statement.executeUpdate();

            if (rows == 0) {
                throw new SQLException("Menu item was not found.");
            }
        }
    }

    /*
     * Admin can delete only menu items that are not used by an order.
     */
    public void deleteMenuItem(int itemId) throws SQLException {

        String sql = """
                DELETE FROM Menu_Item
                WHERE Item_ID = ?
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, itemId);

            int rows = statement.executeUpdate();

            if (rows == 0) {
                throw new SQLException("Menu item was not found.");
            }
        }
    }
}