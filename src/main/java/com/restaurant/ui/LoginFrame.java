package com.restaurant.ui;

import com.restaurant.dao.UserAccountDAO;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;

public class LoginFrame extends JFrame {

    private final JTextField emailField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();

    public LoginFrame() {
        setTitle("Restaurant Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel mainPanel = new JPanel(new GridLayout(1, 2));
        mainPanel.add(createLoginPanel());
        mainPanel.add(new ImagePanel());

        add(mainPanel);
    }

    private JPanel createLoginPanel() {
        JPanel outerPanel = new FoodBackgroundPanel();
        outerPanel.setLayout(new GridBagLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setPreferredSize(new Dimension(500, 490));
        formPanel.setBorder(new EmptyBorder(35, 55, 35, 55));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        JLabel logo = new JLabel("Restaurant Management System");
        logo.setFont(new Font("Arial", Font.BOLD, 20));
        logo.setForeground(new Color(170, 10, 55));

        c.gridy = 0;
        c.insets = new Insets(0, 0, 42, 0);
        formPanel.add(logo, c);

        JLabel heading = new JLabel("Welcome Back!");
        heading.setFont(new Font("Arial", Font.BOLD, 28));
        heading.setForeground(new Color(45, 45, 45));

        c.gridy = 1;
        c.insets = new Insets(0, 0, 8, 0);
        formPanel.add(heading, c);

        JLabel subtitle = new JLabel("Sign in to manage your restaurant.");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitle.setForeground(new Color(110, 110, 110));

        c.gridy = 2;
        c.insets = new Insets(0, 0, 25, 0);
        formPanel.add(subtitle, c);

        c.gridy = 3;
        c.insets = new Insets(0, 0, 15, 0);
        formPanel.add(createInputPanel("Email address", emailField), c);

        c.gridy = 4;
        c.insets = new Insets(0, 0, 12, 0);
        formPanel.add(createInputPanel("Password", passwordField), c);

        JCheckBox rememberCheck = new JCheckBox("Remember me");
        rememberCheck.setBackground(Color.WHITE);
        rememberCheck.setFont(new Font("Arial", Font.PLAIN, 14));

        c.gridy = 5;
        c.insets = new Insets(0, 0, 25, 0);
        formPanel.add(rememberCheck, c);

        RoundedButton loginButton = new RoundedButton("Login");
        loginButton.setBackground(new Color(170, 10, 55));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Arial", Font.BOLD, 15));
        loginButton.setPreferredSize(new Dimension(0, 46));
        loginButton.addActionListener(e -> login());

        c.gridy = 6;
        c.insets = new Insets(0, 0, 18, 0);
        formPanel.add(loginButton, c);

        JButton registerButton = new JButton(
                "<html>Don't have an account? " +
                        "<font color='#AA0A37'><u>Register</u></font></html>"
        );

        registerButton.setFont(new Font("Arial", Font.BOLD, 14));
        registerButton.setBorderPainted(false);
        registerButton.setContentAreaFilled(false);
        registerButton.setFocusPainted(false);
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        registerButton.addActionListener(e -> {
            dispose();
            new CustomerRegistrationFrame().setVisible(true);
        });

        c.gridy = 7;
        c.insets = new Insets(0, 0, 0, 0);
        c.anchor = GridBagConstraints.CENTER;
        formPanel.add(registerButton, c);

        outerPanel.add(formPanel);
        return outerPanel;
    }

    private JPanel createInputPanel(String labelText, JTextField field) {
        JPanel inputPanel = new JPanel(new BorderLayout(0, 6));
        inputPanel.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 13));
        label.setForeground(new Color(55, 55, 55));

        field.setFont(new Font("Arial", Font.PLAIN, 15));
        field.setPreferredSize(new Dimension(0, 44));
        field.setMargin(new Insets(0, 15, 0, 15));
        field.setBorder(new RoundedBorder(new Color(220, 220, 220), 16));
        field.setBackground(new Color(252, 252, 252));

        inputPanel.add(label, BorderLayout.NORTH);
        inputPanel.add(field, BorderLayout.CENTER);

        return inputPanel;
    }

