package com.hospital.client.util;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;

public final class UiKit {

    // -- Buttons ---------------------------------------------------------------
    public static JButton primaryBtn(String text) { return btn(text, Theme.BLUE, Theme.WHITE); }
    public static JButton successBtn(String text) { return btn(text, Theme.GREEN, Theme.WHITE); }
    public static JButton dangerBtn(String text)  { return btn(text, Theme.ERR,  Theme.WHITE); }
    public static JButton warningBtn(String text) { return btn(text, Theme.ORANGE, Theme.WHITE); }
    public static JButton ghostBtn(String text)   { return btn(text, Theme.BG, Theme.TEXT); }

    private static JButton btn(String text, Color bg, Color fg) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? bg.darker()
                          : getModel().isRollover() ? bg.brighter() : bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setForeground(fg);
        b.setFont(Theme.F_BODY);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(7, 16, 7, 16));
        return b;
    }

    // -- Cards -----------------------------------------------------------------
    public static JPanel card() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(Theme.BORDER);
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 12, 12));
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBackground(Theme.CARD);
        p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        return p;
    }

    // -- Labels ----------------------------------------------------------------
    public static JLabel titleLbl(String t) {
        JLabel l = new JLabel(t); l.setFont(Theme.F_TITLE); l.setForeground(Theme.TEXT); return l;
    }
    public static JLabel sectionLbl(String t) {
        JLabel l = new JLabel(t); l.setFont(Theme.F_SECTION); l.setForeground(Theme.TEXT); return l;
    }
    public static JLabel bodyLbl(String t) {
        JLabel l = new JLabel(t); l.setFont(Theme.F_BODY); l.setForeground(Theme.TEXT); return l;
    }
    public static JLabel hintLbl(String t) {
        JLabel l = new JLabel(t); l.setFont(Theme.F_SMALL); l.setForeground(Theme.TEXT_SEC); return l;
    }

    // -- Badge -----------------------------------------------------------------
    public static JLabel badge(String text, Color bg) {
        JLabel l = new JLabel(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        l.setFont(Theme.F_BADGE); l.setForeground(Color.WHITE);
        l.setOpaque(false);
        l.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        return l;
    }

    // -- Fields ----------------------------------------------------------------
    public static JTextField field(String placeholder) {
        JTextField f = new JTextField();
        f.setFont(Theme.F_BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(7, 10, 7, 10)));
        f.setForeground(Theme.TEXT_HINT);
        f.setText(placeholder);
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (f.getText().equals(placeholder)) { f.setText(""); f.setForeground(Theme.TEXT); }
            }
            public void focusLost(FocusEvent e) {
                if (f.getText().isBlank()) { f.setText(placeholder); f.setForeground(Theme.TEXT_HINT); }
            }
        });
        return f;
    }

    public static JPasswordField passField() {
        JPasswordField f = new JPasswordField();
        f.setFont(Theme.F_BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(7, 10, 7, 10)));
        return f;
    }

    public static JTextArea textArea(String placeholder, int rows) {
        JTextArea ta = new JTextArea(rows, 0);
        ta.setFont(Theme.F_BODY); ta.setLineWrap(true); ta.setWrapStyleWord(true);
        ta.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(7, 10, 7, 10)));
        ta.setForeground(Theme.TEXT_HINT); ta.setText(placeholder);
        ta.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (ta.getText().equals(placeholder)) { ta.setText(""); ta.setForeground(Theme.TEXT); }
            }
            public void focusLost(FocusEvent e) {
                if (ta.getText().isBlank()) { ta.setText(placeholder); ta.setForeground(Theme.TEXT_HINT); }
            }
        });
        return ta;
    }

    public static JTextArea outputArea() {
        JTextArea ta = new JTextArea();
        ta.setFont(Theme.F_MONO); ta.setEditable(false);
        ta.setBackground(new Color(0xF8F9FA)); ta.setForeground(Theme.TEXT);
        ta.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        ta.setLineWrap(true); ta.setWrapStyleWord(true);
        return ta;
    }

    // -- Combo -----------------------------------------------------------------
    public static <T> JComboBox<T> combo(T[] items) {
        JComboBox<T> c = new JComboBox<>(items);
        c.setFont(Theme.F_BODY);
        c.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        return c;
    }

    // -- Table -----------------------------------------------------------------
    public static JTable styledTable(String[] cols) {
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setFont(Theme.F_TABLE);
        table.setRowHeight(32);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(0xE8F4FD));
        table.setSelectionForeground(Theme.TEXT);
        table.setBackground(Theme.CARD);
        table.getTableHeader().setFont(Theme.F_TABLE_HDR);
        table.getTableHeader().setBackground(new Color(0xF0F4F8));
        table.getTableHeader().setForeground(Theme.TEXT);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER));
        // Alternate row colors
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel) c.setBackground(row % 2 == 0 ? Theme.CARD : new Color(0xF8F9FB));
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return c;
            }
        });
        return table;
    }

    // -- Scroll ----------------------------------------------------------------
    public static JScrollPane scroll(Component c) {
        JScrollPane s = new JScrollPane(c);
        s.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        s.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        return s;
    }

    // -- Form row builder ------------------------------------------------------
    public static JPanel formRow(String label, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        JLabel lbl = bodyLbl(label);
        lbl.setPreferredSize(new Dimension(140, 30));
        row.add(lbl, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    // -- Separator -------------------------------------------------------------
    public static JSeparator divider() {
        JSeparator s = new JSeparator();
        s.setForeground(Theme.DIVIDER);
        return s;
    }

    // -- File chooser ----------------------------------------------------------
    public static File chooseImage(Component parent) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select Image");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Image files", "jpg", "jpeg", "png", "bmp"));
        return fc.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION ? fc.getSelectedFile() : null;
    }

    // -- Async helper ----------------------------------------------------------
    public static void async(Runnable bg, Runnable onDone) {
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() { bg.run(); return null; }
            @Override protected void done() { if (onDone != null) onDone.run(); }
        }.execute();
    }

    // -- Pretty JSON -----------------------------------------------------------
    public static String prettyJson(String raw) {
        try {
            var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapper.readValue(raw, Object.class));
        } catch (Exception e) { return raw; }
    }

    // -- Dashboard stat card ---------------------------------------------------
    public static JPanel statCard(String title, String value, Color accent) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.CARD); g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),12,12));
                g2.setColor(accent);    g2.fillRoundRect(0, 0, 5, getHeight(), 12, 12);
                g2.setColor(Theme.BORDER); g2.draw(new RoundRectangle2D.Float(0,0,getWidth()-1,getHeight()-1,12,12));
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 16));
        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valLbl.setForeground(accent);
        valLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(Theme.F_SMALL);
        titleLbl.setForeground(Theme.TEXT_SEC);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(valLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(titleLbl);
        return card;
    }

    private UiKit() {}
}
