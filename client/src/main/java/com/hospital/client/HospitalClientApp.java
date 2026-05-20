package com.hospital.client;

import com.formdev.flatlaf.FlatLightLaf;
import com.hospital.client.ui.LoginDialog;
import com.hospital.client.ui.MainWindow;

import javax.swing.*;

public class HospitalClientApp {

    public static void main(String[] args) {
        try {
            FlatLightLaf.setup();
            UIManager.put("Button.arc",          8);
            UIManager.put("Component.arc",        8);
            UIManager.put("TextComponent.arc",    6);
            UIManager.put("ScrollBar.thumbArc",   999);
            UIManager.put("ScrollBar.width",      8);
            UIManager.put("TabbedPane.showTabSeparators", false);
        } catch (Exception e) {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
        }

        SwingUtilities.invokeLater(HospitalClientApp::showLogin);
    }

    public static void showLogin() {
        JFrame dummy = new JFrame();
        LoginDialog login = new LoginDialog(dummy);
        login.setVisible(true);
        dummy.dispose();

        if (login.isLoggedIn()) {
            SwingUtilities.invokeLater(() -> {
                MainWindow window = new MainWindow();
                window.setVisible(true);
            });
        } else {
            System.exit(0);
        }
    }
}
