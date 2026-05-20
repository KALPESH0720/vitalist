package com.hospital.client.ui.panels;

import com.fasterxml.jackson.databind.JsonNode;
import com.hospital.client.api.ApiClient;
import com.hospital.client.model.Session;
import com.hospital.client.util.Theme;
import com.hospital.client.util.UiKit;

import javax.swing.*;
import java.awt.*;

public class DashboardPanel extends JPanel {

    private final JPanel statsGrid;
    private final JLabel welcomeLbl;
    private final JLabel roleLbl;

    public DashboardPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        // -- Header ------------------------------------------------------------
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 24, 0));

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);
        welcomeLbl = UiKit.titleLbl("Welcome back");
        roleLbl    = UiKit.hintLbl("Loading...");
        titleBlock.add(welcomeLbl);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(roleLbl);
        header.add(titleBlock, BorderLayout.WEST);

        JButton refreshBtn = UiKit.primaryBtn("Refresh");
        refreshBtn.addActionListener(e -> loadStats());
        header.add(refreshBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // -- Stats grid --------------------------------------------------------
        statsGrid = new JPanel(new GridLayout(2, 3, 16, 16));
        statsGrid.setOpaque(false);
        add(statsGrid, BorderLayout.CENTER);

        // -- Quick info panel --------------------------------------------------
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(24, 0, 0, 0));

        JPanel infoCard = UiKit.card();
        infoCard.setLayout(new BoxLayout(infoCard, BoxLayout.Y_AXIS));
        // JLabel infoTitle = UiKit.sectionLbl("Access Level for Your Role");
        // infoTitle.setAlignmentX(LEFT_ALIGNMENT);
        // infoCard.add(infoTitle);
        infoCard.add(Box.createVerticalStrut(12));
        // String[][] perms = {
        //     {"Patients",     "View, Admit, Edit, Discharge"},
        //     {"Appointments", "View, Create, Edit, Cancel"},
        //     {"Inventory",    Session.get().canWriteInventory() ? "View, Add, Edit, Delete" : "View only"},
        //     {"AI Features",  Session.get().canUseAI() ? "CaseTwin + UniRad3s (X-ray)" : "Not available for your role"},
        //     {"User Mgmt",    Session.get().canManageUsers() ? "Full access" : "Not available for your role"},
        // };
        // for (String[] perm : perms) {
        //     JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 2));
        //     row.setOpaque(false);
        //     row.setAlignmentX(LEFT_ALIGNMENT);
        //     JLabel k = UiKit.bodyLbl(perm[0] + ":  ");
        //     k.setFont(new Font("Segoe UI", Font.BOLD, 13));
        //     JLabel v = UiKit.hintLbl(perm[1]);
        //     row.add(k); row.add(v);
        //     infoCard.add(row);
        // }
        bottom.add(infoCard, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        loadStats();
    }

    public void loadStats() {
        welcomeLbl.setText("Welcome, " + Session.get().getFullName());
        roleLbl.setText("Role: " + Session.get().getRole() + "  |  " + java.time.LocalDate.now());
        statsGrid.removeAll();

        // Loading placeholders
        Color[] colors = {Theme.BLUE, Theme.GREEN, Theme.ORANGE, Theme.ERR, Theme.PURPLE, Theme.TEAL};
        String[] titles = {"Total Patients","Admitted","Today's Appointments","Low Stock Items","Expired Items","Total Inventory"};
        for (int i = 0; i < 6; i++) {
            statsGrid.add(UiKit.statCard(titles[i], "...", colors[i]));
        }
        statsGrid.revalidate(); statsGrid.repaint();

        SwingWorker<JsonNode, Void> w = new SwingWorker<>() {
            @Override protected JsonNode doInBackground() throws Exception {
                return ApiClient.getDashboardStats();
            }
            @Override protected void done() {
                try {
                    JsonNode n = get();
                    statsGrid.removeAll();
                    statsGrid.add(UiKit.statCard("Total Patients",      n.path("totalPatients").asText("0"),      Theme.BLUE));
                    statsGrid.add(UiKit.statCard("Admitted",            n.path("admittedPatients").asText("0"),   Theme.GREEN));
                    statsGrid.add(UiKit.statCard("Today's Appointments",n.path("todayAppointments").asText("0"),  Theme.ORANGE));
                    statsGrid.add(UiKit.statCard("Low Stock Items",     n.path("lowStockItems").asText("0"),      Theme.ERR));
                    statsGrid.add(UiKit.statCard("Expired Items",       n.path("expiredItems").asText("0"),       Theme.PURPLE));
                    statsGrid.add(UiKit.statCard("Total Inventory",     n.path("totalInventory").asText("0"),     Theme.TEAL));
                    statsGrid.revalidate(); statsGrid.repaint();
                } catch (Exception ex) {
                    statsGrid.removeAll();
                    JLabel err = UiKit.hintLbl("Could not load stats: " + ex.getMessage());
                    statsGrid.add(err);
                    statsGrid.revalidate(); statsGrid.repaint();
                }
            }
        };
        w.execute();
    }
}
