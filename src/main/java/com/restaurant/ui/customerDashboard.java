package com.restaurant.ui;

import com.restaurant.dao.MenuDAO;
import com.restaurant.dao.OrderDAO;
import com.restaurant.dao.PaymentDAO;
import com.restaurant.dao.ReservationDAO;
import com.restaurant.dao.RestaurantTableDAO;
import com.restaurant.dao.RestaurantSettingsDAO;
import com.restaurant.dao.UserAccountDAO;
import com.restaurant.model.MenuItem;
import com.restaurant.model.Reservation;
import com.restaurant.model.RestaurantTable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.File;
import java.net.URL;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class customerDashboard extends JFrame {

    private static final Color BG = new Color(255, 252, 247);
    private static final Color CARD = Color.WHITE;
    private static final Color BROWN = new Color(82, 52, 30);
    private static final Color GOLD = new Color(194, 137, 42);
    private static final Color GREEN = new Color(47, 138, 85);
    private static final Color RED = new Color(195, 61, 61);
    private static final Color BLUE = new Color(66, 105, 175);
    private static final Color TOAST_GREEN = new Color(36, 160, 95);

    private final String customerEmail;
    private int customerId = -1;

    private final MenuDAO menuDAO = new MenuDAO();
    private final RestaurantTableDAO tableDAO = new RestaurantTableDAO();
    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final UserAccountDAO userAccountDAO = new UserAccountDAO();
    private final RestaurantSettingsDAO settingsDAO =
            new RestaurantSettingsDAO();

    private String mainImagePath;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel pageContainer = new JPanel(cardLayout);

    /*
     * Cart stores real Menu_Item objects from the database.
     */
    private final Map<MenuItem, Integer> cart = new LinkedHashMap<>();

    private List<MenuItem> menuItems = new ArrayList<>();
    private List<RestaurantTable> availableTables = new ArrayList<>();

    private final JButton cartButton = new JButton("🛒 Cart (0)");
    private final JPanel cartRowsPanel = new JPanel();
    private final JPanel ordersPanel = new JPanel();
    private final JPanel reservationsPanel = new JPanel();
    private final JPanel reservationGrid = new JPanel();

    private final JLabel subtotalLabel = new JLabel("₹0");
    private final JLabel totalLabel = new JLabel("₹0");

    private final JComboBox<RestaurantTable> orderTableBox =
            new JComboBox<>();

    private final JTextField dateField =
            new JTextField(LocalDate.now().toString());

    private final JComboBox<String> timeBox = new JComboBox<>(
            new String[]{"12:00", "13:00", "18:00", "19:00", "20:00"}
    );

    private final JSpinner guestsSpinner =
            new JSpinner(new SpinnerNumberModel(2, 1, 12, 1));

    private final JTextField requestField = new JTextField();

    private RestaurantTable selectedReservationTable;

    public customerDashboard(String customerEmail) {
        this.customerEmail = customerEmail;

        setTitle("Maison Ember - Customer Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        loadInitialDatabaseData();
        add(createMainPanel());
    }

    private void loadInitialDatabaseData() {
        try {
            customerId = userAccountDAO.getCustomerIdByEmail(customerEmail);
            menuItems = menuDAO.getAllMenuItems();
            availableTables = tableDAO.getAvailableTables();
            mainImagePath = settingsDAO.getMainImagePath();
            refreshOrderTableBox();

        } catch (SQLException exception) {
            showInfo(
                    "Database Error",
                    "Could not load customer data.\n"
                            + exception.getMessage()
            );
        }
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG);

        mainPanel.add(createNavigationBar(), BorderLayout.NORTH);

        pageContainer.setBackground(BG);
        pageContainer.add(createHomePage(), "HOME");
        pageContainer.add(createMenuPage(), "MENU");
        pageContainer.add(createCartPage(), "CART");
        pageContainer.add(createReservationPage(), "RESERVE");
        pageContainer.add(createOrdersPage(), "ORDERS");
        pageContainer.add(createReservationsPage(), "RESERVATIONS");

        mainPanel.add(pageContainer, BorderLayout.CENTER);
        mainPanel.add(createFooter(), BorderLayout.SOUTH);

        cardLayout.show(pageContainer, "HOME");

        return mainPanel;
    }

    private JPanel createNavigationBar() {
        JPanel nav = new JPanel(new BorderLayout());
        nav.setBackground(CARD);
        nav.setBorder(new EmptyBorder(10, 35, 10, 35));

        JLabel logo = new JLabel("Maison Ember");
        logo.setFont(new Font("Georgia", Font.BOLD, 20));
        logo.setForeground(BROWN);

        JPanel links = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        links.setOpaque(false);

        links.add(navButton("Home", e -> showPage("HOME")));
        links.add(navButton("Menu", e -> showPage("MENU")));
        links.add(navButton("Reserve Table", e -> {
            reloadAvailableTables();
            showPage("RESERVE");
        }));
        links.add(navButton("My Orders", e -> {
            refreshOrdersPanel();
            showPage("ORDERS");
        }));
        links.add(navButton("My Reservations", e -> {
            refreshReservationsPanel();
            showPage("RESERVATIONS");
        }));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 0));
        left.setOpaque(false);
        left.add(logo);
        left.add(links);

        cartButton.setFont(new Font("Arial", Font.BOLD, 12));
        cartButton.setBackground(GOLD);
        cartButton.setForeground(Color.WHITE);
        cartButton.setFocusPainted(false);
        cartButton.addActionListener(e -> showPage("CART"));

        JLabel profile = new JLabel("👤 " + customerEmail);
        profile.setFont(new Font("Arial", Font.BOLD, 12));
        profile.setCursor(new Cursor(Cursor.HAND_CURSOR));

        profile.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showInfo(
                        "My Profile",
                        "Customer Profile\n\n"
                                + "Email: " + customerEmail
                                + "\nCustomer ID: " + customerId
                                + "\nRole: CUSTOMER"
                );
            }
        });

        JButton logout = navButton("Logout", e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);
        right.add(cartButton);
        right.add(profile);
        right.add(logout);

        nav.add(left, BorderLayout.WEST);
        nav.add(right, BorderLayout.EAST);

        return nav;
    }

    private JButton navButton(
            String text,
            java.awt.event.ActionListener action
    ) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setForeground(BROWN);
        button.setBackground(CARD);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(action);

        return button;
    }

    private JComponent createHomePage() {
        JPanel page = createPagePanel();

        page.add(createHeroPanel());
        page.add(Box.createVerticalStrut(25));
        page.add(sectionTitle("Categories"));
        page.add(Box.createVerticalStrut(12));
        page.add(createCategoryPanel());
        page.add(Box.createVerticalStrut(28));
        page.add(sectionTitle("Popular Dishes"));
        page.add(Box.createVerticalStrut(12));

        int itemCount = Math.min(4, menuItems.size());

        if (itemCount > 0) {
            page.add(createDishGrid(menuItems.subList(0, itemCount)));
        } else {
            page.add(new JLabel("No menu items are available yet."));
        }

        return wrapInScrollPane(page);
    }

    private JComponent createMenuPage() {
        JPanel page = createPagePanel();

        JLabel title = sectionTitle("Our Menu");
        page.add(title);
        page.add(Box.createVerticalStrut(8));

        JLabel subtitle = new JLabel(
                "Choose your favourite dish and add it to your cart."
        );

        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitle.setForeground(new Color(110, 95, 80));

        page.add(subtitle);
        page.add(Box.createVerticalStrut(22));

        if (menuItems.isEmpty()) {
            page.add(new JLabel(
                    "Menu is empty. Ask Admin to add food items."
            ));
        } else {
            page.add(createDishGrid(menuItems));
        }

        return wrapInScrollPane(page);
    }

    private JPanel createHeroPanel() {
        RoundedPanel hero = new RoundedPanel(
                28,
                new Color(250, 235, 206)
        );
        hero.setLayout(new BorderLayout(30, 0));
        hero.setMaximumSize(new Dimension(Integer.MAX_VALUE, 335));
        hero.setBorder(new EmptyBorder(35, 32, 35, 32));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel(
                "<html>Exceptional Food.<br>Unforgettable<br>Moments.</html>"
        );

        heading.setFont(new Font("Georgia", Font.BOLD, 37));
        heading.setForeground(BROWN);

        JLabel subtitle = new JLabel(
                "Reserve your table and discover dishes prepared with passion."
        );

        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitle.setForeground(new Color(100, 70, 35));

        RoundedButton reserve = new RoundedButton("Reserve a Table");
        reserve.setBackground(GOLD);
        reserve.setForeground(Color.WHITE);
        reserve.addActionListener(e -> {
            reloadAvailableTables();
            showPage("RESERVE");
        });

        RoundedButton explore = new RoundedButton("Explore Menu");
        explore.setBackground(Color.WHITE);
        explore.setForeground(BROWN);
        explore.addActionListener(e -> showPage("MENU"));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(reserve);
        buttonPanel.add(explore);

        textPanel.add(heading);
        textPanel.add(Box.createVerticalStrut(18));
        textPanel.add(subtitle);
        textPanel.add(Box.createVerticalStrut(22));
        textPanel.add(buttonPanel);

        JLabel food = new JLabel("", SwingConstants.CENTER);
        food.setPreferredSize(new Dimension(490, 260));
        food.setOpaque(false);

        ImageIcon mainImage = loadMenuImage(mainImagePath, 460, 260);

        if (mainImage != null) {
            food.setIcon(mainImage);
        } else {
            food.setText(
                    "<html><div style='text-align:center;'>"
                            + "<span style='font-size:70px;'>🍽</span><br>"
                            + "<span style='font-size:13px;'>"
                            + "Admin can add the restaurant banner image"
                            + "</span></div></html>"
            );
            food.setForeground(new Color(125, 88, 45));
        }

        hero.add(textPanel, BorderLayout.CENTER);
        hero.add(food, BorderLayout.EAST);

        return hero;
    }

    private JPanel createCategoryPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 6, 14, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 86));

        addCategory(panel, "🍢", "Starters");
        addCategory(panel, "🍛", "Main Course");
        addCategory(panel, "🍕", "Pizza");
        addCategory(panel, "🍔", "Burgers");
        addCategory(panel, "🍝", "Pasta");
        addCategory(panel, "🍰", "Dessert");

        return panel;
    }

    private void addCategory(JPanel panel, String emoji, String name) {
        RoundedPanel categoryCard = new RoundedPanel(18, CARD);
        categoryCard.setLayout(new BorderLayout());
        categoryCard.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel icon = new JLabel(emoji, SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel label = new JLabel(name, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 13));
        label.setForeground(BROWN);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(Box.createVerticalStrut(12));
        content.add(icon);
        content.add(Box.createVerticalStrut(5));
        content.add(label);

        categoryCard.add(content, BorderLayout.CENTER);

        java.awt.event.MouseAdapter openMenu =
                new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent event) {
                        showPage("MENU");
                    }
                };

        categoryCard.addMouseListener(openMenu);
        icon.addMouseListener(openMenu);
        label.addMouseListener(openMenu);

        panel.add(categoryCard);
    }

    private JPanel createDishGrid(List<MenuItem> items) {
        JPanel grid = new JPanel(new GridLayout(0, 4, 15, 15));
        grid.setOpaque(false);

        for (MenuItem item : items) {
            grid.add(createDishCard(item));
        }

        return grid;
    }

    private JPanel createDishCard(MenuItem item) {
        RoundedPanel card = new RoundedPanel(22, CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(12, 12, 12, 12));
        card.setPreferredSize(new Dimension(235, 280));

        JLabel photo = createMenuPhoto(item);
        photo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel name = new JLabel(item.getItemName());
        name.setFont(new Font("Arial", Font.BOLD, 15));
        name.setForeground(BROWN);
        name.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel description = new JLabel(
                "<html><span style='color:#7B6B60;'>"
                        + item.getCategory()
                        + " · Prepared fresh by our chefs."
                        + "</span></html>"
        );
        description.setFont(new Font("Arial", Font.PLAIN, 11));
        description.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel price = new JLabel(
                "₹" + item.getPrice().stripTrailingZeros().toPlainString()
        );
        price.setFont(new Font("Arial", Font.BOLD, 15));
        price.setForeground(GOLD);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        actionPanel.setOpaque(false);
        refreshDishAction(item, actionPanel);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        bottomPanel.add(price, BorderLayout.WEST);
        bottomPanel.add(actionPanel, BorderLayout.EAST);

        card.add(photo);
        card.add(Box.createVerticalStrut(10));
        card.add(name);
        card.add(Box.createVerticalStrut(4));
        card.add(description);
        card.add(Box.createVerticalGlue());
        card.add(bottomPanel);
        return card;
    }

    private JLabel createMenuPhoto(MenuItem item) {
        JLabel photo = new JLabel("", SwingConstants.CENTER);
        photo.setPreferredSize(new Dimension(210, 125));
        photo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 125));
        photo.setOpaque(true);
        photo.setBackground(new Color(250, 241, 226));
        photo.setBorder(new LineBorder(new Color(239, 229, 217), 1, true));

        ImageIcon icon = loadMenuImage(item.getImagePath(), 210, 125);
        if (icon != null) {
            photo.setIcon(icon);
        } else {
            photo.setText(getFoodEmoji(item.getCategory()));
            photo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        }
        return photo;
    }

    private ImageIcon loadMenuImage(String imagePath, int width, int height) {
        if (imagePath == null || imagePath.isBlank()
                || imagePath.equalsIgnoreCase("No photo")) {
            return null;
        }

        try {
            Image image = null;
            File imageFile = new File(
                    System.getProperty("user.dir")
                            + File.separator + "src"
                            + File.separator + "main"
                            + File.separator + "resources"
                            + imagePath.replace("/", File.separator)
            );

            if (imageFile.exists()) {
                image = new ImageIcon(imageFile.getAbsolutePath()).getImage();
            }

            if (image == null) {
                URL imageUrl = getClass().getResource(imagePath);
                if (imageUrl != null) {
                    image = new ImageIcon(imageUrl).getImage();
                }
            }

            if (image == null) {
                return null;
            }

            return new ImageIcon(image.getScaledInstance(
                    width, height, Image.SCALE_SMOOTH
            ));
        } catch (Exception exception) {
            return null;
        }
    }

    private void refreshDishAction(MenuItem item, JPanel actionPanel) {
        actionPanel.removeAll();
        int quantity = cart.getOrDefault(item, 0);

        if (quantity == 0) {
            RoundedButton addButton = new RoundedButton("Add to Cart");
            addButton.setBackground(GREEN);
            addButton.setForeground(Color.WHITE);
            addButton.setFont(new Font("Arial", Font.BOLD, 11));
            addButton.setPreferredSize(new Dimension(105, 30));

            addButton.addActionListener(e -> {
                cart.put(item, 1);
                updateCartButton();
                refreshCartPage();
                refreshDishAction(item, actionPanel);
                actionPanel.revalidate();
                actionPanel.repaint();
            });

            actionPanel.add(addButton);
            return;
        }

        JLabel addedLabel = new JLabel("✓ Added");
        addedLabel.setFont(new Font("Arial", Font.BOLD, 11));
        addedLabel.setForeground(GREEN);

        RoundedButton minus = createCartControl(
                "−",
                new Color(239, 247, 242)
        );

        RoundedButton plus = createCartControl("+", GREEN);
        minus.setForeground(GREEN);
        plus.setForeground(Color.WHITE);

        JLabel quantityLabel = new JLabel(String.valueOf(quantity));
        quantityLabel.setFont(new Font("Arial", Font.BOLD, 13));
        quantityLabel.setHorizontalAlignment(SwingConstants.CENTER);
        quantityLabel.setPreferredSize(new Dimension(20, 28));

        minus.addActionListener(e -> {
            changeQuantity(item, -1);
            refreshDishAction(item, actionPanel);
            actionPanel.revalidate();
            actionPanel.repaint();
        });

        plus.addActionListener(e -> {
            changeQuantity(item, 1);
            refreshDishAction(item, actionPanel);
            actionPanel.revalidate();
            actionPanel.repaint();
        });

        actionPanel.add(addedLabel);
        actionPanel.add(minus);
        actionPanel.add(quantityLabel);
        actionPanel.add(plus);
    }

    private JPanel createCartPage() {
        JPanel page = createPagePanel();
        page.setLayout(new BorderLayout(18, 0));

        JPanel left = new JPanel(new BorderLayout(0, 15));
        left.setOpaque(false);

        left.add(sectionTitle("Your Cart"), BorderLayout.NORTH);

        cartRowsPanel.setLayout(new BoxLayout(cartRowsPanel, BoxLayout.Y_AXIS));
        cartRowsPanel.setBackground(CARD);

        cartRowsPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(225, 213, 202), 1, true),
                new EmptyBorder(14, 14, 14, 14)
        ));

        left.add(cartRowsPanel, BorderLayout.CENTER);

        page.add(left, BorderLayout.CENTER);
        page.add(createCheckoutPanel(), BorderLayout.EAST);

        refreshCartPage();

        return page;
    }

    private JPanel createCheckoutPanel() {
        JPanel checkout = new JPanel();
        checkout.setPreferredSize(new Dimension(300, 0));
        checkout.setBackground(CARD);
        checkout.setLayout(new BoxLayout(checkout, BoxLayout.Y_AXIS));

        checkout.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(225, 213, 202), 1, true),
                new EmptyBorder(18, 18, 18, 18)
        ));

        JLabel title = new JLabel("Checkout");
        title.setFont(new Font("Georgia", Font.BOLD, 20));
        title.setForeground(BROWN);

        JLabel subtotalText = new JLabel("Food Total");
        JLabel totalText = new JLabel("Total Bill");

        subtotalLabel.setFont(new Font("Arial", Font.BOLD, 14));
        totalLabel.setFont(new Font("Arial", Font.BOLD, 18));
        totalLabel.setForeground(new Color(175, 115, 20));

        checkout.add(title);
        checkout.add(Box.createVerticalStrut(16));
        checkout.add(createPriceRow(subtotalText, subtotalLabel));
        checkout.add(Box.createVerticalStrut(9));
        checkout.add(createPriceRow(totalText, totalLabel));
        checkout.add(Box.createVerticalStrut(20));

        JLabel orderTypeLabel = new JLabel("Order type");
        orderTypeLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        JComboBox<String> orderTypeBox = new JComboBox<>(
                new String[]{"Dine-in", "Takeaway"}
        );

        checkout.add(orderTypeLabel);
        checkout.add(Box.createVerticalStrut(5));
        checkout.add(orderTypeBox);
        checkout.add(Box.createVerticalStrut(12));

        JLabel tableLabel = new JLabel("Select table for dine-in");
        tableLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        checkout.add(tableLabel);
        checkout.add(Box.createVerticalStrut(5));
        checkout.add(orderTableBox);
        checkout.add(Box.createVerticalStrut(20));

        RoundedButton placeOrder = new RoundedButton("Place Order");
        placeOrder.setBackground(GOLD);
        placeOrder.setForeground(Color.WHITE);
        placeOrder.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        placeOrder.addActionListener(e -> {
            String orderType = (String) orderTypeBox.getSelectedItem();
            placeOrder(orderType);
        });

        RoundedButton continueShopping =
                new RoundedButton("Continue Shopping");

        continueShopping.setBackground(new Color(245, 241, 236));
        continueShopping.setForeground(BROWN);
        continueShopping.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 42)
        );

        continueShopping.addActionListener(e -> showPage("MENU"));

        checkout.add(placeOrder);
        checkout.add(Box.createVerticalStrut(10));
        checkout.add(continueShopping);

        return checkout;
    }

    private JPanel createPriceRow(JLabel text, JLabel amount) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.add(text, BorderLayout.WEST);
        row.add(amount, BorderLayout.EAST);
        return row;
    }

    private JComponent createReservationPage() {
        JPanel page = createPagePanel();

        page.add(sectionTitle("Reserve a Table"));
        page.add(Box.createVerticalStrut(15));
        page.add(createReservationForm());
        page.add(Box.createVerticalStrut(22));

        JLabel available = sectionTitle("Available Tables");
        available.setFont(new Font("Georgia", Font.BOLD, 18));

        page.add(available);
        page.add(Box.createVerticalStrut(12));

        reservationGrid.setLayout(new GridLayout(0, 4, 12, 12));
        reservationGrid.setOpaque(false);

        rebuildReservationGrid();
        page.add(reservationGrid);

        RoundedButton confirm = new RoundedButton("Confirm Reservation");
        confirm.setBackground(GOLD);
        confirm.setForeground(Color.WHITE);
        confirm.setAlignmentX(Component.LEFT_ALIGNMENT);
        confirm.addActionListener(e -> confirmReservation());

        page.add(Box.createVerticalStrut(14));
        page.add(confirm);

        return wrapInScrollPane(page);
    }

    private JPanel createReservationForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(CARD);

        form.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(225, 213, 202), 1, true),
                new EmptyBorder(18, 18, 18, 18)
        ));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0, 0, 0, 12);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridy = 0;

        addFormField(form, c, 0, "Date", dateField);
        addFormField(form, c, 1, "Time", timeBox);
        addFormField(form, c, 2, "Guests", guestsSpinner);
        addFormField(form, c, 3, "Special request", requestField);

        return form;
    }

    private void addFormField(
            JPanel panel,
            GridBagConstraints c,
            int x,
            String label,
            JComponent component
    ) {
        JPanel fieldPanel = new JPanel(new BorderLayout(0, 5));
        fieldPanel.setOpaque(false);

        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        fieldLabel.setForeground(new Color(100, 80, 65));

        component.setPreferredSize(new Dimension(x == 3 ? 350 : 110, 35));

        fieldPanel.add(fieldLabel, BorderLayout.NORTH);
        fieldPanel.add(component, BorderLayout.CENTER);

        c.gridx = x;
        c.weightx = x == 3 ? 1 : 0;

        panel.add(fieldPanel, c);
    }

    private void rebuildReservationGrid() {
        reservationGrid.removeAll();

        if (availableTables.isEmpty()) {
            reservationGrid.add(new JLabel("No tables are available."));
        }

        for (RestaurantTable table : availableTables) {
            JButton tableButton = new JButton(
                    "<html><div style='text-align:center;'>"
                            + "<b>Table " + table.getTableId() + "</b><br>"
                            + table.getCapacity() + " guests · "
                            + table.getLocation()
                            + "<br><font color='#2F8A55'>Available</font>"
                            + "</div></html>"
            );

            tableButton.setBackground(CARD);
            tableButton.setForeground(BROWN);
            tableButton.setFont(new Font("Arial", Font.PLAIN, 13));
            tableButton.setPreferredSize(new Dimension(180, 90));
            tableButton.setFocusPainted(false);

            tableButton.setBorder(
                    new LineBorder(
                            table.equals(selectedReservationTable)
                                    ? GREEN
                                    : new Color(225, 213, 202),
                            table.equals(selectedReservationTable) ? 2 : 1,
                            true
                    )
            );

            tableButton.addActionListener(e -> {
                selectedReservationTable = table;
                rebuildReservationGrid();
            });

            reservationGrid.add(tableButton);
        }

        reservationGrid.revalidate();
        reservationGrid.repaint();
    }

    private JComponent createOrdersPage() {
        JPanel page = createPagePanel();

        page.add(sectionTitle("My Orders"));
        page.add(Box.createVerticalStrut(15));

        ordersPanel.setOpaque(false);
        ordersPanel.setLayout(new BoxLayout(ordersPanel, BoxLayout.Y_AXIS));

        page.add(ordersPanel);
        refreshOrdersPanel();

        return wrapInScrollPane(page);
    }

    private JComponent createReservationsPage() {
        JPanel page = createPagePanel();

        page.add(sectionTitle("My Reservations"));
        page.add(Box.createVerticalStrut(15));

        reservationsPanel.setOpaque(false);
        reservationsPanel.setLayout(
                new BoxLayout(reservationsPanel, BoxLayout.Y_AXIS)
        );

        page.add(reservationsPanel);
        refreshReservationsPanel();

        return wrapInScrollPane(page);
    }

    private JPanel createPagePanel() {
        JPanel page = new JPanel();
        page.setBackground(BG);
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBorder(new EmptyBorder(25, 45, 25, 45));
        return page;
    }

    private JScrollPane wrapInScrollPane(JPanel panel) {
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(BG);
        return scrollPane;
    }

    private JLabel sectionTitle(String text) {
        JLabel title = new JLabel(text);
        title.setFont(new Font("Georgia", Font.BOLD, 24));
        title.setForeground(BROWN);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        return title;
    }

    private JLabel createFooter() {
        JLabel footer = new JLabel(
                "Maison Ember · 12 MG Road, Kolhapur · "
                        + "+91 98765 43210 · Open daily 12:00 – 22:00"
        );

        footer.setFont(new Font("Arial", Font.PLAIN, 12));
        footer.setForeground(new Color(105, 80, 55));
        footer.setBorder(new EmptyBorder(12, 40, 12, 40));
        footer.setBackground(CARD);
        footer.setOpaque(true);

        return footer;
    }

    private void addToCart(MenuItem item, RoundedButton button) {
        cart.put(item, cart.getOrDefault(item, 0) + 1);

        updateCartButton();
        refreshCartPage();

        Color originalColor = button.getBackground();

        button.setText("Added ✓");
        button.setBackground(TOAST_GREEN);

        Timer timer = new Timer(900, e -> {
            button.setText("Add to Cart");
            button.setBackground(originalColor);
            ((Timer) e.getSource()).stop();
        });

        timer.start();
        showAddedToast();
    }

    private void showAddedToast() {
        JWindow toast = new JWindow(this);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(35, 35, 38));

        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, TOAST_GREEN),
                new EmptyBorder(13, 16, 13, 16)
        ));

        JLabel message = new JLabel("Added to cart");
        message.setFont(new Font("Arial", Font.BOLD, 13));
        message.setForeground(Color.WHITE);

        panel.add(message, BorderLayout.CENTER);

        toast.add(panel);
        toast.pack();

        Point location = getLocationOnScreen();

        toast.setLocation(
                location.x + getWidth() - toast.getWidth() - 35,
                location.y + getHeight() - toast.getHeight() - 70
        );

        toast.setAlwaysOnTop(true);
        toast.setVisible(true);

        Timer timer = new Timer(1800, e -> {
            toast.dispose();
            ((Timer) e.getSource()).stop();
        });

        timer.start();
    }

    private void updateCartButton() {
        int count = cart.values().stream()
                .mapToInt(Integer::intValue)
                .sum();

        cartButton.setText("🛒 Cart (" + count + ")");
    }

    private void refreshCartPage() {
        cartRowsPanel.removeAll();

        if (cart.isEmpty()) {
            JLabel empty = new JLabel(
                    "Your cart is empty. Add menu items to continue."
            );

            empty.setForeground(new Color(120, 100, 85));
            cartRowsPanel.add(empty);

        } else {
            for (Map.Entry<MenuItem, Integer> entry : cart.entrySet()) {
                cartRowsPanel.add(
                        createCartRow(entry.getKey(), entry.getValue())
                );

                cartRowsPanel.add(Box.createVerticalStrut(8));
            }
        }

        BigDecimal total = getCartTotal();

        subtotalLabel.setText(
                "₹" + total.stripTrailingZeros().toPlainString()
        );

        totalLabel.setText(
                "₹" + total.stripTrailingZeros().toPlainString()
        );

        cartRowsPanel.revalidate();
        cartRowsPanel.repaint();
    }

    private JPanel createCartRow(MenuItem item, int quantity) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(CARD);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

        row.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(225, 213, 202), 1, true),
                new EmptyBorder(9, 12, 9, 12)
        ));

        JLabel details = new JLabel(
                "<html><b>" + getFoodEmoji(item.getCategory()) + " "
                        + item.getItemName() + "</b><br>"
                        + "<span style='color:#7B6B60;'>₹"
                        + item.getPrice().stripTrailingZeros().toPlainString()
                        + " each</span></html>"
        );

        details.setFont(new Font("Arial", Font.PLAIN, 13));
        details.setForeground(BROWN);

        RoundedButton minus = createCartControl(
                "−",
                new Color(245, 241, 236)
        );

        RoundedButton plus = createCartControl(
                "+",
                new Color(245, 241, 236)
        );

        RoundedButton remove = createCartControl("×", RED);

        minus.setForeground(BROWN);
        plus.setForeground(BROWN);
        remove.setForeground(Color.WHITE);

        minus.addActionListener(e -> changeQuantity(item, -1));
        plus.addActionListener(e -> changeQuantity(item, 1));

        remove.addActionListener(e -> {
            cart.remove(item);
            updateCartButton();
            refreshCartPage();
        });

        JLabel quantityLabel = new JLabel(String.valueOf(quantity));
        quantityLabel.setFont(new Font("Arial", Font.BOLD, 13));
        quantityLabel.setHorizontalAlignment(SwingConstants.CENTER);

        BigDecimal amount = item.getPrice()
                .multiply(BigDecimal.valueOf(quantity));

        JLabel amountLabel = new JLabel(
                "₹" + amount.stripTrailingZeros().toPlainString()
        );

        amountLabel.setFont(new Font("Arial", Font.BOLD, 14));
        amountLabel.setForeground(BROWN);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setOpaque(false);

        controls.add(minus);
        controls.add(quantityLabel);
        controls.add(plus);
        controls.add(amountLabel);
        controls.add(remove);

        row.add(details, BorderLayout.CENTER);
        row.add(controls, BorderLayout.EAST);

        return row;
    }

    private RoundedButton createCartControl(String text, Color background) {
        RoundedButton button = new RoundedButton(text);

        button.setBackground(background);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(30, 30));
        button.setMinimumSize(new Dimension(30, 30));
        button.setMaximumSize(new Dimension(30, 30));

        return button;
    }

    private void changeQuantity(MenuItem item, int change) {
        int quantity = cart.getOrDefault(item, 0) + change;

        if (quantity <= 0) {
            cart.remove(item);
        } else {
            cart.put(item, quantity);
        }

        updateCartButton();
        refreshCartPage();
    }

    private BigDecimal getCartTotal() {
        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<MenuItem, Integer> entry : cart.entrySet()) {
            BigDecimal itemTotal = entry.getKey().getPrice().multiply(
                    BigDecimal.valueOf(entry.getValue())
            );

            total = total.add(itemTotal);
        }

        return total;
    }

    private void placeOrder(String orderType) {
        if (customerId == -1) {
            showInfo("Account Error", "Customer account was not found.");
            return;
        }

        if (cart.isEmpty()) {
            showInfo("Cart Empty", "Add menu items before placing an order.");
            return;
        }

        Integer selectedTableId = null;

        if ("Dine-in".equals(orderType)) {
            RestaurantTable selectedOrderTable =
                    (RestaurantTable) orderTableBox.getSelectedItem();

            if (selectedOrderTable == null) {
                showInfo(
                        "Select Table",
                        "Select an available dine-in table first."
                );

                return;
            }

            selectedTableId = selectedOrderTable.getTableId();
        }

        Map<Integer, Integer> databaseCart = new LinkedHashMap<>();

        for (Map.Entry<MenuItem, Integer> entry : cart.entrySet()) {
            databaseCart.put(
                    entry.getKey().getItemId(),
                    entry.getValue()
            );
        }

        try {
            int orderId = orderDAO.createOrder(
                    customerId,
                    selectedTableId,
                    databaseCart
            );

            if (selectedTableId != null) {
                tableDAO.updateTableStatus(selectedTableId, "OCCUPIED");
            }

            cart.clear();
            updateCartButton();
            refreshCartPage();
            reloadAvailableTables();

            showInfo(
                    "Order Placed",
                    "Order #" + orderId
                            + " has been sent to Staff and Admin.\n"
                            + "You may order extra food later."
            );

            refreshOrdersPanel();
            showPage("ORDERS");

        } catch (SQLException exception) {
            showInfo(
                    "Order Error",
                    "Could not place order.\n"
                            + exception.getMessage()
            );
        }
    }

    private void refreshOrdersPanel() {
        ordersPanel.removeAll();

        if (customerId == -1) {
            ordersPanel.add(new JLabel("Customer account was not found."));
            return;
        }

        try {
            List<OrderDAO.OrderDetails> orders =
                    orderDAO.getOrdersByCustomerId(customerId);

            if (orders.isEmpty()) {
                ordersPanel.add(new JLabel("No orders placed yet."));
            }

            for (OrderDAO.OrderDetails order : orders) {
                ordersPanel.add(createOrderCard(order));
                ordersPanel.add(Box.createVerticalStrut(12));
            }

        } catch (SQLException exception) {
            ordersPanel.add(new JLabel(
                    "Could not load orders: " + exception.getMessage()
            ));
        }

        ordersPanel.revalidate();
        ordersPanel.repaint();
    }

    private JPanel createOrderCard(OrderDAO.OrderDetails order) {
        JPanel card = new JPanel(new BorderLayout(15, 8));
        card.setBackground(CARD);

        card.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 130)
        );

        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(225, 213, 202), 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));

        String tableText = order.getTableId() == null
                ? "Takeaway"
                : "Table " + order.getTableId()
                + " · " + order.getTableLocation();

        JLabel details = new JLabel(
                "<html><b>Order #" + order.getOrderId() + "</b><br>"
                        + "<span style='color:#7B6B60;'>"
                        + tableText + "<br>"
                        + order.getOrderItems() + "<br>"
                        + "Staff: " + order.getStaffName()
                        + "</span></html>"
        );

        details.setFont(new Font("Arial", Font.PLAIN, 13));
        details.setForeground(BROWN);

        JPanel rightPanel = new JPanel();
        rightPanel.setOpaque(false);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        JLabel amount = new JLabel(
                "₹" + order.getTotalAmount()
                        .stripTrailingZeros()
                        .toPlainString()
        );

        amount.setFont(new Font("Arial", Font.BOLD, 15));
        amount.setForeground(GOLD);
        amount.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JLabel status = new JLabel(order.getStatus());
        status.setFont(new Font("Arial", Font.BOLD, 12));
        status.setForeground(getStatusColor(order.getStatus()));
        status.setAlignmentX(Component.RIGHT_ALIGNMENT);

        rightPanel.add(amount);
        rightPanel.add(Box.createVerticalStrut(6));
        rightPanel.add(status);

        try {
            String paymentStatus =
                    paymentDAO.getPaymentStatus(order.getOrderId());

            if ("PAID".equals(paymentStatus)) {
                JLabel paid = new JLabel("PAID ✓");
                paid.setFont(new Font("Arial", Font.BOLD, 12));
                paid.setForeground(GREEN);
                paid.setAlignmentX(Component.RIGHT_ALIGNMENT);

                rightPanel.add(Box.createVerticalStrut(6));
                rightPanel.add(paid);

            } else if (
                    "SERVED".equals(order.getStatus())
                            || "AWAITING_PAYMENT".equals(order.getStatus())
            ) {
                RoundedButton payButton =
                        new RoundedButton("Pay Bill");

                payButton.setBackground(GREEN);
                payButton.setForeground(Color.WHITE);
                payButton.setFont(new Font("Arial", Font.BOLD, 11));
                payButton.setMaximumSize(new Dimension(105, 31));
                payButton.setAlignmentX(Component.RIGHT_ALIGNMENT);

                payButton.addActionListener(e -> payOrder(order));
                rightPanel.add(Box.createVerticalStrut(8));
                rightPanel.add(payButton);
            }

        } catch (SQLException ignored) {
            // Payment status will reload next time customer opens My Orders.
        }

        card.add(details, BorderLayout.CENTER);
        card.add(rightPanel, BorderLayout.EAST);

        return card;
    }

    private void payOrder(OrderDAO.OrderDetails order) {
        try {
            BigDecimal amount = paymentDAO.createPendingPayment(
                    order.getOrderId(),
                    "UPI"
            );

            String transactionReference = JOptionPane.showInputDialog(
                    this,
                    "UPI payment amount: ₹"
                            + amount.stripTrailingZeros().toPlainString()
                            + "\n\nFor testing, enter the transaction ID "
                            + "after completing payment:",
                    "Complete Payment",
                    JOptionPane.PLAIN_MESSAGE
            );

            if (transactionReference == null
                    || transactionReference.trim().isEmpty()) {

                showInfo(
                        "Payment Pending",
                        "Payment is pending. Enter a transaction ID "
                                + "when payment is completed."
                );

                refreshOrdersPanel();
                return;
            }

            paymentDAO.markPaymentAsPaid(
                    order.getOrderId(),
                    transactionReference.trim()
            );

            if (order.getTableId() != null) {
                tableDAO.updateTableStatus(
                        order.getTableId(),
                        "AVAILABLE"
                );
            }

            reloadAvailableTables();

            showInfo(
                    "Payment Successful",
                    "Your bill is paid successfully.\n"
                            + "Order #" + order.getOrderId()
                            + " is now completed."
            );

            refreshOrdersPanel();

        } catch (SQLException exception) {
            showInfo(
                    "Payment Error",
                    "Could not complete payment.\n"
                            + exception.getMessage()
            );
        }
    }

    private void confirmReservation() {
        if (customerId == -1) {
            showInfo("Account Error", "Customer account was not found.");
            return;
        }

        if (selectedReservationTable == null) {
            showInfo(
                    "Select Table",
                    "Please select an available table first."
            );

            return;
        }

        try {
            LocalDate reservationDate =
                    LocalDate.parse(dateField.getText().trim());

            LocalTime reservationTime =
                    LocalTime.parse(
                            timeBox.getSelectedItem() + ":00"
                    );

            int guests = (Integer) guestsSpinner.getValue();

            int reservationId = reservationDAO.createReservation(
                    customerId,
                    selectedReservationTable.getTableId(),
                    reservationDate,
                    reservationTime,
                    guests,
                    120,
                    requestField.getText().trim()
            );

            selectedReservationTable = null;
            requestField.setText("");

            reloadAvailableTables();
            refreshReservationsPanel();

            showInfo(
                    "Reservation Confirmed",
                    "Reservation #" + reservationId
                            + " has been created successfully."
            );

            showPage("RESERVATIONS");

        } catch (Exception exception) {
            showInfo(
                    "Reservation Error",
                    "Could not create reservation.\n"
                            + exception.getMessage()
            );
        }
    }

    private void refreshReservationsPanel() {
        reservationsPanel.removeAll();

        if (customerId == -1) {
            reservationsPanel.add(new JLabel("Customer account was not found."));
            return;
        }

        try {
            List<Reservation> reservations =
                    reservationDAO.getReservationsByCustomerId(customerId);

            if (reservations.isEmpty()) {
                reservationsPanel.add(
                        new JLabel("You have no reservations yet.")
                );
            }

            for (Reservation reservation : reservations) {
                reservationsPanel.add(
                        createReservationCard(reservation)
                );

                reservationsPanel.add(Box.createVerticalStrut(10));
            }

        } catch (SQLException exception) {
            reservationsPanel.add(new JLabel(
                    "Could not load reservations: "
                            + exception.getMessage()
            ));
        }

        reservationsPanel.revalidate();
        reservationsPanel.repaint();
    }

    private JPanel createReservationCard(Reservation reservation) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD);

        card.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 85)
        );

        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(225, 213, 202), 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));

        JLabel details = new JLabel(
                "<html><b>Reservation #"
                        + reservation.getReservationId()
                        + "</b><br>"
                        + "<span style='color:#7B6B60;'>"
                        + reservation.getReservationDate()
                        + " at " + reservation.getReservationTime()
                        + " · Table " + reservation.getTableId()
                        + " · " + reservation.getTableLocation()
                        + " · " + reservation.getNumberOfGuests()
                        + " guests</span></html>"
        );

        details.setFont(new Font("Arial", Font.PLAIN, 13));
        details.setForeground(BROWN);

        JLabel status = new JLabel("CONFIRMED");
        status.setForeground(GREEN);
        status.setFont(new Font("Arial", Font.BOLD, 12));

        card.add(details, BorderLayout.CENTER);
        card.add(status, BorderLayout.EAST);

        return card;
    }

    private void reloadAvailableTables() {
        try {
            availableTables = tableDAO.getAvailableTables();
            refreshOrderTableBox();
            rebuildReservationGrid();

        } catch (SQLException exception) {
            showInfo(
                    "Database Error",
                    "Could not load restaurant tables.\n"
                            + exception.getMessage()
            );
        }
    }

    private void refreshOrderTableBox() {
        DefaultComboBoxModel<RestaurantTable> model =
                new DefaultComboBoxModel<>();

        for (RestaurantTable table : availableTables) {
            model.addElement(table);
        }

        orderTableBox.setModel(model);
    }

    private Color getStatusColor(String status) {
        return switch (status) {
            case "PENDING" -> RED;
            case "CONFIRMED" -> GOLD;
            case "PREPARING" -> BLUE;
            case "READY" -> GREEN;
            case "SERVED" -> GREEN;
            case "AWAITING_PAYMENT" -> GOLD;
            case "COMPLETED" -> GREEN;
            default -> BROWN;
        };
    }
    private String getFoodEmoji(String category) {
        if (category == null) {
            return "🍽";
        }

        return switch (category.trim().toUpperCase()) {
            case "STARTER", "STARTERS" -> "🍢";
            case "MAIN COURSE", "MAINCOURSE" -> "🍛";
            case "PIZZA" -> "🍕";
            case "BURGER", "BURGERS" -> "🍔";
            case "PASTA" -> "🍝";
            case "DESSERT", "DESSERTS" -> "🍰";
            case "BEVERAGE", "BEVERAGES", "DRINKS" -> "🥤";
            default -> "🍽";
        };
    }

    private void showPage(String page) {
        cardLayout.show(pageContainer, page);
    }

    private void showInfo(String title, String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                title,
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color panelColor;

        RoundedPanel(int radius, Color panelColor) {
            this.radius = radius;
            this.panelColor = panelColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );
            g2.setColor(panelColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.setColor(new Color(225, 213, 202));
            g2.drawRoundRect(
                    0, 0, getWidth() - 1, getHeight() - 1, radius, radius
            );
            g2.dispose();
            super.paintComponent(graphics);
        }
    }

    static class RoundedButton extends JButton {

        RoundedButton(String text) {
            super(text);
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
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
                            ? getBackground().darker()
                            : getBackground()
            );

            g2.fillRoundRect(
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    16,
                    16
            );

            g2.dispose();
            super.paintComponent(graphics);
        }
    }
}

