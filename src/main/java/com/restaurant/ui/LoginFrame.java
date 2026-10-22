package com.restaurant.ui;

import com.restaurant.dao.UserAccountDAO;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

public class LoginFrame extends JFrame {

    private final JTextField emailField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();

    public LoginFrame() {
        setTitle(" Restaurant Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(900, 650));

        BackgroundImagePanel background = new BackgroundImagePanel();
        background.setLayout(new GridBagLayout());

        // Center the glass-style login card over the full-screen food image.
        background.add(createLoginPanel());

        setContentPane(background);
    }

    private JPanel createLoginPanel() {
        GlassPanel formPanel = new GlassPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setPreferredSize(new Dimension(500, 650));
        formPanel.setBorder(new EmptyBorder(30, 42, 30, 42));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;

        JLabel logo = new JLabel("VALORA", SwingConstants.CENTER);
        logo.setFont(new Font("Serif", Font.BOLD, 31));
        logo.setForeground(new Color(242, 193, 105));

        c.gridy = 0;
        c.insets = new Insets(0, 0, 8, 0);
        formPanel.add(logo, c);

        JLabel tagline = new JLabel(
                "RESTAURANT MANAGEMENT SYSTEM",
                SwingConstants.CENTER
        );
        tagline.setFont(new Font("SansSerif", Font.PLAIN, 11));
        tagline.setForeground(new Color(224, 194, 143));

        c.gridy = 1;
        c.insets = new Insets(0, 0, 30, 0);
        formPanel.add(tagline, c);

        JLabel heading = new JLabel("Welcome Back!", SwingConstants.CENTER);
        heading.setFont(new Font("Serif", Font.BOLD, 30));
        heading.setForeground(Color.WHITE);

        c.gridy = 2;
        c.insets = new Insets(0, 0, 8, 0);
        formPanel.add(heading, c);

        JLabel subtitle = new JLabel(
                "Sign in to manage your restaurant.",
                SwingConstants.CENTER
        );
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(new Color(235, 235, 235));

        c.gridy = 3;
        c.insets = new Insets(0, 0, 25, 0);
        formPanel.add(subtitle, c);

        c.gridy = 4;
        c.insets = new Insets(0, 0, 15, 0);
        formPanel.add(createInputPanel("Email address", emailField), c);

        c.gridy = 5;
        c.insets = new Insets(0, 0, 12, 0);
        formPanel.add(createInputPanel("Password", passwordField), c);

        passwordField.addActionListener(e -> login());

        JCheckBox rememberCheck = new JCheckBox("Remember me");
        rememberCheck.setOpaque(false);
        rememberCheck.setForeground(Color.WHITE);
        rememberCheck.setFont(new Font("SansSerif", Font.PLAIN, 13));
        rememberCheck.setFocusPainted(false);

        c.gridy = 6;
        c.insets = new Insets(0, 0, 22, 0);
        formPanel.add(rememberCheck, c);

        RoundedButton loginButton = new RoundedButton("Login   →");
        loginButton.setBackground(new Color(232, 183, 99));
        loginButton.setForeground(new Color(35, 24, 15));
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 17));
        loginButton.setPreferredSize(new Dimension(0, 50));
        loginButton.addActionListener(e -> login());

        c.gridy = 7;
        c.insets = new Insets(0, 0, 22, 0);
        formPanel.add(loginButton, c);

        JButton registerButton = new JButton(
                "<html><font color='#EEEEEE'>Don't have an account? </font>"
                        + "<font color='#F2C169'><u>Register</u></font></html>"
        );
        registerButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        registerButton.setBorderPainted(false);
        registerButton.setContentAreaFilled(false);
        registerButton.setFocusPainted(false);
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        registerButton.addActionListener(e -> {
            dispose();
            new CustomerRegistrationFrame().setVisible(true);
        });

        c.gridy = 8;
        c.insets = new Insets(0, 0, 0, 0);
        formPanel.add(registerButton, c);

