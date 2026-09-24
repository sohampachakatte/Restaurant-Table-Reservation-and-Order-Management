package com.restaurant.ui;

import javax.swing.*;
import java.awt.*;

public class customerDashboard extends JFrame {

    public customerDashboard(String email) {
        setTitle("Customer Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        JLabel welcomeLabel = new JLabel(
                "Welcome Customer: " + email,
                SwingConstants.CENTER
        );

        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 28));
        welcomeLabel.setForeground(new Color(170, 10, 55));

        add(welcomeLabel);
    }
}