    private void login() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            showStyledMessage(
                    "Missing Information",
                    "Please enter both email and password.",
                    true
            );
            return;
        }

        try {
            UserAccountDAO userAccountDAO = new UserAccountDAO();
            String role = userAccountDAO.authenticate(email, password);

            if (role == null) {
                showStyledMessage(
                        "Login Failed",
                        "Incorrect email or password.",
                        true
                );
                return;
            }

            dispose();
            openDashboard(role, email);

        } catch (SQLException exception) {
            showStyledMessage(
                    "Database Error",
                    "Could not connect to the database.",
                    true
            );
        }
    }

    private void openDashboard(String role, String email) {
        switch (role) {
            case "CUSTOMER" ->
                    new customerDashboard(email).setVisible(true);

            case "STAFF" ->
                    new staffDashboard(email).setVisible(true);

            case "ADMIN" ->
                    new AdminDashboard(email).setVisible(true);

            default ->
                    showStyledMessage(
                            "Account Error",
                            "This account has an unknown role.",
                            true
                    );
        }
    }

    private void showStyledMessage(
            String title,
            String message,
            boolean isError
    ) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setUndecorated(true);

        Color themeColor = isError
                ? new Color(190, 30, 65)
                : new Color(35, 140, 85);

        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(22, 25, 22, 25));

        JLabel icon = new JLabel(isError ? "!" : "✓");
        icon.setFont(new Font("Arial", Font.BOLD, 24));
        icon.setForeground(Color.WHITE);
        icon.setHorizontalAlignment(SwingConstants.CENTER);
        icon.setPreferredSize(new Dimension(45, 45));
        icon.setOpaque(true);
        icon.setBackground(themeColor);

        JLabel text = new JLabel(
                "<html><b>" + title + "</b><br><br>" + message + "</html>"
        );
        text.setFont(new Font("Arial", Font.PLAIN, 14));

        RoundedButton okButton = new RoundedButton("OK");
        okButton.setBackground(themeColor);
        okButton.setForeground(Color.WHITE);
        okButton.setPreferredSize(new Dimension(85, 36));
        okButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(okButton);

        JPanel content = new JPanel(new BorderLayout(0, 15));
        content.setBackground(Color.WHITE);
        content.add(panel, BorderLayout.CENTER);
        content.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(icon, BorderLayout.WEST);
        panel.add(text, BorderLayout.CENTER);

        dialog.setContentPane(content);
        dialog.getRootPane().setBorder(
                new RoundedBorder(themeColor, 18)
        );

        dialog.setSize(410, 175);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    static class RoundedBorder extends AbstractBorder {
        private final Color color;
        private final int radius;

        RoundedBorder(Color color, int radius) {
            this.color = color;
            this.radius = radius;
        }

        @Override
        public Insets getBorderInsets(Component component) {
            return new Insets(8, 12, 8, 12);
        }

        @Override
        public void paintBorder(
                Component component,
                Graphics graphics,
                int x,
                int y,
                int width,
                int height
        ) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(color);
            g2.drawRoundRect(
                    x,
                    y,
                    width - 1,
                    height - 1,
                    radius,
                    radius
            );

            g2.dispose();
        }
    }

    static class RoundedButton extends JButton {

        RoundedButton(String text) {
            super(text);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            Color buttonColor = getModel().isPressed()
                    ? getBackground().darker()
                    : getBackground();

            g2.setColor(buttonColor);
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

    static class FoodBackgroundPanel extends JPanel {
        private final Image sticker;

        FoodBackgroundPanel() {
            setBackground(Color.WHITE);
            sticker = new ImageIcon("images/food-sticker.png").getImage();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);

            if (sticker.getWidth(this) <= 0) {
                return;
            }

            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setComposite(
                    AlphaComposite.getInstance(
                            AlphaComposite.SRC_OVER,
                            0.18f
                    )
            );

            int size = 210;
            int x = getWidth() - size - 25;
            int y = getHeight() - size - 25;

            g2.drawImage(sticker, x, y, size, size, this);
            g2.dispose();
        }
    }

    static class ImagePanel extends JPanel {
        private final Image image;

        ImagePanel() {
            setBackground(new Color(28, 12, 8));
            image = new ImageIcon("images/login.jpg").getImage();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);

            int imageWidth = image.getWidth(this);
            int imageHeight = image.getHeight(this);

            if (imageWidth <= 0 || imageHeight <= 0) {
                return;
            }

            double scale = Math.max(
                    (double) getWidth() / imageWidth,
                    (double) getHeight() / imageHeight
            );

            int newWidth = (int) (imageWidth * scale);
            int newHeight = (int) (imageHeight * scale);

            int x = (getWidth() - newWidth) / 2;
            int y = (getHeight() - newHeight) / 2;

            graphics.drawImage(image, x, y, newWidth, newHeight, this);
        }
    }
}