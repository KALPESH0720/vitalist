package com.hospital.client.ui;

import com.hospital.client.model.Session;
import com.hospital.client.ui.panels.*;
import com.hospital.client.util.Theme;
import com.hospital.client.util.UiKit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class MainWindow extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentArea   = new JPanel(cardLayout);
    private JPanel activeNavBtn        = null;

    private DashboardPanel dashboardPanel;

    public MainWindow() {
        super("Hospital Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 840);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(Theme.BG);
        setContentPane(root);

        root.add(buildSidebar(), BorderLayout.WEST);

        contentArea.setBackground(Theme.BG);
        buildPanels();
        root.add(contentArea, BorderLayout.CENTER);

        cardLayout.show(contentArea, "dashboard");
    }

    private void buildPanels() {
        dashboardPanel = new DashboardPanel();
        contentArea.add(dashboardPanel, "dashboard");
        contentArea.add(new PatientPanel(),     "patients");
        contentArea.add(new AppointmentPanel(), "appointments");
        contentArea.add(new InventoryPanel(),   "inventory");

        if (Session.get().canUseAI()) {
            contentArea.add(new AiPanel(), "ai");
        }
        if (Session.get().canManageUsers()) {
            contentArea.add(new UserPanel(), "users");
        }
    }

    // -- Sidebar ---------------------------------------------------------------
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(Theme.SIDEBAR_BG);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        sidebar.setPreferredSize(new Dimension(Theme.SIDEBAR_W, 0));
        sidebar.setLayout(new BorderLayout());

        // Logo
        JPanel logo = new JPanel(new BorderLayout());
        logo.setOpaque(false);
        logo.setBorder(BorderFactory.createEmptyBorder(20, 18, 16, 18));
        logo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0x2C3E6A)),
            BorderFactory.createEmptyBorder(20, 18, 16, 18)
        ));
        JLabel logoLbl = new JLabel("[+] HMS");
        logoLbl.setFont(new Font("Segoe UI", Font.BOLD, 17));
        logoLbl.setForeground(Color.WHITE);
        logo.add(logoLbl, BorderLayout.WEST);
        JLabel verLbl = new JLabel("v1.0");
        verLbl.setFont(Theme.F_SIDEBAR_SM);
        verLbl.setForeground(new Color(0x8899BB));
        logo.add(verLbl, BorderLayout.EAST);
        sidebar.add(logo, BorderLayout.NORTH);

        // Nav
        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setOpaque(false);
        nav.setBorder(BorderFactory.createEmptyBorder(12, 10, 10, 10));

        addNavSection(nav, "MAIN");
        JPanel dashBtn = navBtn("dashboard", "Dashboard",    "Overview & Stats",     Theme.BLUE);
        nav.add(dashBtn); nav.add(Box.createVerticalStrut(2));
        setActive(dashBtn); activeNavBtn = dashBtn;

        addNavSection(nav, "CLINICAL");
        nav.add(navBtn("patients",     "Patients",      "Admissions & Records",  Theme.GREEN));
        nav.add(Box.createVerticalStrut(2));
        nav.add(navBtn("appointments", "Appointments",  "Scheduling & Calendar", Theme.ORANGE));
        nav.add(Box.createVerticalStrut(2));
        nav.add(navBtn("inventory",    "Inventory",     "Medicines & Stock",     Theme.TEAL));

        if (Session.get().canUseAI()) {
            addNavSection(nav, "AI FEATURES");
            nav.add(navBtn("ai", "AI Tools",   "CaseTwin + UniRad3s",   Theme.PURPLE));
        }

        if (Session.get().canManageUsers()) {
            addNavSection(nav, "ADMINISTRATION");
            nav.add(navBtn("users", "Users",   "Manage Staff Access",   Theme.ERR));
        }

        sidebar.add(nav, BorderLayout.CENTER);
        sidebar.add(buildSidebarFooter(), BorderLayout.SOUTH);
        return sidebar;
    }

    private void addNavSection(JPanel nav, String title) {
        nav.add(Box.createVerticalStrut(12));
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lbl.setForeground(new Color(0x5A6F8F));
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 12, 4, 0));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        nav.add(lbl);
    }

    private JPanel navBtn(String card, String label, String sub, Color dot) {
        JPanel btn = new JPanel(new BorderLayout(10, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean active = Boolean.TRUE.equals(getClientProperty("active"));
                boolean hover  = Boolean.TRUE.equals(getClientProperty("hover"));
                if (active)      { g2.setColor(Theme.SIDEBAR_ACT); g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),8,8)); }
                else if (hover)  { g2.setColor(Theme.SIDEBAR_HOV); g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),8,8)); }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(9, 12, 9, 12));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        btn.setAlignmentX(LEFT_ALIGNMENT);

        // Dot
        JLabel dotLbl = new JLabel("*");
        dotLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        dotLbl.setForeground(dot);
        btn.add(dotLbl, BorderLayout.WEST);

        // Text
        JPanel txt = new JPanel();
        txt.setLayout(new BoxLayout(txt, BoxLayout.Y_AXIS));
        txt.setOpaque(false);
        JLabel nameLbl = new JLabel(label);
        nameLbl.setFont(Theme.F_SIDEBAR);
        nameLbl.setForeground(Theme.SIDEBAR_TXT);
        JLabel subLbl = new JLabel(sub);
        subLbl.setFont(Theme.F_SIDEBAR_SM);
        subLbl.setForeground(new Color(0x6B7FA0));
        txt.add(nameLbl); txt.add(subLbl);
        btn.add(txt, BorderLayout.CENTER);

        // Events
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (!Boolean.TRUE.equals(btn.getClientProperty("active"))) {
                    btn.putClientProperty("hover", true); btn.repaint();
                }
            }
            @Override public void mouseExited(MouseEvent e) {
                btn.putClientProperty("hover", false); btn.repaint();
            }
            @Override public void mouseClicked(MouseEvent e) {
                if (activeNavBtn != null) {
                    activeNavBtn.putClientProperty("active", false); activeNavBtn.repaint();
                }
                setActive(btn); activeNavBtn = btn;
                cardLayout.show(contentArea, card);
                if ("dashboard".equals(card) && dashboardPanel != null) {
                    dashboardPanel.loadStats();
                }
            }
        });
        return btn;
    }

    private void setActive(JPanel btn) {
        btn.putClientProperty("active", true); btn.repaint();
    }

    private JPanel buildSidebarFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0x2C3E6A)),
            BorderFactory.createEmptyBorder(12, 16, 16, 16)
        ));

        // User info
        JPanel userInfo = new JPanel(new BorderLayout(8, 0));
        userInfo.setOpaque(false);

        JLabel avatar = new JLabel("[" + Session.get().getFullName().charAt(0) + "]");
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        avatar.setForeground(Theme.BLUE);
        userInfo.add(avatar, BorderLayout.WEST);

        JPanel txt = new JPanel();
        txt.setLayout(new BoxLayout(txt, BoxLayout.Y_AXIS));
        txt.setOpaque(false);
        JLabel name = new JLabel(Session.get().getFullName());
        name.setFont(new Font("Segoe UI", Font.BOLD, 12));
        name.setForeground(Theme.SIDEBAR_TXT);
        JLabel role = new JLabel(Session.get().getRole());
        role.setFont(Theme.F_SIDEBAR_SM);
        role.setForeground(new Color(0x6B7FA0));
        txt.add(name); txt.add(role);
        userInfo.add(txt, BorderLayout.CENTER);
        footer.add(userInfo, BorderLayout.CENTER);

        // Logout
        JButton logoutBtn = new JButton("Logout") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? Theme.ERR : new Color(0x2C3E6A));
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),6,6));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        logoutBtn.setFont(Theme.F_SIDEBAR_SM);
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setContentAreaFilled(false);
        logoutBtn.setOpaque(false);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        logoutBtn.addActionListener(e -> {
            int c = JOptionPane.showConfirmDialog(this, "Logout?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (c == JOptionPane.YES_OPTION) {
                Session.get().logout();
                dispose();
                com.hospital.client.HospitalClientApp.showLogin();
            }
        });
        footer.add(logoutBtn, BorderLayout.EAST);
        return footer;
    }
}
