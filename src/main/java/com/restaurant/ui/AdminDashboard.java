package com.restaurant.ui;

import com.restaurant.dao.MenuDAO;
import com.restaurant.dao.OrderDAO;
import com.restaurant.dao.PaymentDAO;
import com.restaurant.dao.ReservationDAO;
import com.restaurant.dao.RestaurantTableDAO;
import com.restaurant.dao.RestaurantSettingsDAO;
import com.restaurant.db.DatabaseConnection;
import com.restaurant.model.MenuItem;
import com.restaurant.model.Reservation;
import com.restaurant.model.RestaurantTable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class AdminDashboard extends JFrame {

    private static final Color BG = new Color(248, 246, 242);
    private static final Color CARD = Color.WHITE;
    private static final Color DARK = new Color(45, 32, 24);
    private static final Color RED = new Color(180, 20, 55);
    private static final Color GOLD = new Color(202, 145, 45);
    private static final Color GREEN = new Color(36, 140, 83);
    private static final Color BLUE = new Color(61, 103, 173);
    private static final Color BORDER = new Color(225, 220, 214);
    private static final Color GRAY = new Color(110, 105, 100);

    private final String adminEmail;

    private final MenuDAO menuDAO = new MenuDAO();
    private final RestaurantTableDAO tableDAO = new RestaurantTableDAO();
    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final RestaurantSettingsDAO settingsDAO =
            new RestaurantSettingsDAO();

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private final JPanel dashboardPanel = new JPanel();

    private final DefaultTableModel menuModel = createModel(
            "Item ID", "Item Name", "Price", "Category", "Photo Path"
    );

    private final DefaultTableModel tableModel = createModel(
            "Table ID", "Location", "Capacity", "Status"
    );

    private final DefaultTableModel reservationModel = createModel(
            "Reservation ID", "Customer", "Date", "Time",
            "Guests", "Table", "Request"
    );

    private final DefaultTableModel orderModel = createModel(
            "Order ID", "Customer", "Table", "Items",
            "Total", "Staff", "Order Status", "Payment"
    );

    private final DefaultTableModel userModel = createModel(
            "User ID", "Email", "Role",
            "Customer ID", "Staff ID", "Name"
    );

    private final JTable menuTable = createTable(menuModel);
    private final JTable restaurantTable = createTable(tableModel);
    private final JTable reservationTable = createTable(reservationModel);
    private final JTable orderTable = createTable(orderModel);
    private final JTable userTable = createTable(userModel);

    public AdminDashboard(String adminEmail) {
        this.adminEmail = adminEmail;

        setTitle("Restaurant Management System - Admin Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        setLayout(new BorderLayout());
        add(createHeader(), BorderLayout.NORTH);

        contentPanel.setBackground(BG);
        contentPanel.add(createDashboardPage(), "DASHBOARD");
        contentPanel.add(createMenuPage(), "MENU");
        contentPanel.add(createTablesPage(), "TABLES");
        contentPanel.add(createReservationsPage(), "RESERVATIONS");
        contentPanel.add(createOrdersPage(), "ORDERS");
        contentPanel.add(createUsersPage(), "USERS");

        add(contentPanel, BorderLayout.CENTER);

        refreshAllData();
        cardLayout.show(contentPanel, "DASHBOARD");
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(DARK);
        header.setBorder(new EmptyBorder(12, 35, 12, 35));

        JLabel brand = new JLabel("Maison Ember  |  Admin Portal");
        brand.setFont(new Font("Serif", Font.BOLD, 22));
        brand.setForeground(Color.WHITE);

        JPanel navigation = new JPanel(new FlowLayout(
                FlowLayout.CENTER,
                8,
                0
        ));

        navigation.setOpaque(false);

        navigation.add(createNavButton("Dashboard", "DASHBOARD"));
        navigation.add(createNavButton("Menu", "MENU"));
        navigation.add(createNavButton("Tables", "TABLES"));
        navigation.add(createNavButton("Reservations", "RESERVATIONS"));
        navigation.add(createNavButton("Orders", "ORDERS"));
        navigation.add(createNavButton("Users", "USERS"));

        JPanel rightPanel = new JPanel(new FlowLayout(
                FlowLayout.RIGHT,
                12,
                0
        ));

        rightPanel.setOpaque(false);

        JLabel adminLabel = new JLabel("Admin: " + adminEmail);
        adminLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        adminLabel.setForeground(Color.WHITE);

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

        rightPanel.add(adminLabel);
        rightPanel.add(logoutButton);

        header.add(brand, BorderLayout.WEST);
        header.add(navigation, BorderLayout.CENTER);
        header.add(rightPanel, BorderLayout.EAST);

        return header;
    }

    private JButton createNavButton(String text, String pageName) {
        JButton button = new JButton(text);

        button.setForeground(Color.WHITE);
        button.setBackground(DARK);
        button.setFont(new Font("Arial", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
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
            case "MENU" -> refreshMenuTable();
            case "TABLES" -> refreshRestaurantTable();
            case "RESERVATIONS" -> refreshReservationTable();
            case "ORDERS" -> refreshOrderTable();
            case "USERS" -> refreshUserTable();
        }
    }

    private void refreshAllData() {
        refreshDashboard();
        refreshMenuTable();
        refreshRestaurantTable();
        refreshReservationTable();
        refreshOrderTable();
        refreshUserTable();
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
                "Admin Dashboard",
                "Control menu items, tables, reservations, orders, and users."
        ));

        try {
            List<OrderDAO.OrderDetails> orders = orderDAO.getAllOrders();
            List<Reservation> reservations = reservationDAO.getAllReservations();
            List<RestaurantTable> tables = tableDAO.getAllTables();

            int availableTables = 0;
            BigDecimal revenue = BigDecimal.ZERO;

            for (RestaurantTable table : tables) {
                if ("AVAILABLE".equals(table.getStatus())) {
                    availableTables++;
                }
            }

            for (OrderDAO.OrderDetails order : orders) {
                String paymentStatus =
                        paymentDAO.getPaymentStatus(order.getOrderId());

                if ("PAID".equals(paymentStatus)) {
                    revenue = revenue.add(order.getTotalAmount());
                }
            }

            JPanel statGrid = new JPanel(new GridLayout(1, 4, 18, 18));
            statGrid.setOpaque(false);
            statGrid.setMaximumSize(
                    new Dimension(Integer.MAX_VALUE, 130)
            );

            statGrid.add(createStatCard(
                    "Total Orders",
                    String.valueOf(orders.size()),
                    RED
            ));

            statGrid.add(createStatCard(
                    "Paid Revenue",
                    "₹" + revenue.stripTrailingZeros().toPlainString(),
                    GREEN
            ));

            statGrid.add(createStatCard(
                    "Reservations",
                    String.valueOf(reservations.size()),
                    GOLD
            ));

            statGrid.add(createStatCard(
                    "Available Tables",
                    String.valueOf(availableTables),
                    BLUE
            ));

            dashboardPanel.add(Box.createVerticalStrut(22));
            dashboardPanel.add(statGrid);
            dashboardPanel.add(Box.createVerticalStrut(32));

            JLabel quickTitle = new JLabel("Quick Actions");
            quickTitle.setFont(new Font("Serif", Font.BOLD, 25));
            quickTitle.setForeground(DARK);
            quickTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

            dashboardPanel.add(quickTitle);
            dashboardPanel.add(Box.createVerticalStrut(15));

            JPanel actionGrid = new JPanel(new GridLayout(1, 3, 18, 18));
            actionGrid.setOpaque(false);
            actionGrid.setMaximumSize(
                    new Dimension(Integer.MAX_VALUE, 120)
            );

            actionGrid.add(createQuickAction(
                    "Add Menu Item",
                    "MENU",
                    GOLD
            ));

            actionGrid.add(createQuickAction(
                    "Add Restaurant Table",
                    "TABLES",
                    GREEN
            ));

            actionGrid.add(createQuickAction(
                    "View User Accounts",
                    "USERS",
                    RED
            ));

            dashboardPanel.add(actionGrid);

        } catch (SQLException exception) {
            dashboardPanel.add(new JLabel(
                    "Could not load dashboard: "
                            + exception.getMessage()
            ));
        }

        dashboardPanel.revalidate();
        dashboardPanel.repaint();
    }

    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new RoundedPanel(20, CARD);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(18, 20, 18, 20));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(GRAY);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 28));
        valueLabel.setForeground(color);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.SOUTH);

        return card;
    }

    private JButton createQuickAction(String text, String page, Color color) {
        RoundedButton button = new RoundedButton(text, color, Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 15));

        button.addActionListener(e -> {
            refreshPage(page);
            cardLayout.show(contentPanel, page);
        });

        return button;
    }

    private JComponent createMenuPage() {
        JPanel page = createPagePanel();

        page.add(createTitle(
                "Menu Management",
                "Add, update, or remove food items from the real menu."
        ));

        page.add(Box.createVerticalStrut(20));

        JPanel buttonPanel = new JPanel(new FlowLayout(
                FlowLayout.LEFT,
                10,
                0
        ));

        buttonPanel.setOpaque(false);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton addButton = new RoundedButton(
                "+ Add Menu Item",
                GREEN,
                Color.WHITE
        );

        JButton editButton = new RoundedButton(
                "Edit Selected",
                GOLD,
                Color.WHITE
        );

        JButton deleteButton = new RoundedButton(
                "Delete Selected",
                RED,
                Color.WHITE
        );

        JButton mainImageButton = new RoundedButton(
                "Set Main Banner Image",
                BLUE,
                Color.WHITE
        );

        addButton.addActionListener(e -> showAddMenuDialog());
        editButton.addActionListener(e -> showEditMenuDialog());
        deleteButton.addActionListener(e -> deleteSelectedMenuItem());
        mainImageButton.addActionListener(e -> showMainImageDialog());

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(mainImageButton);

        page.add(buttonPanel);
        page.add(Box.createVerticalStrut(18));
        page.add(createTableScrollPane(menuTable));

        return wrapInScrollPane(page);
    }

    private void refreshMenuTable() {
        menuModel.setRowCount(0);

        try {
            List<MenuItem> menuItems = menuDAO.getAllMenuItems();

            for (MenuItem item : menuItems) {
                menuModel.addRow(new Object[]{
                        item.getItemId(),
                        item.getItemName(),
                        "₹" + item.getPrice()
                                .stripTrailingZeros()
                                .toPlainString(),
                        item.getCategory(),
                        item.getImagePath() == null
                                ? "No photo"
                                : item.getImagePath()
                });
            }

        } catch (SQLException exception) {
            showMessage(
                    "Menu Error",
                    "Could not load menu.\n" + exception.getMessage(),
                    true
            );
        }
    }

    private void showAddMenuDialog() {
        JTextField itemNameField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField categoryField = new JTextField();
        JTextField photoPathField = new JTextField();
        photoPathField.setEditable(false);

        JButton choosePhotoButton = new RoundedButton(
                "Choose Food Photo", BLUE, Color.WHITE
        );

        choosePhotoButton.addActionListener(e -> {
            String imagePath = selectAndSaveImage("menu");
            if (imagePath != null) {
                photoPathField.setText(imagePath);
            }
        });

        JPanel photoPanel = new JPanel(new BorderLayout(8, 0));
        photoPanel.setOpaque(false);
        photoPanel.add(photoPathField, BorderLayout.CENTER);
        photoPanel.add(choosePhotoButton, BorderLayout.EAST);

        Object[] form = {
                "Item name:", itemNameField,
                "Price:", priceField,
                "Category:", categoryField,
                "Food photo:", photoPanel
        };

        if (JOptionPane.showConfirmDialog(
                this, form, "Add Menu Item",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        ) != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            String itemName = itemNameField.getText().trim();
            String category = categoryField.getText().trim();

            if (itemName.isEmpty() || category.isEmpty()
                    || priceField.getText().trim().isEmpty()) {
                showMessage(
                        "Validation Error",
                        "Item name, price, and category are required.",
                        true
                );
                return;
            }

            menuDAO.addMenuItem(
                    itemName,
                    new BigDecimal(priceField.getText().trim()),
                    category,
                    photoPathField.getText().trim().isEmpty()
                            ? null
                            : photoPathField.getText().trim()
            );

            showMessage(
                    "Menu Updated",
                    "Menu item added successfully.",
                    false
            );
            refreshMenuTable();

        } catch (Exception exception) {
            showMessage(
                    "Menu Error",
                    "Could not add menu item.\n"
                            + exception.getMessage(),
                    true
            );
        }
    }

    private void showEditMenuDialog() {
        int row = menuTable.getSelectedRow();

        if (row == -1) {
            showMessage("Select Item", "Select a menu item first.", true);
            return;
        }

        int itemId = (Integer) menuModel.getValueAt(row, 0);
        JTextField nameField = new JTextField(
                String.valueOf(menuModel.getValueAt(row, 1))
        );
        JTextField priceField = new JTextField(
                String.valueOf(menuModel.getValueAt(row, 2)).replace("₹", "")
        );
        JTextField categoryField = new JTextField(
                String.valueOf(menuModel.getValueAt(row, 3))
        );
        JTextField photoPathField = new JTextField(
                String.valueOf(menuModel.getValueAt(row, 4))
                        .replace("No photo", "")
        );
        photoPathField.setEditable(false);

        JButton choosePhotoButton = new RoundedButton(
                "Change Photo", BLUE, Color.WHITE
        );

        choosePhotoButton.addActionListener(e -> {
            String imagePath = selectAndSaveImage("menu");
            if (imagePath != null) {
                photoPathField.setText(imagePath);
            }
        });

        JPanel photoPanel = new JPanel(new BorderLayout(8, 0));
        photoPanel.setOpaque(false);
        photoPanel.add(photoPathField, BorderLayout.CENTER);
        photoPanel.add(choosePhotoButton, BorderLayout.EAST);

        Object[] form = {
                "Item name:", nameField,
                "Price:", priceField,
                "Category:", categoryField,
                "Food photo:", photoPanel
        };

        if (JOptionPane.showConfirmDialog(
                this, form, "Edit Menu Item",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        ) != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            menuDAO.updateMenuItem(
                    itemId,
                    nameField.getText().trim(),
                    new BigDecimal(priceField.getText().trim()),
                    categoryField.getText().trim(),
                    photoPathField.getText().trim().isEmpty()
                            ? null
                            : photoPathField.getText().trim()
            );

            showMessage(
                    "Menu Updated",
                    "Menu item updated successfully.",
                    false
            );
            refreshMenuTable();

        } catch (Exception exception) {
            showMessage(
                    "Menu Error",
                    "Could not update menu item.\n"
                            + exception.getMessage(),
                    true
            );
        }
    }

    private void deleteSelectedMenuItem() {
        int row = menuTable.getSelectedRow();

        if (row == -1) {
            showMessage(
                    "Select Item",
                    "Select a menu item first.",
                    true
            );

            return;
        }

        int itemId = (Integer) menuModel.getValueAt(row, 0);

        int answer = JOptionPane.showConfirmDialog(
                this,
                "Delete this menu item?",
                "Delete Menu Item",
                JOptionPane.YES_NO_OPTION
        );

        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            menuDAO.deleteMenuItem(itemId);

            showMessage(
                    "Menu Updated",
                    "Menu item deleted successfully.",
                    false
            );

            refreshMenuTable();

        } catch (SQLException exception) {
            showMessage(
                    "Delete Error",
                    "This menu item may be used by an existing order.\n"
                            + exception.getMessage(),
                    true
            );
        }
    }

    private void showMainImageDialog() {
        JTextField imagePathField = new JTextField();
        imagePathField.setEditable(false);

        try {
            String savedPath = settingsDAO.getMainImagePath();
            if (savedPath != null) {
                imagePathField.setText(savedPath);
            }
        } catch (SQLException ignored) {
            // The user can still choose a new image.
        }

        JButton chooseButton = new RoundedButton(
                "Choose Main Image", BLUE, Color.WHITE
        );

        chooseButton.addActionListener(e -> {
            String imagePath = selectAndSaveImage("banner");
            if (imagePath != null) {
                imagePathField.setText(imagePath);
            }
        });

        JPanel imagePanel = new JPanel(new BorderLayout(8, 0));
        imagePanel.setOpaque(false);
        imagePanel.add(imagePathField, BorderLayout.CENTER);
        imagePanel.add(chooseButton, BorderLayout.EAST);

        Object[] form = {
                "Main customer banner image:", imagePanel
        };

        if (JOptionPane.showConfirmDialog(
                this,
                form,
                "Set Main Banner Image",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        ) != JOptionPane.OK_OPTION) {
            return;
        }

        if (imagePathField.getText().trim().isEmpty()) {
            showMessage(
                    "Select Image",
                    "Choose a restaurant banner image first.",
                    true
            );
            return;
        }

        try {
            settingsDAO.saveMainImagePath(
                    imagePathField.getText().trim()
            );

            showMessage(
                    "Banner Updated",
                    "The customer home page will show this image next time it opens.",
                    false
            );

        } catch (SQLException exception) {
            showMessage(
                    "Banner Error",
                    "Could not save the main image.\n"
                            + exception.getMessage(),
                    true
            );
        }
    }

    private String selectAndSaveImage(String folderName) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose Restaurant Image");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Image Files", "jpg", "jpeg", "png", "webp"
        ));

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        File selectedFile = chooser.getSelectedFile();

        try {
            String fileName = selectedFile.getName();
            String extension = "";
            int dot = fileName.lastIndexOf(".");

            if (dot >= 0) {
                extension = fileName.substring(dot);
            }

            Path folder = Path.of(
                    "src", "main", "resources", "images", folderName
            );

            Files.createDirectories(folder);

            String newFileName = UUID.randomUUID() + extension;
            Path target = folder.resolve(newFileName);

            Files.copy(
                    selectedFile.toPath(),
                    target,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return "/images/" + folderName + "/" + newFileName;

        } catch (IOException exception) {
            showMessage(
                    "Image Error",
                    "Could not save the selected image.\n"
                            + exception.getMessage(),
                    true
            );
            return null;
        }
    }

    private JComponent createTablesPage() {
        JPanel page = createPagePanel();

        page.add(createTitle(
                "Restaurant Table Management",
                "Add tables and update availability status."
        ));

        page.add(Box.createVerticalStrut(20));

        JPanel buttonPanel = new JPanel(new FlowLayout(
                FlowLayout.LEFT,
                10,
                0
        ));

        buttonPanel.setOpaque(false);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton addButton = new RoundedButton(
                "+ Add Table",
                GREEN,
                Color.WHITE
        );

        JButton updateStatusButton = new RoundedButton(
                "Update Status",
                GOLD,
                Color.WHITE
        );

        addButton.addActionListener(e -> showAddTableDialog());
        updateStatusButton.addActionListener(e -> showTableStatusDialog());

        buttonPanel.add(addButton);
        buttonPanel.add(updateStatusButton);

        page.add(buttonPanel);
        page.add(Box.createVerticalStrut(18));
        page.add(createTableScrollPane(restaurantTable));

        return wrapInScrollPane(page);
    }

    private void refreshRestaurantTable() {
        tableModel.setRowCount(0);

        try {
            List<RestaurantTable> tables = tableDAO.getAllTables();

            for (RestaurantTable table : tables) {
                tableModel.addRow(new Object[]{
                        table.getTableId(),
                        table.getLocation(),
                        table.getCapacity(),
                        table.getStatus()
                });
            }

        } catch (SQLException exception) {
            showMessage(
                    "Table Error",
                    "Could not load restaurant tables.\n"
                            + exception.getMessage(),
                    true
            );
        }
    }

    private void showAddTableDialog() {
        JTextField locationField = new JTextField();
        JTextField capacityField = new JTextField();

        Object[] form = {
                "Location:", locationField,
                "Capacity:", capacityField
        };

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                "Add Restaurant Table",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            tableDAO.addTable(
                    locationField.getText().trim(),
                    Integer.parseInt(capacityField.getText().trim())
            );

            showMessage(
                    "Table Added",
                    "Restaurant table added successfully.",
                    false
            );

            refreshRestaurantTable();
            refreshDashboard();

        } catch (Exception exception) {
            showMessage(
                    "Table Error",
                    "Could not add table.\n"
                            + exception.getMessage(),
                    true
            );
        }
    }

    private void showTableStatusDialog() {
        int row = restaurantTable.getSelectedRow();

        if (row == -1) {
            showMessage(
                    "Select Table",
                    "Select a restaurant table first.",
                    true
            );

            return;
        }

        int tableId = (Integer) tableModel.getValueAt(row, 0);

        String status = (String) JOptionPane.showInputDialog(
                this,
                "Choose table status:",
                "Update Table Status",
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[]{
                        "AVAILABLE",
                        "RESERVED",
                        "OCCUPIED",
                        "UNAVAILABLE"
                },
                tableModel.getValueAt(row, 3)
        );

        if (status == null) {
            return;
        }

        try {
            tableDAO.updateTableStatus(tableId, status);

            showMessage(
                    "Table Updated",
                    "Table status updated successfully.",
                    false
            );

            refreshRestaurantTable();
            refreshDashboard();

        } catch (SQLException exception) {
            showMessage(
                    "Table Error",
                    "Could not update table status.\n"
                            + exception.getMessage(),
                    true
            );
        }
    }

    private JComponent createReservationsPage() {
        JPanel page = createPagePanel();

        page.add(createTitle(
                "Reservation Management",
                "View reservations created by registered customers."
        ));

        page.add(Box.createVerticalStrut(20));
        page.add(createTableScrollPane(reservationTable));

        return wrapInScrollPane(page);
    }

    private void refreshReservationTable() {
        reservationModel.setRowCount(0);

        try {
            List<Reservation> reservations =
                    reservationDAO.getAllReservations();

            for (Reservation reservation : reservations) {
                reservationModel.addRow(new Object[]{
                        reservation.getReservationId(),
                        reservation.getCustomerName(),
                        reservation.getReservationDate(),
                        reservation.getReservationTime(),
                        reservation.getNumberOfGuests(),
                        "Table " + reservation.getTableId()
                                + " · "
                                + reservation.getTableLocation(),
                        reservation.getSpecialRequest()
                });
            }

        } catch (SQLException exception) {
            showMessage(
                    "Reservation Error",
                    "Could not load reservations.\n"
                            + exception.getMessage(),
                    true
            );
        }
    }

    private JComponent createOrdersPage() {
        JPanel page = createPagePanel();

        page.add(createTitle(
                "Order Management",
                "View orders, staff assignment, total bills, and payments."
        ));

        page.add(Box.createVerticalStrut(20));

        JPanel buttonPanel = new JPanel(new FlowLayout(
                FlowLayout.LEFT,
                10,
                0
        ));

        buttonPanel.setOpaque(false);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton deleteButton = new RoundedButton(
                "Delete Completed Paid Order",
                RED,
                Color.WHITE
        );

        deleteButton.addActionListener(e -> deleteSelectedOrder());

        buttonPanel.add(deleteButton);

        page.add(buttonPanel);
        page.add(Box.createVerticalStrut(18));
        page.add(createTableScrollPane(orderTable));

        return wrapInScrollPane(page);
    }

    private void refreshOrderTable() {
        orderModel.setRowCount(0);

        try {
            List<OrderDAO.OrderDetails> orders =
                    orderDAO.getAllOrders();

            for (OrderDAO.OrderDetails order : orders) {
                String paymentStatus =
                        paymentDAO.getPaymentStatus(order.getOrderId());

                String tableText = order.getTableId() == null
                        ? "Takeaway"
                        : "Table " + order.getTableId()
                        + " · " + order.getTableLocation();

                orderModel.addRow(new Object[]{
                        order.getOrderId(),
                        order.getCustomerName(),
                        tableText,
                        order.getOrderItems(),
                        "₹" + order.getTotalAmount()
                                .stripTrailingZeros()
                                .toPlainString(),
                        order.getStaffName(),
                        order.getStatus(),
                        paymentStatus
                });
            }

        } catch (SQLException exception) {
            showMessage(
                    "Order Error",
                    "Could not load orders.\n"
                            + exception.getMessage(),
                    true
            );
        }
    }

    private void deleteSelectedOrder() {
        int row = orderTable.getSelectedRow();

        if (row == -1) {
            showMessage(
                    "Select Order",
                    "Select a completed paid order first.",
                    true
            );

            return;
        }

        int orderId = (Integer) orderModel.getValueAt(row, 0);
        String paymentStatus = String.valueOf(
                orderModel.getValueAt(row, 7)
        );

        if (!"PAID".equals(paymentStatus)) {
            showMessage(
                    "Cannot Delete",
                    "Only paid orders can be deleted.",
                    true
            );

            return;
        }

        int answer = JOptionPane.showConfirmDialog(
                this,
                "Delete Order #" + orderId + "?",
                "Delete Completed Order",
                JOptionPane.YES_NO_OPTION
        );

        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            boolean deleted =
                    orderDAO.deleteCompletedOrder(orderId);

            if (deleted) {
                showMessage(
                        "Order Deleted",
                        "Completed paid order deleted successfully.",
                        false
                );

                refreshOrderTable();
                refreshDashboard();

            } else {
                showMessage(
                        "Cannot Delete",
                        "Only paid orders can be deleted.",
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

    private JComponent createUsersPage() {
        JPanel page = createPagePanel();

        page.add(createTitle(
                "User Account Management",
                "View customers, staff members, and administrators."
        ));

        page.add(Box.createVerticalStrut(20));

        JLabel note = new JLabel(
                "Customers are created only through Customer Registration."
        );

        note.setForeground(RED);
        note.setFont(new Font("Arial", Font.BOLD, 13));
        note.setAlignmentX(Component.LEFT_ALIGNMENT);

        page.add(note);
        page.add(Box.createVerticalStrut(15));
        page.add(createTableScrollPane(userTable));

        return wrapInScrollPane(page);
    }

    private void refreshUserTable() {
        userModel.setRowCount(0);

        String sql = """
                SELECT
                    ua.User_ID,
                    ua.Email,
                    ua.Role,
                    ua.Customer_ID,
                    ua.Staff_ID,

                    COALESCE(
                        CONCAT(c.First_Name, ' ', c.Last_Name),
                        CONCAT(s.First_Name, ' ', s.Last_Name),
                        'No linked profile'
                    ) AS Full_Name

                FROM User_Account ua

                LEFT JOIN Customer c
                    ON ua.Customer_ID = c.Customer_ID

                LEFT JOIN Staff s
                    ON ua.Staff_ID = s.Staff_ID

                ORDER BY ua.User_ID DESC
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()
        ) {
            while (result.next()) {
                userModel.addRow(new Object[]{
                        result.getInt("User_ID"),
                        result.getString("Email"),
                        result.getString("Role"),
                        result.getObject("Customer_ID"),
                        result.getObject("Staff_ID"),
                        result.getString("Full_Name")
                });
            }

        } catch (SQLException exception) {
            showMessage(
                    "User Error",
                    "Could not load user accounts.\n"
                            + exception.getMessage(),
                    true
            );
        }
    }

    private DefaultTableModel createModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private JTable createTable(DefaultTableModel model) {
        JTable table = new JTable(model);

        table.setRowHeight(38);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setForeground(DARK);
        table.setBackground(Color.WHITE);
        table.setGridColor(BORDER);
        table.setSelectionBackground(new Color(252, 230, 236));
        table.setSelectionForeground(DARK);

        table.getTableHeader().setFont(
                new Font("Arial", Font.BOLD, 13)
        );

        table.getTableHeader().setBackground(DARK);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setReorderingAllowed(false);

        return table;
    }

    private JScrollPane createTableScrollPane(JTable table) {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER));
        scrollPane.setPreferredSize(new Dimension(1000, 400));
        return scrollPane;
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
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(DARK);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 32));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setForeground(GRAY);
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

