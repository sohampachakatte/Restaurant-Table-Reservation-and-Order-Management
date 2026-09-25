package com.restaurant.ui;

import com.restaurant.dao.StaffDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class staffDashboard extends JFrame {

    private final StaffDAO staffDAO = new StaffDAO();
    private final int loggedInStaffId;

    private DefaultTableModel staffTableModel;
    private DefaultTableModel ordersTableModel;
    private JTable ordersTable;

    public staffDashboard(int loggedInStaffId) {
        this.loggedInStaffId = loggedInStaffId;

        setTitle("Restaurant Management - Staff Dashboard (ID: " + loggedInStaffId + ")");
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Split view: Top (Staff overview) | Bottom (Orders & Checkbox)
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainSplitPane.setDividerLocation(220);

        mainSplitPane.setTopComponent(createStaffOverviewPanel());
        mainSplitPane.setBottomComponent(createAssignedOrdersPanel());

        add(mainSplitPane, BorderLayout.CENTER);

        loadStaffData();
        loadOrdersData(this.loggedInStaffId); // Load orders assigned to logged-in staff
    }

    // Default constructor for GUI preview/testing
    public staffDashboard() {
        this(1);
    }

    // --- TOP PANEL: STAFF MEMBERS & WORKLOAD ---
    private JPanel createStaffOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("All Staff Members & Active Workloads"));

        String[] staffColumns = {"Staff ID", "Staff Name", "Role", "Active Orders Assigned"};
        staffTableModel = new DefaultTableModel(staffColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable staffTable = new JTable(staffTableModel);
        panel.add(new JScrollPane(staffTable), BorderLayout.CENTER);

        JButton refreshBtn = new JButton("Refresh Staff List");
        refreshBtn.addActionListener(e -> loadStaffData());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(refreshBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    // --- BOTTOM PANEL: ORDERS WITH CHECKBOX ---
    private JPanel createAssignedOrdersPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Orders Assigned to You (Check box to Complete)"));

        String[] orderColumns = {"Order ID", "Customer Name", "Assigned Staff", "Status", "Amount ($)", "Completed?"};

        ordersTableModel = new DefaultTableModel(orderColumns, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                // Column 5 renders as a JCheckBox
                if (columnIndex == 5) {
                    return Boolean.class;
                }
                return String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                // Only allow checking if not already completed
                Boolean isAlreadyCompleted = (Boolean) getValueAt(row, 5);
                return column == 5 && !Boolean.TRUE.equals(isAlreadyCompleted);
            }
        };

        ordersTable = new JTable(ordersTableModel);
        panel.add(new JScrollPane(ordersTable), BorderLayout.CENTER);

        // Listen for Checkbox toggle
        ordersTableModel.addTableModelListener(e -> {
            int row = e.getFirstRow();
            int col = e.getColumn();

            if (col == 5 && row >= 0 && row < ordersTable.getRowCount()) {
                Boolean isChecked = (Boolean) ordersTableModel.getValueAt(row, 5);
                if (Boolean.TRUE.equals(isChecked)) {
                    int orderId = (int) ordersTableModel.getValueAt(row, 0);

                    int confirm = JOptionPane.showConfirmDialog(
                            this,
                            "Are you sure you want to mark Order #" + orderId + " as COMPLETED?",
                            "Confirm Order Completion",
                            JOptionPane.YES_NO_OPTION
                    );

                    if (confirm == JOptionPane.YES_OPTION) {
                        if (staffDAO.markOrderCompleted(orderId)) {
                            JOptionPane.showMessageDialog(this, "Order #" + orderId + " marked as COMPLETED!");
                            loadStaffData();
                            loadOrdersData(loggedInStaffId);
                        }
                    } else {
                        // Reset checkbox if canceled
                        SwingUtilities.invokeLater(() -> ordersTableModel.setValueAt(false, row, 5));
                    }
                }
            }
        });

        // Filter Controls
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton showMyOrdersBtn = new JButton("Show My Orders");
        JButton showAllOrdersBtn = new JButton("Show All Orders");

        showMyOrdersBtn.addActionListener(e -> loadOrdersData(loggedInStaffId));
        showAllOrdersBtn.addActionListener(e -> loadOrdersData(-1));

        controlPanel.add(showMyOrdersBtn);
        controlPanel.add(showAllOrdersBtn);
        panel.add(controlPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void loadStaffData() {
        staffTableModel.setRowCount(0);
        List<Object[]> staffList = staffDAO.getAllStaffWithOrderCounts();
        for (Object[] row : staffList) {
            staffTableModel.addRow(row);
        }
    }

    private void loadOrdersData(int staffId) {
        ordersTableModel.setRowCount(0);
        List<Object[]> orders = staffDAO.getOrdersByStaff(staffId);
        for (Object[] row : orders) {
            ordersTableModel.addRow(row);
        }
    }
}
