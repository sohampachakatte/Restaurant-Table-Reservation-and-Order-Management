package com.restaurant.ui;

import com.restaurant.dao.CustomerRegistrationDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

public class CustomerRegistrationFrame extends JFrame {

    private final JTextField firstNameField = new JTextField();
    private final JTextField middleNameField = new JTextField();
    private final JTextField lastNameField = new JTextField();
    private final JTextField emailField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JPasswordField confirmPasswordField = new JPasswordField();

    private static final Color GOLD = new Color(242, 193, 105);
    private static final Color CREAM = new Color(245, 235, 220);
    private static final Color GLASS = new Color(15, 15, 15, 205);

    public CustomerRegistrationFrame() {

        setTitle("Maison Ember - Customer Registration");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1000, 750));

        // Full-screen background image
        ImagePanel background = new ImagePanel();
        background.setLayout(new GridBagLayout());

        // Position registration card on the LEFT
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(20, 60, 20, 20);

        background.add(createRegistrationPanel(), gbc);

        setContentPane(background);
        setLocationRelativeTo(null);
    }

    private JPanel createRegistrationPanel() {

        JPanel formPanel = new GlassPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setPreferredSize(new Dimension(480, 690));
        formPanel.setBorder(new EmptyBorder(25, 38, 25, 38));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridwidth = 1;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        // Branding
        JLabel logo = new JLabel("MAISON EMBER");
        logo.setFont(new Font("Serif", Font.BOLD, 27));
        logo.setForeground(GOLD);
        logo.setHorizontalAlignment(SwingConstants.CENTER);

        c.gridy = 0;
        c.insets = new Insets(0, 0, 5, 0);
        formPanel.add(logo, c);

        JLabel tagline = new JLabel(
                "RESTAURANT MANAGEMENT SYSTEM",
                SwingConstants.CENTER
        );
        tagline.setFont(new Font("SansSerif", Font.PLAIN, 10));
        tagline.setForeground(new Color(224, 194, 143));

        c.gridy = 1;
        c.insets = new Insets(0, 0, 20, 0);
        formPanel.add(tagline, c);

        // Heading
        JLabel heading = new JLabel(
                "Create Account",
                SwingConstants.CENTER
        );
        heading.setFont(new Font("Serif", Font.BOLD, 27));
        heading.setForeground(Color.WHITE);

        c.gridy = 2;
        c.insets = new Insets(0, 0, 5, 0);
        formPanel.add(heading, c);

        JLabel subtitle = new JLabel(
                "Join us to book tables and manage your orders.",
                SwingConstants.CENTER
        );
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(CREAM);

        c.gridy = 3;
        c.insets = new Insets(0, 0, 15, 0);
        formPanel.add(subtitle, c);

        // Registration fields
        addField(formPanel, c, 4, "First name", firstNameField);
        addField(formPanel, c, 5, "Middle name (optional)", middleNameField);
        addField(formPanel, c, 6, "Last name", lastNameField);
        addField(formPanel, c, 7, "Email address", emailField);
        addField(formPanel, c, 8, "Password", passwordField);
        addField(formPanel, c, 9, "Confirm password", confirmPasswordField);

        // Register button
        JButton registerButton = new RoundedButton("Create Account");

        registerButton.setFont(new Font("SansSerif", Font.BOLD, 15));
        registerButton.setBackground(GOLD);
        registerButton.setForeground(new Color(35, 24, 15));
        registerButton.setFocusPainted(false);
        registerButton.setPreferredSize(new Dimension(0, 44));
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        registerButton.addActionListener(e -> registerCustomer());

        c.gridy = 10;
        c.insets = new Insets(12, 0, 8, 0);
        formPanel.add(registerButton, c);

        // Back to login
        JButton backButton = new JButton(
                "<html><font color='#EEEEEE'>Already have an account? </font>"
                        + "<font color='#F2C169'><u>Login</u></font></html>"
        );

        backButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        backButton.setBorderPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setFocusPainted(false);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        backButton.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        c.gridy = 11;
        c.insets = new Insets(0, 0, 0, 0);
        c.anchor = GridBagConstraints.CENTER;
        formPanel.add(backButton, c);

        return formPanel;
    }

    private void addField(
            JPanel panel,
            GridBagConstraints c,
            int row,
            String labelText,
            JTextField field
    ) {

        JPanel inputPanel = new JPanel(new BorderLayout(0, 4));
        inputPanel.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setForeground(CREAM);

        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setBackground(new Color(35, 35, 35, 220));
        field.setPreferredSize(new Dimension(0, 35));
        field.setBorder(
                new RoundedBorder(new Color(190, 170, 140), 12)
        );

        inputPanel.add(label, BorderLayout.NORTH);
        inputPanel.add(field, BorderLayout.CENTER);

        c.gridy = row;
        c.insets = new Insets(0, 0, 9, 0);
        c.fill = GridBagConstraints.HORIZONTAL;

        panel.add(inputPanel, c);
    }

    private void registerCustomer() {

        String firstName = firstNameField.getText().trim();
        String middleName = middleNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();

        String password = new String(passwordField.getPassword());
        String confirmPassword =
                new String(confirmPasswordField.getPassword());

        if (firstName.isEmpty() || lastName.isEmpty()
                || email.isEmpty() || password.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please fill all required fields.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (!password.equals(confirmPassword)) {

            JOptionPane.showMessageDialog(
                    this,
                    "Passwords do not match.",
                    "Password Error",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {

            CustomerRegistrationDAO registrationDAO =
                    new CustomerRegistrationDAO();

            registrationDAO.registerCustomer(
                    firstName,
                    middleName,
                    lastName,
                    email,
                    password
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Customer account created successfully.",
                    "Registration Successful",
                    JOptionPane.INFORMATION_MESSAGE
            );

            dispose();
            new LoginFrame().setVisible(true);

        } catch (SQLException exception) {

            JOptionPane.showMessageDialog(
                    this,
                    "Registration failed: " + exception.getMessage(),
                    "Registration Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // Full-screen restaurant background
    static class ImagePanel extends JPanel {

        private final Image image;

        ImagePanel() {
            setBackground(new Color(28, 12, 8));
            image = new ImageIcon("images/img3.jpg").getImage();
        }

        @Override
        protected void paintComponent(Graphics g) {

            super.paintComponent(g);

            int imageWidth = image.getWidth(this);
            int imageHeight = image.getHeight(this);

            if (imageWidth <= 0 || imageHeight <= 0) {
                return;
            }

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR
            );

            double scale = Math.max(
                    (double) getWidth() / imageWidth,
                    (double) getHeight() / imageHeight
            );

            int newWidth = (int) (imageWidth * scale);
            int newHeight = (int) (imageHeight * scale);

            int x = (getWidth() - newWidth) / 2;
            int y = (getHeight() - newHeight) / 2;

            g2.drawImage(
                    image,
                    x, y,
                    newWidth, newHeight,
                    this
            );

            // Dark overlay for better contrast
            g2.setColor(new Color(0, 0, 0, 35));
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.dispose();
        }
    }

    // Translucent glass registration card
    static class GlassPanel extends JPanel {

        GlassPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(new Color(15, 15, 15, 205));

            g2.fillRoundRect(
                    0, 0,
                    getWidth() - 1,
                    getHeight() - 1,
                    28, 28
            );

            g2.setColor(new Color(255, 255, 255, 100));
            g2.setStroke(new BasicStroke(1.2f));

            g2.drawRoundRect(
                    1, 1,
                    getWidth() - 3,
                    getHeight() - 3,
                    28, 28
            );

            g2.dispose();

            super.paintComponent(g);
        }
    }

    // Rounded field border
    static class RoundedBorder extends javax.swing.border.AbstractBorder {

        private final Color borderColor;
        private final int radius;

        RoundedBorder(Color borderColor, int radius) {
            this.borderColor = borderColor;
            this.radius = radius;
        }

        @Override
        public void paintBorder(
                Component component,
                Graphics g,
                int x,
                int y,
                int width,
                int height
        ) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(borderColor);

            g2.drawRoundRect(
                    x + 1, y + 1,
                    width - 3, height - 3,
                    radius, radius
            );

            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(8, 12, 8, 12);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.set(8, 12, 8, 12);
            return insets;
        }
    }

    // Rounded gold button
    static class RoundedButton extends JButton {

        RoundedButton(String text) {
            super(text);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
        }

        @Override
        protected void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(getModel().isPressed()
                    ? new Color(210, 160, 75)
                    : getBackground());

            g2.fillRoundRect(
                    0, 0,
                    getWidth(), getHeight(),
                    16, 16
            );

            g2.dispose();

            super.paintComponent(g);
        }
    }
}