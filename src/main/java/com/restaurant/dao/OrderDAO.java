package com.restaurant.dao;

import com.restaurant.db.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OrderDAO {

    private static final Set<String> VALID_STATUSES = Set.of(
            "PENDING",
            "CONFIRMED",
            "PREPARING",
            "READY",
            "SERVED",
            "AWAITING_PAYMENT",
            "COMPLETED",
            "CANCELLED"
    );

    /*
     * Creates an order when customer clicks Place Order.
     *
     * cartItems format:
     * Item_ID, Quantity
     *
     * Example:
     * Map<Integer, Integer> cart = new LinkedHashMap<>();
     * cart.put(1, 2);
     * cart.put(4, 1);
     */
    public int createOrder(
            int customerId,
            Integer tableId,
            Map<Integer, Integer> cartItems
    ) throws SQLException {

        if (cartItems == null || cartItems.isEmpty()) {
            throw new SQLException("Cart is empty.");
        }

        String itemPriceSql = """
                SELECT Price
                FROM Menu_Item
                WHERE Item_ID = ?
                """;

        String createOrderSql = """
                INSERT INTO Orders
                (Order_Date, Order_Status, Total_Amount, Customer_ID, Table_ID)
                VALUES (CURDATE(), 'PENDING', ?, ?, ?)
                """;

        String addOrderItemSql = """
                INSERT INTO Order_Contains
                (Order_ID, Item_ID, Quantity)
                VALUES (?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                BigDecimal totalAmount = BigDecimal.ZERO;

                // Calculate bill from Menu_Item table prices.
                for (Map.Entry<Integer, Integer> entry : cartItems.entrySet()) {
                    int itemId = entry.getKey();
                    int quantity = entry.getValue();

                    if (quantity <= 0) {
                        throw new SQLException("Item quantity must be greater than zero.");
                    }

                    try (PreparedStatement statement =
                                 connection.prepareStatement(itemPriceSql)) {

                        statement.setInt(1, itemId);

                        try (ResultSet result = statement.executeQuery()) {
                            if (!result.next()) {
                                throw new SQLException(
                                        "Menu item was not found. Item ID: " + itemId
                                );
                            }

                            BigDecimal price = result.getBigDecimal("Price");

                            totalAmount = totalAmount.add(
                                    price.multiply(BigDecimal.valueOf(quantity))
                            );
                        }
                    }
                }

                int orderId;

                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     createOrderSql,
                                     Statement.RETURN_GENERATED_KEYS
                             )) {

                    statement.setBigDecimal(1, totalAmount);
                    statement.setInt(2, customerId);

                    if (tableId == null) {
                        statement.setNull(3, Types.INTEGER);
                    } else {
                        statement.setInt(3, tableId);
                    }

                    statement.executeUpdate();

                    try (ResultSet result = statement.getGeneratedKeys()) {
                        if (!result.next()) {
                            throw new SQLException("Could not create order.");
                        }

                        orderId = result.getInt(1);
                    }
                }

                try (PreparedStatement statement =
                             connection.prepareStatement(addOrderItemSql)) {

                    for (Map.Entry<Integer, Integer> entry : cartItems.entrySet()) {
                        statement.setInt(1, orderId);
                        statement.setInt(2, entry.getKey());
                        statement.setInt(3, entry.getValue());
                        statement.addBatch();
                    }

                    statement.executeBatch();
                }

                connection.commit();
                return orderId;

            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    /*
     * Staff can add an extra menu item to an existing order.
     * The total bill is recalculated from Menu_Item prices.
     */
    public void addExtraItem(
            int orderId,
            int itemId,
            int quantity
    ) throws SQLException {

        if (quantity <= 0) {
            throw new SQLException("Quantity must be greater than zero.");
        }

        String itemCheckSql = """
                SELECT Item_ID
                FROM Menu_Item
                WHERE Item_ID = ?
                """;

        String addItemSql = """
                INSERT INTO Order_Contains
                (Order_ID, Item_ID, Quantity)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE Quantity = Quantity + ?
                """;

        String updateTotalSql = """
                UPDATE Orders
                SET Total_Amount = (
                    SELECT COALESCE(SUM(mi.Price * oc.Quantity), 0)
                    FROM Order_Contains oc
                    JOIN Menu_Item mi ON mi.Item_ID = oc.Item_ID
                    WHERE oc.Order_ID = ?
                ),
                Order_Status = 'PENDING'
                WHERE Order_ID = ?
                  AND Order_Status NOT IN ('COMPLETED', 'CANCELLED')
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                try (PreparedStatement statement =
                             connection.prepareStatement(itemCheckSql)) {

                    statement.setInt(1, itemId);

                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) {
                            throw new SQLException("Menu item was not found.");
                        }
                    }
                }

                try (PreparedStatement statement =
                             connection.prepareStatement(addItemSql)) {

                    statement.setInt(1, orderId);
                    statement.setInt(2, itemId);
                    statement.setInt(3, quantity);
                    statement.setInt(4, quantity);

                    statement.executeUpdate();
                }

                try (PreparedStatement statement =
                             connection.prepareStatement(updateTotalSql)) {

                    statement.setInt(1, orderId);
                    statement.setInt(2, orderId);

                    int rows = statement.executeUpdate();

                    if (rows == 0) {
                        throw new SQLException(
                                "Completed or cancelled orders cannot be changed."
                        );
                    }
                }

                connection.commit();

            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    /*
     * Staff updates the kitchen/order progress.
     */
    public void updateOrderStatus(
            int orderId,
            int staffId,
            String status
    ) throws SQLException {

        status = status.trim().toUpperCase();

        if (!VALID_STATUSES.contains(status)) {
            throw new SQLException("Invalid order status: " + status);
        }

        String sql = """
                UPDATE Orders
                SET Order_Status = ?,
                    Staff_ID = ?
                WHERE Order_ID = ?
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, status);
            statement.setInt(2, staffId);
            statement.setInt(3, orderId);

            int rows = statement.executeUpdate();

            if (rows == 0) {
                throw new SQLException("Order was not found.");
            }
        }
    }

    /*
     * Shows all orders to Staff and Admin.
     */
    public List<OrderDetails> getAllOrders() throws SQLException {

        String sql = """
                SELECT
                    o.Order_ID,
                    o.Order_Date,
                    o.Order_Status,
                    o.Total_Amount,
                    o.Customer_ID,
                    o.Staff_ID,
                    o.Table_ID,

                    CONCAT(
                        c.First_Name,
                        ' ',
                        c.Last_Name
                    ) AS Customer_Name,

                    COALESCE(
                        CONCAT(
                            s.First_Name,
                            ' ',
                            s.Last_Name
                        ),
                        'Not Assigned'
                    ) AS Staff_Name,

                    COALESCE(
                        rt.Location,
                        'Takeaway'
                    ) AS Table_Location,

                    GROUP_CONCAT(
                        CONCAT(
                            mi.Item_Name,
                            ' × ',
                            oc.Quantity
                        )
                        ORDER BY mi.Item_Name
                        SEPARATOR ', '
                    ) AS Order_Items

                FROM Orders o

                JOIN Customer c
                    ON o.Customer_ID = c.Customer_ID

                LEFT JOIN Staff s
                    ON o.Staff_ID = s.Staff_ID

                LEFT JOIN RestaurantTable rt
                    ON o.Table_ID = rt.Table_ID

                LEFT JOIN Order_Contains oc
                    ON o.Order_ID = oc.Order_ID

                LEFT JOIN Menu_Item mi
                    ON oc.Item_ID = mi.Item_ID

                GROUP BY
                    o.Order_ID,
                    o.Order_Date,
                    o.Order_Status,
                    o.Total_Amount,
                    o.Customer_ID,
                    o.Staff_ID,
                    o.Table_ID,
                    c.First_Name,
                    c.Last_Name,
                    s.First_Name,
                    s.Last_Name,
                    rt.Location

                ORDER BY o.Order_ID DESC
                """;

        return getOrderList(sql, null);
    }

    /*
     * Shows only one customer's orders in Customer Dashboard.
     */
    public List<OrderDetails> getOrdersByCustomerId(
            int customerId
    ) throws SQLException {

        String sql = """
                SELECT
                    o.Order_ID,
                    o.Order_Date,
                    o.Order_Status,
                    o.Total_Amount,
                    o.Customer_ID,
                    o.Staff_ID,
                    o.Table_ID,

                    CONCAT(
                        c.First_Name,
                        ' ',
                        c.Last_Name
                    ) AS Customer_Name,

                    COALESCE(
                        CONCAT(
                            s.First_Name,
                            ' ',
                            s.Last_Name
                        ),
                        'Not Assigned'
                    ) AS Staff_Name,

                    COALESCE(
                        rt.Location,
                        'Takeaway'
                    ) AS Table_Location,

                    GROUP_CONCAT(
                        CONCAT(
                            mi.Item_Name,
                            ' × ',
                            oc.Quantity
                        )
                        ORDER BY mi.Item_Name
                        SEPARATOR ', '
                    ) AS Order_Items

                FROM Orders o

                JOIN Customer c
                    ON o.Customer_ID = c.Customer_ID

                LEFT JOIN Staff s
                    ON o.Staff_ID = s.Staff_ID

                LEFT JOIN RestaurantTable rt
                    ON o.Table_ID = rt.Table_ID

                LEFT JOIN Order_Contains oc
                    ON o.Order_ID = oc.Order_ID

                LEFT JOIN Menu_Item mi
                    ON oc.Item_ID = mi.Item_ID

                WHERE o.Customer_ID = ?

                GROUP BY
                    o.Order_ID,
                    o.Order_Date,
                    o.Order_Status,
                    o.Total_Amount,
                    o.Customer_ID,
                    o.Staff_ID,
                    o.Table_ID,
                    c.First_Name,
                    c.Last_Name,
                    s.First_Name,
                    s.Last_Name,
                    rt.Location

                ORDER BY o.Order_ID DESC
                """;

        return getOrderList(sql, customerId);
    }

    private List<OrderDetails> getOrderList(
            String sql,
            Integer customerId
    ) throws SQLException {

        List<OrderDetails> orders = new ArrayList<>();

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            if (customerId != null) {
                statement.setInt(1, customerId);
            }

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    orders.add(new OrderDetails(
                            result.getInt("Order_ID"),
                            result.getDate("Order_Date").toLocalDate(),
                            result.getString("Order_Status"),
                            result.getBigDecimal("Total_Amount"),
                            result.getInt("Customer_ID"),
                            result.getObject("Staff_ID", Integer.class),
                            result.getObject("Table_ID", Integer.class),
                            result.getString("Customer_Name"),
                            result.getString("Staff_Name"),
                            result.getString("Table_Location"),
                            result.getString("Order_Items")
                    ));
                }
            }
        }

        return orders;
    }

    /*
     * Staff/Admin can delete an order only after payment is PAID.
     */
    public boolean deleteCompletedOrder(int orderId)
            throws SQLException {

        String paymentCheckSql = """
                SELECT Payment_Status
                FROM Payment
                WHERE Order_ID = ?
                """;

        String deletePaymentSql = """
                DELETE FROM Payment
                WHERE Order_ID = ?
                """;

        String deleteOrderItemsSql = """
                DELETE FROM Order_Contains
                WHERE Order_ID = ?
                """;

        String deleteOrderSql = """
                DELETE FROM Orders
                WHERE Order_ID = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                try (PreparedStatement statement =
                             connection.prepareStatement(paymentCheckSql)) {

                    statement.setInt(1, orderId);

                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()
                                || !"PAID".equals(
                                result.getString("Payment_Status")
                        )) {
                            connection.rollback();
                            return false;
                        }
                    }
                }

                try (PreparedStatement statement =
                             connection.prepareStatement(deletePaymentSql)) {
                    statement.setInt(1, orderId);
                    statement.executeUpdate();
                }

                try (PreparedStatement statement =
                             connection.prepareStatement(deleteOrderItemsSql)) {
                    statement.setInt(1, orderId);
                    statement.executeUpdate();
                }

                try (PreparedStatement statement =
                             connection.prepareStatement(deleteOrderSql)) {
                    statement.setInt(1, orderId);
                    statement.executeUpdate();
                }

                connection.commit();
                return true;

            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    public static class OrderDetails {
        private final int orderId;
        private final LocalDate orderDate;
        private final String status;
        private final BigDecimal totalAmount;
        private final int customerId;
        private final Integer staffId;
        private final Integer tableId;
        private final String customerName;
        private final String staffName;
        private final String tableLocation;
        private final String orderItems;

        public OrderDetails(
                int orderId,
                LocalDate orderDate,
                String status,
                BigDecimal totalAmount,
                int customerId,
                Integer staffId,
                Integer tableId,
                String customerName,
                String staffName,
                String tableLocation,
                String orderItems
        ) {
            this.orderId = orderId;
            this.orderDate = orderDate;
            this.status = status;
            this.totalAmount = totalAmount;
            this.customerId = customerId;
            this.staffId = staffId;
            this.tableId = tableId;
            this.customerName = customerName;
            this.staffName = staffName;
            this.tableLocation = tableLocation;
            this.orderItems = orderItems;
        }

        public int getOrderId() {
            return orderId;
        }

        public LocalDate getOrderDate() {
            return orderDate;
        }

        public String getStatus() {
            return status;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }

        public int getCustomerId() {
            return customerId;
        }

        public Integer getStaffId() {
            return staffId;
        }

        public Integer getTableId() {
            return tableId;
        }

        public String getCustomerName() {
            return customerName;
        }

        public String getStaffName() {
            return staffName;
        }

        public String getTableLocation() {
            return tableLocation;
        }

        public String getOrderItems() {
            return orderItems;
        }
    }
}