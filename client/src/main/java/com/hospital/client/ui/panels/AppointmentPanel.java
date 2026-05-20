package com.hospital.client.ui.panels;

import com.fasterxml.jackson.databind.JsonNode;
import com.hospital.client.api.ApiClient;
import com.hospital.client.util.Theme;
import com.hospital.client.util.UiKit;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;

public class AppointmentPanel extends JPanel {

    private final JTable table;
    private final DefaultTableModel model;
    private final JLabel statusLbl;

    private static final String[] COLS = {
        "ID", "Patient", "Doctor", "Date", "Time", "Department", "Reason", "Status"
    };

    public AppointmentPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        // -- Header ------------------------------------------------------------
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        header.add(UiKit.titleLbl("Appointments"), BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton todayBtn   = UiKit.ghostBtn("Today");
        JButton allBtn     = UiKit.ghostBtn("All");
        JButton refreshBtn = UiKit.ghostBtn("Refresh");
        JButton addBtn     = UiKit.successBtn("+ New Appointment");
        todayBtn.addActionListener(e -> loadToday());
        allBtn.addActionListener(e -> loadAll());
        refreshBtn.addActionListener(e -> loadAll());
        addBtn.addActionListener(e -> openDialog(null));
        actions.add(todayBtn); actions.add(allBtn); actions.add(refreshBtn); actions.add(addBtn);
        header.add(actions, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // -- Table -------------------------------------------------------------
        table = UiKit.styledTable(COLS);
        model = (DefaultTableModel) table.getModel();
        add(UiKit.scroll(table), BorderLayout.CENTER);

        // -- Bottom bar --------------------------------------------------------
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        bottomBar.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnGroup.setOpaque(false);
        JButton editBtn     = UiKit.primaryBtn("Edit");
        JButton completeBtn = UiKit.successBtn("Mark Complete");
        JButton cancelBtn   = UiKit.warningBtn("Cancel Appt.");
        JButton deleteBtn   = UiKit.dangerBtn("Delete");
        editBtn.addActionListener(e -> openDialog(getSelectedId()));
        completeBtn.addActionListener(e -> updateStatus("COMPLETED"));
        cancelBtn.addActionListener(e -> updateStatus("CANCELLED"));
        deleteBtn.addActionListener(e -> deleteSelected());
        btnGroup.add(editBtn); btnGroup.add(completeBtn);
        btnGroup.add(cancelBtn); btnGroup.add(deleteBtn);

        statusLbl = UiKit.hintLbl("Loading...");
        bottomBar.add(btnGroup,  BorderLayout.WEST);
        bottomBar.add(statusLbl, BorderLayout.EAST);
        add(bottomBar, BorderLayout.SOUTH);

        loadAll();
    }

    private void loadAll() {
        statusLbl.setText("Loading...");
        SwingWorker<JsonNode, Void> w = new SwingWorker<>() {
            @Override protected JsonNode doInBackground() throws Exception {
                return ApiClient.getAppointments();
            }
            @Override protected void done() { try {
                populateTable(get()); 
            } catch (Exception e) {
                // TODO: handle exception
            }}
        };
        w.execute();
    }

    private void loadToday() {
        statusLbl.setText("Loading today...");
        SwingWorker<JsonNode, Void> w = new SwingWorker<>() {
            @Override protected JsonNode doInBackground() throws Exception {
                return ApiClient.getTodayAppointments();
            }
            @Override protected void done() { try {
                populateTable(get());
            } catch (Exception e) {
                // TODO: handle exception
            } }
        };
        w.execute();
    }

    private void populateTable(JsonNode data) {
        try {
            model.setRowCount(0);
            if (data != null && data.isArray()) {
                for (JsonNode a : data) {
                    String patient = a.path("patient").path("fullName").asText("N/A");
                    String doctor  = a.path("doctor").path("fullName").asText("N/A");
                    String status  = a.path("status").asText();
                    model.addRow(new Object[]{
                        a.path("id").asLong(),
                        patient, doctor,
                        a.path("appointmentDate").asText(),
                        a.path("appointmentTime").asText(),
                        a.path("department").asText(),
                        a.path("reason").asText(),
                        status
                    });
                }
            }
            // Colour-code status column
            table.getColumnModel().getColumn(7).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                    Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                    String s = v != null ? v.toString() : "";
                    if (!sel) c.setForeground(switch (s) {
                        case "SCHEDULED"  -> Theme.INFO;
                        case "COMPLETED"  -> Theme.OK;
                        case "CANCELLED"  -> Theme.ERR;
                        default           -> Theme.TEXT;
                    });
                    return c;
                }
            });
            statusLbl.setText(model.getRowCount() + " appointment(s)");
        } catch (Exception ex) {
            statusLbl.setText("Error: " + ex.getMessage());
        }
    }

    private Long getSelectedId() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select an appointment first."); return null; }
        return (Long) model.getValueAt(row, 0);
    }

    private void updateStatus(String status) {
        Long id = getSelectedId();
        if (id == null) return;
        SwingWorker<Void, Void> w = new SwingWorker<>() {
            @Override protected Void doInBackground() throws Exception {
                ApiClient.updateAppointmentStatus(id, status); return null;
            }
            @Override protected void done() { loadAll(); }
        };
        w.execute();
    }

    private void deleteSelected() {
        Long id = getSelectedId();
        if (id == null) return;
        int c = JOptionPane.showConfirmDialog(this, "Delete this appointment?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (c != JOptionPane.YES_OPTION) return;
        SwingWorker<Void, Void> w = new SwingWorker<>() {
            @Override protected Void doInBackground() throws Exception {
                ApiClient.deleteAppointment(id); return null;
            }
            @Override protected void done() { loadAll(); }
        };
        w.execute();
    }

    private void openDialog(Long apptId) {
        AppointmentDialog dlg = new AppointmentDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), apptId);
        dlg.setVisible(true);
        if (dlg.isSaved()) loadAll();
    }
}

