package com.restaurant.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

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
        JPanel outerPanel = new JPanel(new GridBagLayout());
        outerPanel.setBackground(Color.WHITE);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setPreferredSize(new Dimension(500, 480));
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
        c.insets = new Insets(0, 0, 45, 0);
        formPanel.add(logo, c);

        JLabel heading = new JLabel("Welcome Back!");
        heading.setFont(new Font("Arial", Font.BOLD, 28));

        c.gridy = 1;
        c.insets = new Insets(0, 0, 8, 0);
        formPanel.add(heading, c);

        JLabel subtitle = new JLabel("Sign in to manage your restaurant.");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitle.setForeground(Color.GRAY);

        c.gridy = 2;
        c.insets = new Insets(0, 0, 25, 0);
        formPanel.add(subtitle, c);

        emailField.setBorder(
                BorderFactory.createTitledBorder("Email address")
        );

        c.gridy = 3;
        c.insets = new Insets(0, 0, 15, 0);
        formPanel.add(emailField, c);

        passwordField.setBorder(
                BorderFactory.createTitledBorder("Password")
        );

        c.gridy = 4;
        c.insets = new Insets(0, 0, 12, 0);
        formPanel.add(passwordField, c);

        JCheckBox rememberCheck = new JCheckBox("Remember me");
        rememberCheck.setBackground(Color.WHITE);

        c.gridy = 5;
        c.insets = new Insets(0, 0, 25, 0);
        formPanel.add(rememberCheck, c);

        JButton loginButton = new JButton("Login");
        loginButton.setBackground(new Color(170, 10, 55));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Arial", Font.BOLD, 15));
        loginButton.setFocusPainted(false);
        loginButton.setPreferredSize(new Dimension(0, 45));
        loginButton.addActionListener(e -> login());

        c.gridy = 6;
        c.insets = new Insets(0, 0, 22, 0);
        formPanel.add(loginButton, c);

        JLabel registerLabel = new JLabel(
                "<html>Don't have an account? " +
                        "<font color='#AA0A37'>Contact Admin</font></html>"
        );
        registerLabel.setHorizontalAlignment(SwingConstants.CENTER);

        c.gridy = 7;
        c.insets = new Insets(0, 0, 0, 0);
        c.anchor = GridBagConstraints.CENTER;
        formPanel.add(registerLabel, c);

        outerPanel.add(formPanel);
        return outerPanel;
    }

    private void login() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter email and password."
            );
            return;
        }

        JOptionPane.showMessageDialog(this, "Login input accepted.");
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