        return formPanel;
    }

    private JPanel createInputPanel(String labelText, JTextField field) {
        JPanel inputPanel = new JPanel(new BorderLayout(0, 8));
        inputPanel.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setForeground(new Color(245, 235, 220));

        field.setFont(new Font("SansSerif", Font.PLAIN, 15));
        field.setPreferredSize(new Dimension(0, 46));
        field.setMargin(new Insets(0, 15, 0, 15));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setBackground(new Color(35, 35, 35, 180));
        field.setOpaque(false);
        field.setBorder(new RoundedBorder(new Color(210, 195, 175, 190), 20));

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

            openDashboard(role, email);

        } catch (SQLException exception) {
            exception.printStackTrace();

            showStyledMessage(
                    "Database Error",
                    "Could not connect to database: " + exception.getMessage(),
                    true
            );
        }
    }

    private void openDashboard(String role, String email) {
        role = role.trim().toUpperCase();

        switch (role) {
            case "CUSTOMER" -> {
                dispose();
                SwingUtilities.invokeLater(() -> {
                    customerDashboard dashboard = new customerDashboard(email);
                    dashboard.setVisible(true);
                });
            }

            case "STAFF" -> {
                dispose();
                SwingUtilities.invokeLater(() -> {
                    staffDashboard dashboard = new staffDashboard(email);
                    dashboard.setVisible(true);
                });
            }

            case "ADMIN" -> {
                dispose();
                SwingUtilities.invokeLater(() -> {
                    AdminDashboard dashboard = new AdminDashboard(email);
                    dashboard.setVisible(true);
                });
            }

            default -> showStyledMessage(
                    "Account Error",
                    "Unknown account role: " + role,
                    true
            );
        }
    }

    private void showStyledMessage(String title, String message, boolean isError) {
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

        panel.add(icon, BorderLayout.WEST);
        panel.add(text, BorderLayout.CENTER);

        JPanel content = new JPanel(new BorderLayout(0, 15));
        content.setBackground(Color.WHITE);
        content.add(panel, BorderLayout.CENTER);
        content.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setContentPane(content);
        dialog.getRootPane().setBorder(new RoundedBorder(themeColor, 18));
        dialog.setSize(430, 180);
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
                    x, y,
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
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
            g2.dispose();

            super.paintComponent(graphics);
        }
    }

    static class GlassPanel extends JPanel {
        GlassPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            int arc = 35;
            g2.setColor(new Color(15, 15, 15, 185));
            g2.fillRoundRect(
                    0, 0,
                    getWidth() - 1,
                    getHeight() - 1,
                    arc, arc
            );

            g2.setColor(new Color(255, 255, 255, 100));
            g2.setStroke(new BasicStroke(1.3f));
            g2.drawRoundRect(
                    1, 1,
                    getWidth() - 3,
                    getHeight() - 3,
                    arc, arc
            );

            g2.dispose();
            super.paintComponent(graphics);
        }
    }

    static class BackgroundImagePanel extends JPanel {
        private final Image backgroundImage;

        BackgroundImagePanel() {
            setBackground(new Color(20, 15, 10));
            backgroundImage = new ImageIcon("images/img2.jpg").getImage();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);

            int panelWidth = getWidth();
            int panelHeight = getHeight();
            int imageWidth = backgroundImage.getWidth(this);
            int imageHeight = backgroundImage.getHeight(this);

            if (imageWidth <= 0 || imageHeight <= 0) {
                return;
            }

            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR
            );

            double scale = Math.max(
                    (double) panelWidth / imageWidth,
                    (double) panelHeight / imageHeight
            );

            int scaledWidth = (int) (imageWidth * scale);
            int scaledHeight = (int) (imageHeight * scale);
            int x = (panelWidth - scaledWidth) / 2;
            int y = (panelHeight - scaledHeight) / 2;

            g2.drawImage(
                    backgroundImage,
                    x, y,
                    scaledWidth, scaledHeight,
                    this
            );

            // Subtle dark overlay makes the centered glass card readable.
            g2.setColor(new Color(0, 0, 0, 35));
            g2.fillRect(0, 0, panelWidth, panelHeight);
            g2.dispose();
        }
    }
}
