package com.hospital.client.ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.hospital.client.api.ApiClient;
import com.hospital.client.model.Session;
import com.hospital.client.util.Theme;
import com.hospital.client.util.UiKit;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class LoginDialog extends JDialog {

    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JLabel errorLabel;
    private final JButton loginBtn;
    private boolean loggedIn = false;

    public LoginDialog(Frame parent) {
        super(parent, "Hospital Management System - Login", true);
        setUndecorated(true);
        setSize(420, 520);
        setLocationRelativeTo(parent);
        setBackground(new Color(0, 0, 0, 0));

        JPanel root = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                // Top accent bar
                g2.setColor(Theme.NAVY);
                g2.fillRoundRect(0, 0, getWidth(), 90, 16, 16);
                g2.fillRect(0, 74, getWidth(), 16);
                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // -- Header ------------------------------------------------------------
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));
        JLabel ico = new JLabel("[+]");
        ico.setFont(new Font("Segoe UI", Font.BOLD, 28));
        ico.setForeground(Color.WHITE);
        ico.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel titleLbl = new JLabel("Hospital Management");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLbl.setForeground(Color.WHITE);
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel subLbl = new JLabel("Clinical Information System");
        subLbl.setFont(Theme.F_SMALL);
        subLbl.setForeground(new Color(0xCDD9F0));
        subLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(ico);
        header.add(Box.createVerticalStrut(6));
        header.add(titleLbl);
        header.add(Box.createVerticalStrut(2));
        header.add(subLbl);
        root.add(header);

        // -- Form --------------------------------------------------------------
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createEmptyBorder(32, 40, 32, 40));

        JLabel uLabel = UiKit.bodyLbl("Username");
        uLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(uLabel);
        form.add(Box.createVerticalStrut(6));

        usernameField = UiKit.field("Enter username");
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        form.add(usernameField);
        form.add(Box.createVerticalStrut(20));

        JLabel pLabel = UiKit.bodyLbl("Password");
        pLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(pLabel);
        form.add(Box.createVerticalStrut(6));

        passwordField = UiKit.passField();
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        form.add(passwordField);
        form.add(Box.createVerticalStrut(8));

        errorLabel = new JLabel(" ");
        errorLabel.setFont(Theme.F_SMALL);
        errorLabel.setForeground(Theme.ERR);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(errorLabel);
        form.add(Box.createVerticalStrut(20));

        loginBtn = UiKit.primaryBtn("Sign In");
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        loginBtn.addActionListener(e -> doLogin());
        form.add(loginBtn);
        form.add(Box.createVerticalStrut(24));

    

        root.add(form);

        // Enter key triggers login
        getRootPane().setDefaultButton(loginBtn);
        passwordField.addActionListener(e -> doLogin());

        setContentPane(root);
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        char[] pw = passwordField.getPassword();
        if (username.isBlank() || pw.length == 0) {
            errorLabel.setText("Please enter username and password.");
            return;
        }
        loginBtn.setEnabled(false);
        loginBtn.setText("Signing in...");
        errorLabel.setText(" ");

        String password = new String(pw);
        SwingWorker<JsonNode, Void> worker = new SwingWorker<>() {
            @Override protected JsonNode doInBackground() throws Exception {
                return ApiClient.login(username, password);
            }
            @Override protected void done() {
                try {
                    JsonNode resp = get();
                    if (resp.path("success").asBoolean(false)) {
                        Session.get().login(
                            resp.path("userId").asLong(),
                            resp.path("username").asText(),
                            resp.path("fullName").asText(),
                            resp.path("role").asText(),
                            resp.path("basicToken").asText()
                        );
                        loggedIn = true;
                        dispose();
                    } else {
                        errorLabel.setText(resp.path("error").asText("Login failed"));
                        loginBtn.setEnabled(true);
                        loginBtn.setText("Sign In");
                    }
                } catch (Exception ex) {
                    errorLabel.setText("Cannot connect to server. Is the backend running?");
                    loginBtn.setEnabled(true);
                    loginBtn.setText("Sign In");
                }
            }
        };
        worker.execute();
    }

    public boolean isLoggedIn() { return loggedIn; }
}
