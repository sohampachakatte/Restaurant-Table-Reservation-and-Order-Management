package com.restaurant.ui;

import com.restaurant.dao.MenuDAO;
import com.restaurant.dao.OrderDAO;
import com.restaurant.dao.PaymentDAO;
import com.restaurant.dao.ReservationDAO;
import com.restaurant.dao.RestaurantTableDAO;
import com.restaurant.dao.UserAccountDAO;
import com.restaurant.model.MenuItem;
import com.restaurant.model.Reservation;
import com.restaurant.model.RestaurantTable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class staffDashboard extends JFrame {

    private static final Color BG = new Color(248, 246, 242);
    private static final Color CARD = Color.WHITE;
    private static final Color DARK = new Color(45, 32, 24);
    private static final Color RED = new Color(180, 20, 55);
    private static final Color GOLD = new Color(202, 145, 45);
    private static final Color GREEN = new Color(36, 140, 83);
    private static final Color BLUE = new Color(62, 103, 173);
    private static final Color BORDER = new Color(225, 220, 214);
    private static final Color TEXT_GRAY = new Color(110, 105, 100);

    private final String staffEmail;
    private int staffId = -1;

    private final UserAccountDAO userAccountDAO = new UserAccountDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final MenuDAO menuDAO = new MenuDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final RestaurantTableDAO tableDAO = new RestaurantTableDAO();

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    private final JPanel dashboardPanel = new JPanel();
    private final JPanel ordersListPanel = new JPanel();
    private final JPanel reservationsListPanel = new JPanel();
    private final JPanel tablesGridPanel = new JPanel();
    private final JPanel paymentsListPanel = new JPanel();

    public staffDashboard(String staffEmail) {
        this.staffEmail = staffEmail;

        setTitle("Restaurant Management System - Staff Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        loadStaffId();

        setLayout(new BorderLayout());
        add(createHeader(), BorderLayout.NORTH);

        contentPanel.setBackground(BG);
        contentPanel.add(createDashboardPage(), "DASHBOARD");
        contentPanel.add(createOrdersPage(), "ORDERS");
        contentPanel.add(createReservationsPage(), "RESERVATIONS");
        contentPanel.add(createTablesPage(), "TABLES");
        contentPanel.add(createPaymentsPage(), "PAYMENTS");

        add(contentPanel, BorderLayout.CENTER);

        refreshDashboard();
        refreshOrdersList();
        refreshReservationsList();
        refreshTablesGrid();
        refreshPaymentsList();

        cardLayout.show(contentPanel, "DASHBOARD");
    }

    private void loadStaffId() {
        try {
            staffId = userAccountDAO.getStaffIdByEmail(staffEmail);

        } catch (SQLException exception) {
            showMessage(
                    "Database Error",
                    "Could not load Staff ID.\n"
                            + exception.getMessage(),
                    true
            );
        }
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(DARK);
        header.setBorder(new EmptyBorder(12, 35, 12, 35));

        JLabel brand = new JLabel("VELORA  |  Staff Portal");
        brand.setFont(new Font("Serif", Font.BOLD, 22));
        brand.setForeground(Color.WHITE);

        JPanel navigation = new JPanel(new FlowLayout(
                FlowLayout.CENTER,
                10,
                0
        ));

        navigation.setOpaque(false);

        navigation.add(createNavButton("Dashboard", "DASHBOARD"));
        navigation.add(createNavButton("Orders", "ORDERS"));
        navigation.add(createNavButton("Reservations", "RESERVATIONS"));
        navigation.add(createNavButton("Tables", "TABLES"));
        navigation.add(createNavButton("Payments", "PAYMENTS"));

        JPanel rightPanel = new JPanel(new FlowLayout(
                FlowLayout.RIGHT,
                12,
                0
        ));

        rightPanel.setOpaque(false);

        JLabel staffLabel = new JLabel("Staff: " + staffEmail);
        staffLabel.setForeground(Color.WHITE);
        staffLabel.setFont(new Font("Arial", Font.PLAIN, 13));

        JButton logoutButton = new RoundedButton(
                "Logout",
                RED,
                Color.WHITE
        );

        logoutButton.setPreferredSize(new Dimension(95, 34));

        logoutButton.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        rightPanel.add(staffLabel);
        rightPanel.add(logoutButton);

        header.add(brand, BorderLayout.WEST);
        header.add(navigation, BorderLayout.CENTER);
        header.add(rightPanel, BorderLayout.EAST);

        return header;
    }

    private JButton createNavButton(String text, String pageName) {
        JButton button = new JButton(text);

        button.setFont(new Font("Arial", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(DARK);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addActionListener(e -> {
            refreshPage(pageName);
            cardLayout.show(contentPanel, pageName);
        });

        return button;
    }

    private void refreshPage(String pageName) {
        switch (pageName) {
            case "DASHBOARD" -> refreshDashboard();
            case "ORDERS" -> refreshOrdersList();
            case "RESERVATIONS" -> refreshReservationsList();
            case "TABLES" -> refreshTablesGrid();
            case "PAYMENTS" -> refreshPaymentsList();
        }
    }

    private JComponent createDashboardPage() {
        dashboardPanel.setBackground(BG);
        dashboardPanel.setLayout(new BoxLayout(
                dashboardPanel,
                BoxLayout.Y_AXIS
        ));

        dashboardPanel.setBorder(new EmptyBorder(32, 55, 40, 55));

        return wrapInScrollPane(dashboardPanel);
    }

    private void refreshDashboard() {
        dashboardPanel.removeAll();

        dashboardPanel.add(createTitle(
                "Staff Dashboard",
                "Manage restaurant orders, reservations, tables, and bills."
        ));

        try {
            List<OrderDAO.OrderDetails> orders = orderDAO.getAllOrders();
            List<Reservation> reservations = reservationDAO.getAllReservations();

            int pending = countOrders(orders, "PENDING");
            int preparing = countOrders(orders, "PREPARING");
            int ready = countOrders(orders, "READY");

            JPanel statsPanel = new JPanel(new GridLayout(1, 4, 18, 18));
            statsPanel.setOpaque(false);
            statsPanel.setMaximumSize(
                    new Dimension(Integer.MAX_VALUE, 130)
            );

            statsPanel.add(createStatCard(
                    "Pending Orders",
                    String.valueOf(pending),
                    RED
            ));

            statsPanel.add(createStatCard(
                    "Preparing",
                    String.valueOf(preparing),
                    GOLD
            ));

            statsPanel.add(createStatCard(
                    "Ready to Serve",
                    String.valueOf(ready),
                    GREEN
            ));

            statsPanel.add(createStatCard(
                    "Reservations",
                    String.valueOf(reservations.size()),
                    BLUE
            ));

            dashboardPanel.add(Box.createVerticalStrut(20));
            dashboardPanel.add(statsPanel);
            dashboardPanel.add(Box.createVerticalStrut(30));

            JLabel section = new JLabel("Recent Orders");
            section.setFont(new Font("Serif", Font.BOLD, 25));
            section.setForeground(DARK);
            section.setAlignmentX(Component.LEFT_ALIGNMENT);

            dashboardPanel.add(section);
            dashboardPanel.add(Box.createVerticalStrut(15));

            if (orders.isEmpty()) {
                dashboardPanel.add(
                        new JLabel("No customer orders are available.")
                );
            }

            for (int i = 0; i < Math.min(3, orders.size()); i++) {
                dashboardPanel.add(createOrderCard(orders.get(i)));
                dashboardPanel.add(Box.createVerticalStrut(12));
            }

        } catch (SQLException exception) {
            dashboardPanel.add(new JLabel(
                    "Could not load dashboard: "
                            + exception.getMessage()
            ));
        }

        dashboardPanel.revalidate();
        dashboardPanel.repaint();
    }

    private int countOrders(
            List<OrderDAO.OrderDetails> orders,
            String status
    ) {
        int count = 0;

        for (OrderDAO.OrderDetails order : orders) {
            if (status.equals(order.getStatus())) {
                count++;
            }
        }

        return count;
    }

    private JPanel createStatCard(String title, String value, Color accent) {
        JPanel card = new RoundedPanel(20, CARD);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(18, 20, 18, 20));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(TEXT_GRAY);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setForeground(accent);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 34));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.SOUTH);

        return card;
    }

    private JComponent createOrdersPage() {
        JPanel page = createPagePanel();

        page.add(createTitle(
                "Order Management",
                "Accept orders, add extra items, and update food status."
        ));

        ordersListPanel.setLayout(new BoxLayout(
                ordersListPanel,
                BoxLayout.Y_AXIS
        ));

        ordersListPanel.setBackground(BG);
        ordersListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        page.add(Box.createVerticalStrut(20));
        page.add(ordersListPanel);

        return wrapInScrollPane(page);
    }

    private void refreshOrdersList() {
        ordersListPanel.removeAll();

        try {
            List<OrderDAO.OrderDetails> orders = orderDAO.getAllOrders();

            if (orders.isEmpty()) {
                ordersListPanel.add(
                        new JLabel("No customer orders are available.")
                );
            }

            for (OrderDAO.OrderDetails order : orders) {
                ordersListPanel.add(createOrderCard(order));
                ordersListPanel.add(Box.createVerticalStrut(14));
            }

        } catch (SQLException exception) {
            ordersListPanel.add(new JLabel(
                    "Could not load orders: "
                            + exception.getMessage()
            ));
        }

        ordersListPanel.revalidate();
        ordersListPanel.repaint();
    }

    private JPanel createOrderCard(OrderDAO.OrderDetails order) {
        JPanel card = new RoundedPanel(20, CARD);
        card.setLayout(new BorderLayout(15, 15));
        card.setBorder(new EmptyBorder(18, 20, 18, 20));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 210));

        JPanel leftPanel = new JPanel();
        leftPanel.setOpaque(false);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        String tableText = order.getTableId() == null
                ? "Takeaway"
                : "Table " + order.getTableId()
                + " · " + order.getTableLocation();

        JLabel orderTitle = new JLabel(
                "Order #" + order.getOrderId()
                        + "   •   " + tableText
        );

        orderTitle.setForeground(DARK);
        orderTitle.setFont(new Font("Arial", Font.BOLD, 18));
        orderTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel customer = new JLabel(
                "Customer: " + order.getCustomerName()
        );

        customer.setForeground(TEXT_GRAY);
        customer.setFont(new Font("Arial", Font.PLAIN, 14));
        customer.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel items = new JLabel(
                "<html>Items: "
                        + (order.getOrderItems() == null
                        ? "No items"
                        : order.getOrderItems())
                        + "</html>"
        );

        items.setForeground(DARK);
        items.setFont(new Font("Arial", Font.PLAIN, 14));
        items.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel total = new JLabel(
                "Total: ₹"
                        + order.getTotalAmount()
                        .stripTrailingZeros()
                        .toPlainString()
        );

        total.setForeground(RED);
        total.setFont(new Font("Arial", Font.BOLD, 15));
        total.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel assignedStaff = new JLabel(
                "Assigned staff: " + order.getStaffName()
        );

        assignedStaff.setForeground(TEXT_GRAY);
        assignedStaff.setFont(new Font("Arial", Font.PLAIN, 12));
        assignedStaff.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftPanel.add(orderTitle);
        leftPanel.add(Box.createVerticalStrut(7));
        leftPanel.add(customer);
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(items);
        leftPanel.add(Box.createVerticalStrut(8));
        leftPanel.add(total);
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(assignedStaff);

        JPanel rightPanel = new JPanel();
        rightPanel.setOpaque(false);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        JLabel statusLabel = new JLabel(order.getStatus());
        statusLabel.setFont(new Font("Arial", Font.BOLD, 13));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(getStatusColor(order.getStatus()));
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setBorder(new EmptyBorder(7, 13, 7, 13));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        rightPanel.add(statusLabel);
        rightPanel.add(Box.createVerticalStrut(10));

        if (!"COMPLETED".equals(order.getStatus())
                && !"CANCELLED".equals(order.getStatus())) {

            RoundedButton addExtraButton = new RoundedButton(
                    "+ Extra Item",
                    GOLD,
                    Color.WHITE
            );

            addExtraButton.setMaximumSize(new Dimension(145, 34));
            addExtraButton.setAlignmentX(Component.CENTER_ALIGNMENT);

            addExtraButton.addActionListener(
                    e -> showAddExtraItemDialog(order)
            );

            rightPanel.add(addExtraButton);
            rightPanel.add(Box.createVerticalStrut(8));
        }

        String nextStatus = getNextStatus(order.getStatus());

        if (nextStatus != null) {
            RoundedButton updateButton = new RoundedButton(
                    getButtonText(nextStatus),
                    GREEN,
                    Color.WHITE
            );

            updateButton.setMaximumSize(new Dimension(145, 36));
            updateButton.setAlignmentX(Component.CENTER_ALIGNMENT);

            updateButton.addActionListener(
                    e -> updateOrderStatus(order, nextStatus)
            );

            rightPanel.add(updateButton);
        }

        try {
            String paymentStatus =
                    paymentDAO.getPaymentStatus(order.getOrderId());

            if ("PAID".equals(paymentStatus)) {
                RoundedButton deleteButton = new RoundedButton(
                        "Delete Order",
                        RED,
                        Color.WHITE
                );

                deleteButton.setMaximumSize(new Dimension(145, 34));
                deleteButton.setAlignmentX(Component.CENTER_ALIGNMENT);

                deleteButton.addActionListener(
                        e -> deleteCompletedOrder(order.getOrderId())
                );

                rightPanel.add(Box.createVerticalStrut(8));
                rightPanel.add(deleteButton);
            }

        } catch (SQLException ignored) {
            // The next refresh will show payment information.
        }

        card.add(leftPanel, BorderLayout.CENTER);
        card.add(rightPanel, BorderLayout.EAST);

        return card;
    }

    private void updateOrderStatus(
            OrderDAO.OrderDetails order,
            String nextStatus
    ) {
        if (staffId == -1) {
            showMessage(
                    "Staff Account Error",
                    "Staff ID was not found for this account.",
                    true
            );
            return;
        }

        try {
            orderDAO.updateOrderStatus(
                    order.getOrderId(),
                    staffId,
                    nextStatus
            );

            showToast(
                    "Order #" + order.getOrderId()
                            + " is now " + nextStatus
            );

            refreshOrdersList();
            refreshDashboard();

        } catch (SQLException exception) {
            showMessage(
                    "Order Error",
                    "Could not update order status.\n"
                            + exception.getMessage(),
                    true
            );
        }
    }

    private void showAddExtraItemDialog(
            OrderDAO.OrderDetails order
    ) {
        try {
            List<MenuItem> menuItems = menuDAO.getAllMenuItems();

            if (menuItems.isEmpty()) {
                showMessage(
                        "Menu Empty",
                        "Admin has not added menu items yet.",
                        true
                );
                return;
            }

            JComboBox<MenuItem> itemBox =
                    new JComboBox<>(
                            menuItems.toArray(new MenuItem[0])
                    );

            JSpinner quantitySpinner = new JSpinner(
                    new SpinnerNumberModel(1, 1, 20, 1)
            );

            Object[] form = {
                    "Menu item:", itemBox,
                    "Quantity:", quantitySpinner
            };

            int result = JOptionPane.showConfirmDialog(
                    this,
                    form,
                    "Add Extra Item to Order #"
                            + order.getOrderId(),
                    JOptionPane.OK_CANCEL_OPTION
            );

            if (result != JOptionPane.OK_OPTION) {
                return;
            }

            MenuItem selectedItem =
                    (MenuItem) itemBox.getSelectedItem();

            int quantity = (Integer) quantitySpinner.getValue();

            if (selectedItem == null) {
                return;
            }

            orderDAO.addExtraItem(
                    order.getOrderId(),
                    selectedItem.getItemId(),
                    quantity
            );

            showToast(
                    selectedItem.getItemName()
                            + " added to Order #"
                            + order.getOrderId()
            );

            refreshOrdersList();
            refreshDashboard();

        } catch (SQLException exception) {
            showMessage(
                    "Extra Item Error",
                    "Could not add menu item.\n"
                            + exception.getMessage(),
                    true
            );
        }
    }

    private void deleteCompletedOrder(int orderId) {
        int answer = JOptionPane.showConfirmDialog(
                this,
                "Delete completed Order #" + orderId + "?",
                "Delete Order",
                JOptionPane.YES_NO_OPTION
        );

        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            boolean deleted =
                    orderDAO.deleteCompletedOrder(orderId);

            if (deleted) {
                showToast("Completed order deleted.");
                refreshOrdersList();
                refreshDashboard();
                refreshPaymentsList();

            } else {
                showMessage(
                        "Cannot Delete",
                        "Only PAID orders can be deleted.",
                        true
                );
            }

        } catch (SQLException exception) {
            showMessage(
                    "Delete Error",
                    "Could not delete order.\n"
                            + exception.getMessage(),
                    true
            );
        }
    }

    private JComponent createReservationsPage() {
        JPanel page = createPagePanel();

        page.add(createTitle(
                "Reservations",
                "View all customer table reservations."
        ));

        reservationsListPanel.setLayout(new BoxLayout(
                reservationsListPanel,
                BoxLayout.Y_AXIS
        ));

        reservationsListPanel.setBackground(BG);

        page.add(Box.createVerticalStrut(20));
        page.add(reservationsListPanel);

        return wrapInScrollPane(page);
    }

    private void refreshReservationsList() {
        reservationsListPanel.removeAll();

        try {
            List<Reservation> reservations =
                    reservationDAO.getAllReservations();

            if (reservations.isEmpty()) {
                reservationsListPanel.add(
                        new JLabel("No reservations are available.")
                );
            }

            for (Reservation reservation : reservations) {
                reservationsListPanel.add(
                        createReservationCard(reservation)
                );

                reservationsListPanel.add(Box.createVerticalStrut(12));
            }

        } catch (SQLException exception) {
            reservationsListPanel.add(new JLabel(
                    "Could not load reservations: "
                            + exception.getMessage()
            ));
        }

        reservationsListPanel.revalidate();
        reservationsListPanel.repaint();
    }

    private JPanel createReservationCard(Reservation reservation) {
        JPanel card = new RoundedPanel(18, CARD);
        card.setLayout(new BorderLayout(16, 0));
        card.setBorder(new EmptyBorder(18, 20, 18, 20));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));

        JPanel detailsPanel = new JPanel(new GridLayout(1, 5, 15, 10));
        detailsPanel.setOpaque(false);

        detailsPanel.add(createInfoLabel(
                "Reservation",
                "#" + reservation.getReservationId()
        ));

        detailsPanel.add(createInfoLabel(
                "Customer",
                reservation.getCustomerName()
        ));

        detailsPanel.add(createInfoLabel(
                "Time",
                reservation.getReservationDate()
                        + " " + reservation.getReservationTime()
        ));

        detailsPanel.add(createInfoLabel(
                "Table",
                "Table " + reservation.getTableId()
                        + " · " + reservation.getTableLocation()
        ));

        detailsPanel.add(createInfoLabel(
                "Guests",
                reservation.getNumberOfGuests() + " Guests"
        ));

        RoundedButton cancelButton = new RoundedButton(
                "Cancel",
                RED,
                Color.WHITE
        );
        cancelButton.setPreferredSize(new Dimension(105, 34));
        cancelButton.addActionListener(e -> cancelReservation(
                reservation.getReservationId()
        ));

        card.add(detailsPanel, BorderLayout.CENTER);
        card.add(cancelButton, BorderLayout.EAST);

        return card;
    }

    private void cancelReservation(int reservationId) {
        int answer = JOptionPane.showConfirmDialog(
                this,
                "Cancel Reservation #" + reservationId + "?\n"
                        + "The table will become AVAILABLE again.",
                "Cancel Reservation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            boolean cancelled = reservationDAO.cancelReservation(reservationId);

            if (!cancelled) {
                showMessage(
                        "Reservation Not Found",
                        "The reservation was already cancelled or does not exist.",
                        true
                );
                return;
            }

            refreshReservationsList();
            refreshTablesGrid();
            refreshDashboard();
            showToast("Reservation #" + reservationId + " cancelled.");

        } catch (SQLException exception) {
            showMessage(
                    "Cancellation Error",
                    "Could not cancel the reservation.\n"
                            + exception.getMessage(),
                    true
            );
        }
    }

    private JComponent createTablesPage() {
        JPanel page = createPagePanel();

        page.add(createTitle(
                "Restaurant Tables",
                "Check availability and current table status."
        ));

        tablesGridPanel.setLayout(new GridLayout(0, 3, 18, 18));
        tablesGridPanel.setOpaque(false);

        page.add(Box.createVerticalStrut(20));
        page.add(tablesGridPanel);

        return wrapInScrollPane(page);
    }

    private void refreshTablesGrid() {
        tablesGridPanel.removeAll();

        try {
            List<RestaurantTable> tables =
                    tableDAO.getAllTables();

            if (tables.isEmpty()) {
                tablesGridPanel.add(
                        new JLabel("No restaurant tables are available.")
                );
            }

            for (RestaurantTable table : tables) {
                tablesGridPanel.add(createTableCard(table));
            }

        } catch (SQLException exception) {
            tablesGridPanel.add(new JLabel(
                    "Could not load tables: "
                            + exception.getMessage()
            ));
        }

        tablesGridPanel.revalidate();
        tablesGridPanel.repaint();
    }

    private JPanel createTableCard(RestaurantTable table) {
        JPanel card = new RoundedPanel(20, CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel tableLabel = new JLabel("Table " + table.getTableId());
        tableLabel.setFont(new Font("Serif", Font.BOLD, 22));
        tableLabel.setForeground(DARK);
        tableLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel detailLabel = new JLabel(
                table.getCapacity() + " Seats · " + table.getLocation()
        );

        detailLabel.setForeground(TEXT_GRAY);
        detailLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        detailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel statusLabel = new JLabel(table.getStatus());
        statusLabel.setForeground(getStatusColor(table.getStatus()));
        statusLabel.setFont(new Font("Arial", Font.BOLD, 13));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(tableLabel);
        card.add(Box.createVerticalStrut(12));
        card.add(detailLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(statusLabel);

        return card;
    }

    private JComponent createPaymentsPage() {
        JPanel page = createPagePanel();

        page.add(createTitle(
                "Payments and Bills",
                "Check customer bills and payment status."
        ));

        paymentsListPanel.setLayout(new BoxLayout(
                paymentsListPanel,
                BoxLayout.Y_AXIS
        ));

        paymentsListPanel.setBackground(BG);

        page.add(Box.createVerticalStrut(20));
        page.add(paymentsListPanel);

        return wrapInScrollPane(page);
    }

    private void refreshPaymentsList() {
        paymentsListPanel.removeAll();

        try {
            List<OrderDAO.OrderDetails> orders =
                    orderDAO.getAllOrders();

            if (orders.isEmpty()) {
                paymentsListPanel.add(
                        new JLabel("No orders are available.")
                );
            }

            for (OrderDAO.OrderDetails order : orders) {
                String paymentStatus =
                        paymentDAO.getPaymentStatus(order.getOrderId());

                paymentsListPanel.add(createPaymentCard(
                        order,
                        paymentStatus
                ));

                paymentsListPanel.add(Box.createVerticalStrut(12));
            }

        } catch (SQLException exception) {
            paymentsListPanel.add(new JLabel(
                    "Could not load payments: "
                            + exception.getMessage()
            ));
        }

        paymentsListPanel.revalidate();
        paymentsListPanel.repaint();
    }

    private JPanel createPaymentCard(
            OrderDAO.OrderDetails order,
            String paymentStatus
    ) {
        JPanel card = new RoundedPanel(18, CARD);
        card.setLayout(new GridLayout(1, 5, 15, 10));
        card.setBorder(new EmptyBorder(18, 20, 18, 20));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 82));

        card.add(createInfoLabel(
                "Order",
                "#" + order.getOrderId()
        ));

        card.add(createInfoLabel(
                "Customer",
                order.getCustomerName()
        ));

        card.add(createInfoLabel(
                "Amount",
                "₹" + order.getTotalAmount()
                        .stripTrailingZeros()
                        .toPlainString()
        ));

        card.add(createInfoLabel(
                "Order Status",
                order.getStatus()
        ));

        card.add(createInfoLabel(
                "Payment",
                paymentStatus
        ));

        return card;
    }

    private JPanel createInfoLabel(String title, String value) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(TEXT_GRAY);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setForeground(DARK);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 14));

        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(valueLabel);

        return panel;
    }

    private JPanel createPagePanel() {
        JPanel page = new JPanel();
        page.setBackground(BG);
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBorder(new EmptyBorder(32, 55, 40, 55));
        return page;
    }

    private JPanel createTitle(String title, String subtitle) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(DARK);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 32));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setForeground(TEXT_GRAY);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(7));
        panel.add(subtitleLabel);

        return panel;
    }

    private JScrollPane wrapInScrollPane(JPanel page) {
        JScrollPane scrollPane = new JScrollPane(page);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    private String getNextStatus(String currentStatus) {
        return switch (currentStatus) {
            case "PENDING" -> "CONFIRMED";
            case "CONFIRMED" -> "PREPARING";
            case "PREPARING" -> "READY";
            case "READY" -> "SERVED";
            default -> null;
        };
    }

    private String getButtonText(String nextStatus) {
        return switch (nextStatus) {
            case "CONFIRMED" -> "Confirm Order";
            case "PREPARING" -> "Start Preparing";
            case "READY" -> "Mark Ready";
            case "SERVED" -> "Mark Served";
            default -> "Update";
        };
    }

    private Color getStatusColor(String status) {
        return switch (status) {
            case "PENDING" -> RED;
            case "CONFIRMED" -> GOLD;
            case "PREPARING" -> BLUE;
            case "READY", "AVAILABLE", "PAID", "COMPLETED" -> GREEN;
            case "SERVED", "OCCUPIED" -> new Color(100, 100, 100);
            case "RESERVED", "AWAITING_PAYMENT" -> GOLD;
            default -> TEXT_GRAY;
        };
    }

    private void showToast(String message) {
        JWindow toast = new JWindow(this);

        JPanel toastPanel = new RoundedPanel(16, DARK);
        toastPanel.setBorder(new EmptyBorder(12, 18, 12, 18));

        JLabel label = new JLabel("✓  " + message);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 13));

        toastPanel.add(label);
        toast.add(toastPanel);
        toast.pack();

        Point location = getLocationOnScreen();

        toast.setLocation(
                location.x + getWidth() - toast.getWidth() - 35,
                location.y + getHeight() - toast.getHeight() - 55
        );

        toast.setVisible(true);

        Timer timer = new Timer(2200, e -> toast.dispose());
        timer.setRepeats(false);
        timer.start();
    }

    private void showMessage(
            String title,
            String message,
            boolean error
    ) {
        JOptionPane.showMessageDialog(
                this,
                message,
                title,
                error
                        ? JOptionPane.ERROR_MESSAGE
                        : JOptionPane.INFORMATION_MESSAGE
        );
    }

    static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color backgroundColor;

        RoundedPanel(int radius, Color backgroundColor) {
            this.radius = radius;
            this.backgroundColor = backgroundColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(backgroundColor);
            g2.fillRoundRect(
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    radius,
                    radius
            );

            g2.setColor(BORDER);
            g2.drawRoundRect(
                    0,
                    0,
                    getWidth() - 1,
                    getHeight() - 1,
                    radius,
                    radius
            );

            g2.dispose();
            super.paintComponent(graphics);
        }
    }

    static class RoundedButton extends JButton {
        private final Color buttonColor;

        RoundedButton(String text, Color buttonColor, Color textColor) {
            super(text);
            this.buttonColor = buttonColor;

            setForeground(textColor);
            setFont(new Font("Arial", Font.BOLD, 13));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(
                    getModel().isPressed()
                            ? buttonColor.darker()
                            : buttonColor
            );

            g2.fillRoundRect(
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    18,
                    18
            );

            g2.dispose();
            super.paintComponent(graphics);
        }
    }
}
