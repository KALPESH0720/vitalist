package com.hospital.client.ui.panels;

import com.fasterxml.jackson.databind.JsonNode;
import com.hospital.client.api.ApiClient;
import com.hospital.client.util.Theme;
import com.hospital.client.util.UiKit;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class UserPanel extends JPanel {

    private final JTable table;
    private final DefaultTableModel model;
    private final JLabel statusLbl;

    private static final String[] COLS = {"ID", "Username", "Full Name", "Role", "Email", "Phone", "Active"};

    public UserPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        header.add(UiKit.titleLbl("User Management"), BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton refreshBtn = UiKit.ghostBtn("Refresh");
        JButton addBtn     = UiKit.successBtn("+ Add User");
        refreshBtn.addActionListener(e -> loadUsers());
        addBtn.addActionListener(e -> openDialog(null));
        actions.add(refreshBtn); actions.add(addBtn);
        header.add(actions, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        table = UiKit.styledTable(COLS);
        model = (DefaultTableModel) table.getModel();
        add(UiKit.scroll(table), BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        bottomBar.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnGroup.setOpaque(false);
        JButton editBtn   = UiKit.primaryBtn("Edit");
        JButton deleteBtn = UiKit.dangerBtn("Delete");
        editBtn.addActionListener(e -> openDialog(getSelectedId()));
        deleteBtn.addActionListener(e -> deleteSelected());
        btnGroup.add(editBtn); btnGroup.add(deleteBtn);
        statusLbl = UiKit.hintLbl("Loading...");
        bottomBar.add(btnGroup, BorderLayout.WEST);
        bottomBar.add(statusLbl, BorderLayout.EAST);
        add(bottomBar, BorderLayout.SOUTH);

        loadUsers();
    }

    private void loadUsers() {
        statusLbl.setText("Loading...");
        SwingWorker<JsonNode, Void> w = new SwingWorker<>() {
            @Override protected JsonNode doInBackground() throws Exception { return ApiClient.getUsers(); }
            @Override protected void done() {
                try {
                    JsonNode data = get(); model.setRowCount(0);
                    if (data.isArray()) for (JsonNode u : data) {
                        model.addRow(new Object[]{
                            u.path("id").asLong(),
                            u.path("username").asText(),
                            u.path("fullName").asText(),
                            u.path("role").asText(),
                            u.path("email").asText(),
                            u.path("phone").asText(),
                            u.path("active").asBoolean() ? "Yes" : "No"
                        });
                    }
                    statusLbl.setText(model.getRowCount() + " user(s)");
                } catch (Exception ex) { statusLbl.setText("Error: " + ex.getMessage()); }
            }
        };
        w.execute();
    }

    private Long getSelectedId() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a user first."); return null; }
        return (Long) model.getValueAt(row, 0);
    }

    private void deleteSelected() {
        Long id = getSelectedId(); if (id == null) return;
        int c = JOptionPane.showConfirmDialog(this, "Delete this user?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (c != JOptionPane.YES_OPTION) return;
        SwingWorker<Void, Void> w = new SwingWorker<>() {
            @Override protected Void doInBackground() throws Exception { ApiClient.deleteUser(id); return null; }
            @Override protected void done() { loadUsers(); }
        };
        w.execute();
    }

    private void openDialog(Long userId) {
        UserDialog dlg = new UserDialog((Frame) SwingUtilities.getWindowAncestor(this), userId);
        dlg.setVisible(true);
        if (dlg.isSaved()) loadUsers();
    }
}

class UserDialog extends JDialog {
    private boolean saved = false;
    private final Long userId;
    private JTextField usernameF, fullNameF, emailF, phoneF;
    private JPasswordField passF;
    private JComboBox<String> roleC;
    private JCheckBox activeChk;

    UserDialog(Frame parent, Long userId) {
        super(parent, userId == null ? "Add User" : "Edit User", true);
        this.userId = userId;
        setSize(460, 480);
        setLocationRelativeTo(parent);

        JPanel root = new JPanel(new BorderLayout()); root.setBackground(Theme.CARD);
        JPanel header = new JPanel(new BorderLayout()); header.setBackground(Theme.NAVY);
        header.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        JLabel title = new JLabel(userId == null ? "Add New User" : "Edit User");
        title.setFont(Theme.F_SECTION); title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST); root.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.CARD);
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(6, 4, 6, 4);

        usernameF = UiKit.field("Username"); fullNameF = UiKit.field("Full name");
        emailF    = UiKit.field("Email");    phoneF    = UiKit.field("Phone");
        passF     = UiKit.passField();
        roleC     = new JComboBox<>(new String[]{"ADMIN","DOCTOR","RECEPTIONIST"});
        activeChk = new JCheckBox("Active", true);

        addRow(form, gbc, "Username *",   usernameF, 0);
        addRow(form, gbc, "Full Name *",  fullNameF, 1);
        addRow(form, gbc, "Password *",   passF,     2);
        addRow(form, gbc, "Role *",       roleC,     3);
        addRow(form, gbc, "Email",        emailF,    4);
        addRow(form, gbc, "Phone",        phoneF,    5);
        gbc.gridx=1; gbc.gridy=6; form.add(activeChk, gbc);

        root.add(new JScrollPane(form), BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        btns.setBackground(Theme.CARD);
        btns.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER));
        JButton cancelBtn = UiKit.ghostBtn("Cancel"); JButton saveBtn = UiKit.primaryBtn("Save");
        cancelBtn.addActionListener(e -> dispose());
        saveBtn.addActionListener(e -> save());
        btns.add(cancelBtn); btns.add(saveBtn);
        root.add(btns, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private void addRow(JPanel p, GridBagConstraints gbc, String label, JComponent field, int row) {
        gbc.gridx=0; gbc.gridy=row; gbc.weightx=0.35; p.add(UiKit.bodyLbl(label), gbc);
        gbc.gridx=1; gbc.weightx=0.65; p.add(field, gbc);
    }

    private void save() {
        if (usernameF.getText().isBlank() || fullNameF.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Username and Full Name are required."); return;
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("username", usernameF.getText().trim());
        body.put("fullName", fullNameF.getText().trim());
        String pw = new String(passF.getPassword());
        if (!pw.isBlank()) body.put("password", pw);
        body.put("role",   roleC.getSelectedItem());
        body.put("email",  emailF.getText().trim());
        body.put("phone",  phoneF.getText().trim());
        body.put("active", activeChk.isSelected());

        SwingWorker<JsonNode, Void> w = new SwingWorker<>() {
            @Override protected JsonNode doInBackground() throws Exception {
                return userId == null ? ApiClient.createUser(body) : ApiClient.updateUser(userId, body);
            }
            @Override protected void done() {
                try { get(); saved = true; dispose(); }
                catch (Exception ex) { JOptionPane.showMessageDialog(UserDialog.this, "Error: " + ex.getMessage()); }
            }
        };
        w.execute();
    }

    boolean isSaved() { return saved; }
}
