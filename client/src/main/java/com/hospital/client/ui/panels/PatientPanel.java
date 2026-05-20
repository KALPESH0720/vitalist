package com.hospital.client.ui.panels;

import com.fasterxml.jackson.databind.JsonNode;
import com.hospital.client.api.ApiClient;
import com.hospital.client.model.Session;
import com.hospital.client.util.Theme;
import com.hospital.client.util.UiKit;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;

public class PatientPanel extends JPanel {

    private final JTable table;
    private final DefaultTableModel model;
    private final JTextField searchField;
    private final JLabel statusLbl;

    private static final String[] COLS = {
        "ID", "Patient ID", "Name", "Age", "Gender", "Contact", "Blood", "Ward/Room", "Doctor", "Status"
    };

    public PatientPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        // -- Header ------------------------------------------------------------
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        header.add(UiKit.titleLbl("Patient Management"), BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        searchField = UiKit.field("Search patients...");
        searchField.setPreferredSize(new Dimension(220, 34));
        JButton searchBtn = UiKit.primaryBtn("Search");
        searchBtn.addActionListener(e -> loadPatients(searchField.getText()));
        JButton addBtn = UiKit.successBtn("+ Admit Patient");
        addBtn.addActionListener(e -> openAdmitDialog(null));
        JButton refreshBtn = UiKit.ghostBtn("Refresh");
        refreshBtn.addActionListener(e -> loadPatients(null));
        actions.add(searchField); actions.add(searchBtn);
        actions.add(refreshBtn);  actions.add(addBtn);
        header.add(actions, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // -- Table -------------------------------------------------------------
        table = UiKit.styledTable(COLS);
        model = (DefaultTableModel) table.getModel();
        JScrollPane scroll = UiKit.scroll(table);
        add(scroll, BorderLayout.CENTER);

        // -- Bottom action bar -------------------------------------------------
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        bottomBar.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnGroup.setOpaque(false);
        JButton viewBtn      = UiKit.primaryBtn("View / Edit");
        JButton dischargeBtn = UiKit.warningBtn("Discharge");
        JButton deleteBtn    = UiKit.dangerBtn("Delete");
        JButton appointBtn   = UiKit.ghostBtn("Appointments");
        viewBtn.addActionListener(e -> openAdmitDialog(getSelectedId()));
        dischargeBtn.addActionListener(e -> dischargeSelected());
        deleteBtn.addActionListener(e -> deleteSelected());
        appointBtn.addActionListener(e -> viewAppointments(getSelectedId()));
        btnGroup.add(viewBtn); btnGroup.add(dischargeBtn);
        btnGroup.add(deleteBtn); btnGroup.add(appointBtn);

        statusLbl = UiKit.hintLbl("Loading patients...");
        bottomBar.add(btnGroup,   BorderLayout.WEST);
        bottomBar.add(statusLbl,  BorderLayout.EAST);
        add(bottomBar, BorderLayout.SOUTH);

        loadPatients(null);
    }

    private void loadPatients(String search) {
        statusLbl.setText("Loading...");
        SwingWorker<JsonNode, Void> w = new SwingWorker<>() {
            @Override protected JsonNode doInBackground() throws Exception {
                return (search != null && !search.isBlank())
                    ? ApiClient.searchPatients(search)
                    : ApiClient.getPatients();
            }
            @Override protected void done() {
                try {
                    JsonNode data = get();
                    model.setRowCount(0);
                    if (data.isArray()) {
                        for (JsonNode p : data) {
                            String doctor = "";
                            if (p.has("assignedDoctor") && !p.get("assignedDoctor").isNull())
                                doctor = p.path("assignedDoctor").path("fullName").asText("");
                            model.addRow(new Object[]{
                                p.path("id").asLong(),
                                p.path("patientId").asText(),
                                p.path("fullName").asText(),
                                p.path("age").asInt(),
                                p.path("gender").asText(),
                                p.path("contact").asText(),
                                p.path("bloodGroup").asText(),
                                p.path("ward").asText("") + "/" + p.path("roomNumber").asText(""),
                                doctor,
                                p.path("status").asText()
                            });
                        }
                    }
                    statusLbl.setText(model.getRowCount() + " patient(s) found");
                } catch (Exception ex) {
                    statusLbl.setText("Error: " + ex.getMessage());
                }
            }
        };
        w.execute();
    }

    private Long getSelectedId() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a patient first."); return null; }
        return (Long) model.getValueAt(row, 0);
    }

