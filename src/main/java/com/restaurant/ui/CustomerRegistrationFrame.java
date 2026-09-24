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

    public CustomerRegistrationFrame() {
        setTitle("Restaurant Management System - Customer Registration");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel mainPanel = new JPanel(new GridLayout(1, 2));
        mainPanel.add(createRegistrationPanel());
        mainPanel.add(new ImagePanel());

        add(mainPanel);
    }

    private JPanel createRegistrationPanel() {
        JPanel outerPanel = new JPanel(new GridBagLayout());
        outerPanel.setBackground(Color.WHITE);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setPreferredSize(new Dimension(500, 620));
        formPanel.setBorder(new EmptyBorder(35, 55, 35, 55));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridwidth = 2;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        JLabel logo = new JLabel("Restaurant Management System");
        logo.setFont(new Font("Arial", Font.BOLD, 20));
        logo.setForeground(new Color(170, 10, 55));

        c.gridy = 0;
        c.insets = new Insets(0, 0, 35, 0);
        formPanel.add(logo, c);

        JLabel heading = new JLabel("Create Customer Account");
        heading.setFont(new Font("Arial", Font.BOLD, 28));

        c.gridy = 1;
        c.insets = new Insets(0, 0, 8, 0);
        formPanel.add(heading, c);

        JLabel subtitle = new JLabel(
                "Register to book tables and manage your orders."
        );
        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitle.setForeground(Color.GRAY);

        c.gridy = 2;
        c.insets = new Insets(0, 0, 20, 0);
        formPanel.add(subtitle, c);

        addField(formPanel, c, 3, "First name", firstNameField);
        addField(formPanel, c, 4, "Middle name (optional)", middleNameField);
        addField(formPanel, c, 5, "Last name", lastNameField);
        addField(formPanel, c, 6, "Email address", emailField);
        addField(formPanel, c, 7, "Password", passwordField);
        addField(formPanel, c, 8, "Confirm password", confirmPasswordField);

        JButton registerButton = new JButton("Register as Customer");
        registerButton.setBackground(new Color(170, 10, 55));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFont(new Font("Arial", Font.BOLD, 15));
        registerButton.setFocusPainted(false);
        registerButton.setPreferredSize(new Dimension(0, 45));
        registerButton.addActionListener(e -> registerCustomer());

        c.gridy = 9;
        c.insets = new Insets(15, 0, 10, 0);
        formPanel.add(registerButton, c);

        JButton backButton = new JButton(
                "<html>Already have an account? " +
                        "<font color='#AA0A37'><u>Login</u></font></html>"
        );

        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.setBorderPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setFocusPainted(false);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        backButton.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        c.gridy = 10;
        c.insets = new Insets(0, 0, 0, 0);
        c.anchor = GridBagConstraints.CENTER;
        formPanel.add(backButton, c);

        outerPanel.add(formPanel);
        return outerPanel;
    }

    private void addField(
            JPanel panel,
            GridBagConstraints c,
            int row,
            String label,
            JTextField field
    ) {
        field.setBorder(BorderFactory.createTitledBorder(label));

        c.gridy = row;
        c.insets = new Insets(0, 0, 10, 0);
        panel.add(field, c);
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
                    "Please fill all required fields."
            );
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(
                    this,
                    "Passwords do not match."
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
                    "Customer account created successfully."
            );

            dispose();
            new LoginFrame().setVisible(true);

        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    "Registration failed: " + exception.getMessage()
            );
        }
    }

    static class ImagePanel extends JPanel {
        private final Image image;

        ImagePanel() {
            setBackground(new Color(28, 12, 8));
            image = new ImageIcon("images/login.jpg").getImage();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

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

            g.drawImage(image, x, y, newWidth, newHeight, this);
        }
    }
}