// -- Appointment Dialog --------------------------------------------------------
class AppointmentDialog extends JDialog {

    private boolean saved = false;
    private final Long apptId;

    private JComboBox<String> patientC, doctorC, statusC;
    private JTextField dateF, timeF, deptF, reasonF;
    private JTextArea notesA;

    AppointmentDialog(Frame parent, Long apptId) {
        super(parent, apptId == null ? "New Appointment" : "Edit Appointment", true);
        this.apptId = apptId;
        setSize(520, 560);
        setLocationRelativeTo(parent);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.CARD);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.NAVY);
        header.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        JLabel title = new JLabel(apptId == null ? "Schedule New Appointment" : "Edit Appointment");
        title.setFont(Theme.F_SECTION); title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);
        root.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.CARD);
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(6, 4, 6, 4);

        patientC = new JComboBox<>(); patientC.addItem("-- Select Patient --");
        doctorC  = new JComboBox<>(); doctorC.addItem("-- Select Doctor --");
        statusC  = new JComboBox<>(new String[]{"SCHEDULED","COMPLETED","CANCELLED"});
        dateF    = UiKit.field("YYYY-MM-DD");
        timeF    = UiKit.field("HH:MM (e.g. 09:30)");
        deptF    = UiKit.field("Department");
        reasonF  = UiKit.field("Reason for visit");
        notesA   = UiKit.textArea("Additional notes...", 3);

        loadDropdowns();

        addRow(form, gbc, "Patient *",    patientC, 0);
        addRow(form, gbc, "Doctor *",     doctorC,  1);
        addRow(form, gbc, "Date *",       dateF,    2);
        addRow(form, gbc, "Time *",       timeF,    3);
        addRow(form, gbc, "Department",   deptF,    4);
        addRow(form, gbc, "Reason",       reasonF,  5);
        addRow(form, gbc, "Status",       statusC,  6);
        gbc.gridx=0; gbc.gridy=7; form.add(UiKit.bodyLbl("Notes"), gbc);
        gbc.gridx=1; form.add(new JScrollPane(notesA), gbc);

        root.add(new JScrollPane(form), BorderLayout.CENTER);

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
    }

    private void addRow(JPanel p, GridBagConstraints gbc, String label, JComponent field, int row) {
        gbc.gridx=0; gbc.gridy=row; gbc.gridwidth=1; gbc.weightx=0.3;
        p.add(UiKit.bodyLbl(label), gbc);
        gbc.gridx=1; gbc.weightx=0.7;
        p.add(field, gbc);
    }

    private void loadDropdowns() {
        SwingWorker<Void, Void> w = new SwingWorker<>() {
            JsonNode patients, doctors;
            @Override protected Void doInBackground() throws Exception {
                patients = ApiClient.getPatients();
                doctors  = ApiClient.getDoctors();
                return null;
            }
            @Override protected void done() {
                try {
                    if (patients != null && patients.isArray())
                        for (JsonNode p : patients)
                            patientC.addItem(p.path("id").asText() + " | " + p.path("fullName").asText());
                    if (doctors != null && doctors.isArray())
                        for (JsonNode d : doctors)
                            doctorC.addItem(d.path("id").asText() + " | " + d.path("fullName").asText());
                } catch (Exception ignored) {}
            }
        };
        w.execute();
    }

    private void save() {
        String patSel = (String) patientC.getSelectedItem();
        String docSel = (String) doctorC.getSelectedItem();
        String date   = dateF.getText().trim();
        String time   = timeF.getText().trim();
        if (patSel == null || !patSel.contains("|") || docSel == null || !docSel.contains("|") || date.isBlank() || time.isBlank()) {
            JOptionPane.showMessageDialog(this, "Patient, Doctor, Date and Time are required."); return;
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("patientId", patSel.split("\\|")[0].trim());
        body.put("doctorId",  docSel.split("\\|")[0].trim());
        body.put("appointmentDate", date);
        body.put("appointmentTime", time.length() == 5 ? time + ":00" : time);
        body.put("department", deptF.getText().trim());
        body.put("reason",     reasonF.getText().trim());
        body.put("notes",      notesA.getText().trim());
        body.put("status",     statusC.getSelectedItem());
        body.put("createdById", com.hospital.client.model.Session.get().getUserId());

        SwingWorker<JsonNode, Void> w = new SwingWorker<>() {
            @Override protected JsonNode doInBackground() throws Exception {
                return apptId == null
                    ? ApiClient.createAppointment(body)
                    : ApiClient.updateAppointment(apptId, body);
            }
            @Override protected void done() {
                try { get(); saved = true; dispose(); }
                catch (Exception ex) { JOptionPane.showMessageDialog(AppointmentDialog.this, "Error: " + ex.getMessage()); }
            }
        };
        w.execute();
    }

    boolean isSaved() { return saved; }
}