    private void dischargeSelected() {
        Long id = getSelectedId();
        if (id == null) return;
        int c = JOptionPane.showConfirmDialog(this, "Discharge this patient?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (c != JOptionPane.YES_OPTION) return;
        SwingWorker<Void, Void> w = new SwingWorker<>() {
            @Override protected Void doInBackground() throws Exception {
                ApiClient.dischargePatient(id); return null;
            }
            @Override protected void done() { loadPatients(null); }
        };
        w.execute();
    }

    private void deleteSelected() {
        Long id = getSelectedId();
        if (id == null) return;
        int c = JOptionPane.showConfirmDialog(this, "Delete this patient? This cannot be undone.", "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c != JOptionPane.YES_OPTION) return;
        SwingWorker<Void, Void> w = new SwingWorker<>() {
            @Override protected Void doInBackground() throws Exception {
                ApiClient.deletePatient(id); return null;
            }
            @Override protected void done() { loadPatients(null); }
        };
        w.execute();
    }

    private void viewAppointments(Long id) {
        if (id == null) return;
        JOptionPane.showMessageDialog(this, "Switch to Appointments tab and filter by patient ID: " + id);
    }

    private void openAdmitDialog(Long patientId) {
        PatientDialog dlg = new PatientDialog((Frame) SwingUtilities.getWindowAncestor(this), patientId);
        dlg.setVisible(true);
        if (dlg.isSaved()) loadPatients(null);
    }
}

// -- Patient Add / Edit Dialog -------------------------------------------------
class PatientDialog extends JDialog {

    private boolean saved = false;
    private final Long patientId;

    private JTextField nameF, ageF, contactF, bloodF, addressF, wardF, roomF, pidF;
    private JComboBox<String> genderC, statusC;
    private JComboBox<Object> doctorC;
    private JTextArea notesA;

    PatientDialog(Frame parent, Long patientId) {
        super(parent, patientId == null ? "Admit New Patient" : "Edit Patient", true);
        this.patientId = patientId;
        setSize(560, 640);
        setLocationRelativeTo(parent);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(Theme.CARD);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.NAVY);
        header.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        JLabel title = new JLabel(patientId == null ? "Admit New Patient" : "Edit Patient Record");
        title.setFont(Theme.F_SECTION); title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);
        root.add(header, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.CARD);
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(5, 4, 5, 4);

        nameF    = UiKit.field("Full name");
        ageF     = UiKit.field("Age");
        contactF = UiKit.field("Phone number");
        bloodF   = UiKit.field("e.g. A+");
        addressF = UiKit.field("Full address");
        wardF    = UiKit.field("e.g. General, ICU");
        roomF    = UiKit.field("Room number");
        pidF     = UiKit.field("Auto-generated if blank");
        notesA   = UiKit.textArea("Emergency notes...", 3);

        genderC = new JComboBox<>(new String[]{"MALE","FEMALE","OTHER"});
        statusC = new JComboBox<>(new String[]{"ADMITTED","OUTPATIENT","DISCHARGED"});
        doctorC = new JComboBox<>();
        doctorC.addItem("-- No Doctor Assigned --");
        loadDoctors();

        addRow(form, gbc, "Patient ID",   pidF,     0);
        addRow(form, gbc, "Full Name *",  nameF,    1);
        addRow(form, gbc, "Age *",        ageF,     2);
        addRow(form, gbc, "Gender *",     genderC,  3);
        addRow(form, gbc, "Contact *",    contactF, 4);
        addRow(form, gbc, "Blood Group",  bloodF,   5);
        addRow(form, gbc, "Address",      addressF, 6);
        addRow(form, gbc, "Ward",         wardF,    7);
        addRow(form, gbc, "Room",         roomF,    8);
        addRow(form, gbc, "Assign Doctor",doctorC,  9);
        addRow(form, gbc, "Status",       statusC, 10);
        gbc.gridx=0; gbc.gridy=11; gbc.gridwidth=1; form.add(UiKit.bodyLbl("Notes"), gbc);
        gbc.gridx=1; form.add(new JScrollPane(notesA), gbc);

        JScrollPane formScroll = new JScrollPane(form);
        formScroll.setBorder(null);
        root.add(formScroll, BorderLayout.CENTER);

        // Buttons
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        btns.setBackground(Theme.CARD);
        btns.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER));
        JButton cancelBtn = UiKit.ghostBtn("Cancel");
        JButton saveBtn   = UiKit.primaryBtn("Save");
        cancelBtn.addActionListener(e -> dispose());
        saveBtn.addActionListener(e -> save());
        btns.add(cancelBtn); btns.add(saveBtn);
        root.add(btns, BorderLayout.SOUTH);

        setContentPane(root);

        if (patientId != null) loadExistingPatient();
    }

    private void addRow(JPanel p, GridBagConstraints gbc, String label, JComponent field, int row) {
        gbc.gridx=0; gbc.gridy=row; gbc.gridwidth=1; gbc.weightx=0.3;
        p.add(UiKit.bodyLbl(label), gbc);
        gbc.gridx=1; gbc.weightx=0.7;
        p.add(field, gbc);
    }

    private void loadDoctors() {
        SwingWorker<JsonNode, Void> w = new SwingWorker<>() {
            @Override protected JsonNode doInBackground() throws Exception { return ApiClient.getDoctors(); }
            @Override protected void done() {
                try {
                    JsonNode docs = get();
                    if (docs.isArray()) {
                        for (JsonNode d : docs) {
                            doctorC.addItem(d.path("id").asText() + " | " + d.path("fullName").asText());
                        }
                    }
                } catch (Exception ignored) {}
            }
        };
        w.execute();
    }

    private void loadExistingPatient() {
        SwingWorker<JsonNode, Void> w = new SwingWorker<>() {
            @Override protected JsonNode doInBackground() throws Exception { return ApiClient.getPatient(patientId); }
            @Override protected void done() {
                try {
                    JsonNode p = get();
                    pidF.setText(p.path("patientId").asText());
                    nameF.setText(p.path("fullName").asText()); nameF.setForeground(Theme.TEXT);
                    ageF.setText(p.path("age").asText()); ageF.setForeground(Theme.TEXT);
                    contactF.setText(p.path("contact").asText()); contactF.setForeground(Theme.TEXT);
                    bloodF.setText(p.path("bloodGroup").asText()); bloodF.setForeground(Theme.TEXT);
                    addressF.setText(p.path("address").asText()); addressF.setForeground(Theme.TEXT);
                    wardF.setText(p.path("ward").asText()); wardF.setForeground(Theme.TEXT);
                    roomF.setText(p.path("roomNumber").asText()); roomF.setForeground(Theme.TEXT);
                    genderC.setSelectedItem(p.path("gender").asText("MALE"));
                    statusC.setSelectedItem(p.path("status").asText("ADMITTED"));
                    if (!p.path("emergencyNotes").asText().isBlank()) {
                        notesA.setText(p.path("emergencyNotes").asText()); notesA.setForeground(Theme.TEXT);
                    }
                } catch (Exception ignored) {}
            }
        };
        w.execute();
    }

    private void save() {
        String name = nameF.getText().trim();
        String age  = ageF.getText().trim();
        String contact = contactF.getText().trim();
        if (name.isBlank() || age.isBlank() || contact.isBlank()) {
            JOptionPane.showMessageDialog(this, "Name, Age and Contact are required."); return;
        }
        Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("fullName", name);
        body.put("age", age);
        body.put("gender", genderC.getSelectedItem());
        body.put("contact", contact);
        body.put("bloodGroup", bloodF.getText().trim());
        body.put("address", addressF.getText().trim());
        body.put("ward", wardF.getText().trim());
        body.put("roomNumber", roomF.getText().trim());
        body.put("admissionDate", java.time.LocalDate.now().toString());
        body.put("status", statusC.getSelectedItem());
        body.put("emergencyNotes", notesA.getText().trim());
        if (!pidF.getText().isBlank()) body.put("patientId", pidF.getText().trim());

        String sel = (String) doctorC.getSelectedItem();
        if (sel != null && sel.contains("|")) body.put("doctorId", sel.split("\\|")[0].trim());

        SwingWorker<JsonNode, Void> w = new SwingWorker<>() {
            @Override protected JsonNode doInBackground() throws Exception {
                return patientId == null
                    ? ApiClient.createPatient(body)
                    : ApiClient.updatePatient(patientId, body);
            }
            @Override protected void done() {
                try {
                    get(); saved = true; dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(PatientDialog.this, "Error: " + ex.getMessage());
                }
            }
        };
        w.execute();
    }

    boolean isSaved() { return saved; }
}
