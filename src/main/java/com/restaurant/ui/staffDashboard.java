package com.restaurant.ui;

import javax.swing.*;
import java.awt.*;

public class staffDashboard extends JFrame {

    public staffDashboard(String email) {
        setTitle("Staff Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        JLabel welcomeLabel = new JLabel(
                "Welcome Staff: " + email,
                SwingConstants.CENTER
        );

        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 28));
        welcomeLabel.setForeground(new Color(170, 10, 55));

        add(welcomeLabel);
    }
}