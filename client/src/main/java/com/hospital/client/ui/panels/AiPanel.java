package com.hospital.client.ui.panels;

import com.fasterxml.jackson.databind.JsonNode;
import com.hospital.client.api.ApiClient;
import com.hospital.client.util.Theme;
import com.hospital.client.util.UiKit;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public class AiPanel extends JPanel {

    public AiPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        JLabel title = UiKit.titleLbl("AI Clinical Features");
        header.add(title, BorderLayout.WEST);
        header.add(UiKit.badge("Doctor / Admin Only", Theme.PURPLE), BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Theme.F_SECTION);
        tabs.addTab("Ollama Setup",                buildOllamaSetupPanel());
        tabs.addTab("CaseTwin - Profile",          buildProfileTab());
        tabs.addTab("CaseTwin - CXR Annotation",   buildCXRAnnotateTab());
        tabs.addTab("CaseTwin - Compare Cases",    buildCompareTab());
        tabs.addTab("CaseTwin - Referral",         buildReferralTab());
        tabs.addTab("UniRad3s - SPOT",             buildSpotTab());
        tabs.addTab("UniRad3s - SEGMENT",          buildSegmentTab());
        tabs.addTab("UniRad3s - Clinical Report",  buildClinicalReportTab());
        tabs.addTab("UniRad3s - Patient Summary",  buildPatientSummaryTab());
        add(tabs, BorderLayout.CENTER);
    }

    // =========================================================================
    // Ollama Setup Tab
    // =========================================================================
    private JPanel buildOllamaSetupPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Theme.BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        // Status card
        JPanel statusCard = UiKit.card();
        statusCard.setLayout(new BoxLayout(statusCard, BoxLayout.Y_AXIS));
        statusCard.setAlignmentX(LEFT_ALIGNMENT);

        JLabel statusTitle = UiKit.sectionLbl("Ollama Status");
        statusTitle.setAlignmentX(LEFT_ALIGNMENT);
        statusCard.add(statusTitle);
        statusCard.add(Box.createVerticalStrut(10));

        JLabel statusLbl = UiKit.hintLbl("Checking Ollama...");
        statusLbl.setAlignmentX(LEFT_ALIGNMENT);
        statusCard.add(statusLbl);
        statusCard.add(Box.createVerticalStrut(8));

        JLabel modelLbl = UiKit.hintLbl("Model: loading...");
        modelLbl.setAlignmentX(LEFT_ALIGNMENT);
        statusCard.add(modelLbl);
        statusCard.add(Box.createVerticalStrut(12));

        JButton checkBtn = UiKit.primaryBtn("Check Ollama Status");
        checkBtn.setAlignmentX(LEFT_ALIGNMENT);
        checkBtn.addActionListener(e -> checkOllama(statusLbl, modelLbl));
        statusCard.add(checkBtn);
        panel.add(statusCard);
        panel.add(Box.createVerticalStrut(16));

        // Model selector card
        JPanel modelCard = UiKit.card();
        modelCard.setLayout(new BoxLayout(modelCard, BoxLayout.Y_AXIS));
        modelCard.setAlignmentX(LEFT_ALIGNMENT);

        JLabel modelTitle = UiKit.sectionLbl("Select AI Model");
        modelTitle.setAlignmentX(LEFT_ALIGNMENT);
        modelCard.add(modelTitle);
        modelCard.add(Box.createVerticalStrut(8));

        JLabel modelHint = UiKit.hintLbl("Text models: llama3.2, mistral, phi3, gemma2 | Vision models: llava, llama3.2-vision");
        modelHint.setAlignmentX(LEFT_ALIGNMENT);
        modelCard.add(modelHint);
        modelCard.add(Box.createVerticalStrut(10));

        String[] models = {"llama3.2", "llama3.1", "llama3", "mistral", "phi3",
                            "gemma2", "llava", "llama3.2-vision", "medllama2"};
        JComboBox<String> modelCombo = UiKit.combo(models);
        modelCombo.setAlignmentX(LEFT_ALIGNMENT);
        modelCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        modelCard.add(modelCombo);
        modelCard.add(Box.createVerticalStrut(10));

        JLabel saveStatusLbl = UiKit.hintLbl("No model saved yet.");
        saveStatusLbl.setAlignmentX(LEFT_ALIGNMENT);
        modelCard.add(saveStatusLbl);
        modelCard.add(Box.createVerticalStrut(8));

        JButton saveModelBtn = UiKit.primaryBtn("Set Model");
        saveModelBtn.setAlignmentX(LEFT_ALIGNMENT);
        saveModelBtn.addActionListener(e -> {
            String selected = (String) modelCombo.getSelectedItem();
            saveModelBtn.setEnabled(false);
            SwingWorker<JsonNode, Void> w = new SwingWorker<>() {
                @Override protected JsonNode doInBackground() throws Exception {
                    return ApiClient.setAiModel(selected);
                }
                @Override protected void done() {
                    try {
                        JsonNode r = get();
                        saveStatusLbl.setText("[OK] Model set to: " + r.path("model").asText());
                        saveStatusLbl.setForeground(Theme.OK);
                    } catch (Exception ex) {
                        saveStatusLbl.setText("Error: " + ex.getMessage());
                        saveStatusLbl.setForeground(Theme.ERR);
                    }
                    saveModelBtn.setEnabled(true);
                }
            };
            w.execute();
        });
        modelCard.add(saveModelBtn);
        panel.add(modelCard);
        panel.add(Box.createVerticalStrut(16));

        // Install instructions card
        JPanel instrCard = UiKit.card();
        instrCard.setLayout(new BoxLayout(instrCard, BoxLayout.Y_AXIS));
        instrCard.setAlignmentX(LEFT_ALIGNMENT);
        instrCard.setBackground(new Color(0xE8F4FD));

        JLabel instrTitle = UiKit.sectionLbl("How to Install & Start Ollama");
        instrTitle.setAlignmentX(LEFT_ALIGNMENT);
        instrCard.add(instrTitle);
        instrCard.add(Box.createVerticalStrut(10));

        String[] steps = {
            "1. Download Ollama from: https://ollama.com/download",
            "2. Install it (Windows/Mac/Linux supported)",
            "3. Open a terminal and run:  ollama serve",
            "4. Pull a model:  ollama pull llama3.2",
            "   For image analysis:  ollama pull llava",
            "5. Come back here, select the model above, click Set Model",
            "6. Use any AI feature tab - all runs 100% offline on your machine"
        };
        for (String step : steps) {
            JLabel lbl = UiKit.hintLbl(step);
            lbl.setAlignmentX(LEFT_ALIGNMENT);
            instrCard.add(lbl);
            instrCard.add(Box.createVerticalStrut(3));
        }
        panel.add(instrCard);

        // Auto-check on load
        checkOllama(statusLbl, modelLbl);
        return panel;
    }

    private void checkOllama(JLabel statusLbl, JLabel modelLbl) {
        statusLbl.setText("Checking...");
        statusLbl.setForeground(Theme.WARN);
        SwingWorker<JsonNode, Void> w = new SwingWorker<>() {
            @Override protected JsonNode doInBackground() throws Exception {
                return ApiClient.getAiHealth();
            }
            @Override protected void done() {
                try {
                    JsonNode r = get();
                    String ollama = r.path("ollama").asText("OFFLINE");
                    String model  = r.path("model").asText("unknown");
                    String msg    = r.path("message").asText();
                    boolean ok    = "RUNNING".equals(ollama);
                    statusLbl.setText((ok ? "[OK] " : "[X] ") + ollama + " - " + msg);
                    statusLbl.setForeground(ok ? Theme.OK : Theme.ERR);
                    modelLbl.setText("Active model: " + model);
                } catch (Exception ex) {
                    statusLbl.setText("[X] Cannot reach backend: " + ex.getMessage());
                    statusLbl.setForeground(Theme.ERR);
                }
            }
        };
        w.execute();
    }

    // =========================================================================
    // CaseTwin Tabs
    // =========================================================================
    private JPanel buildProfileTab() {
        JTextArea notesArea  = UiKit.textArea("Paste unstructured clinical notes here...", 8);
        JTextField patientId = UiKit.field("Patient ID (numbers only, optional)");
        JTextArea outputArea = UiKit.outputArea();
        JButton runBtn       = UiKit.primaryBtn("Extract Clinical Profile");

        runBtn.addActionListener(e -> {
            runBtn.setEnabled(false); runBtn.setText("Running...");
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("clinicalNotes", notesArea.getText());
            String pid = patientId.getText().trim();
            if (pid.matches("\\d+")) body.put("patientId", pid);
            SwingWorker<JsonNode, Void> w = new SwingWorker<>() {
                @Override protected JsonNode doInBackground() throws Exception {
                    return ApiClient.extractClinicalProfile(body);
                }
                @Override protected void done() {
                    try { outputArea.setText(pretty(get())); }
                    catch (Exception ex) { outputArea.setText("Error: " + ex.getMessage()); }
                    runBtn.setEnabled(true); runBtn.setText("Extract Clinical Profile");
                }
            };
            w.execute();
        });
        return buildSplit(buildStack("Clinical Notes", notesArea, "Patient ID", patientId, runBtn), outputArea);
    }

    private JPanel buildCXRAnnotateTab() {
        JTextField contextF  = UiKit.field("Clinical context (optional)");
        JTextField patientId = UiKit.field("Patient ID (numbers only, optional)");
        JLabel fileLabel     = UiKit.hintLbl("No file selected");
        JTextArea outputArea = UiKit.outputArea();
        JButton chooseBtn    = UiKit.ghostBtn("Choose X-ray Image");
        JButton runBtn       = UiKit.primaryBtn("Annotate Chest X-ray");
        final File[] sel     = {null};

        chooseBtn.addActionListener(e -> {
            File f = UiKit.chooseImage(this);
            if (f != null) { sel[0] = f; fileLabel.setText("[OK] " + f.getName()); fileLabel.setForeground(Theme.OK); }
        });
        runBtn.addActionListener(e -> {
            if (sel[0] == null) { JOptionPane.showMessageDialog(this, "Select a CXR image first."); return; }
            runBtn.setEnabled(false); runBtn.setText("Running...");
            String ctx = contextF.getText().trim();
            String pid = patientId.getText().trim();
            Long pidLong = pid.matches("\\d+") ? Long.parseLong(pid) : null;
            File img = sel[0];
            SwingWorker<JsonNode, Void> w = new SwingWorker<>() {
                @Override protected JsonNode doInBackground() throws Exception {
                    return ApiClient.annotateCXR(img, ctx, pidLong);
                }
                @Override protected void done() {
                    try { outputArea.setText(pretty(get())); }
                    catch (Exception ex) { outputArea.setText("Error: " + ex.getMessage()); }
                    runBtn.setEnabled(true); runBtn.setText("Annotate Chest X-ray");
                }
            };
            w.execute();
        });

        JPanel inp = buildImgInputPanel("X-ray Image", fileLabel, chooseBtn, contextF, patientId, runBtn);
        return buildSplit(inp, outputArea);
    }

    private JPanel buildCompareTab() {
        JTextArea current    = UiKit.textArea("Current patient case...", 5);
        JTextArea historical = UiKit.textArea("Historical twin case...", 5);
        JTextField patientId = UiKit.field("Patient ID (numbers only, optional)");
        JTextArea outputArea = UiKit.outputArea();
        JButton runBtn       = UiKit.primaryBtn("Compare Cases");

        runBtn.addActionListener(e -> {
            runBtn.setEnabled(false); runBtn.setText("Comparing...");
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("currentCase",    current.getText());
            body.put("historicalTwin", historical.getText());
            String pid = patientId.getText().trim();
            if (pid.matches("\\d+")) body.put("patientId", pid);
            SwingWorker<JsonNode, Void> w = new SwingWorker<>() {
                @Override protected JsonNode doInBackground() throws Exception { return ApiClient.compareCases(body); }
                @Override protected void done() {
                    try { outputArea.setText(pretty(get())); }
                    catch (Exception ex) { outputArea.setText("Error: " + ex.getMessage()); }
                    runBtn.setEnabled(true); runBtn.setText("Compare Cases");
                }
            };
            w.execute();
        });

        JPanel inp = new JPanel(); inp.setLayout(new BoxLayout(inp, BoxLayout.Y_AXIS));
        inp.setOpaque(false); inp.setBorder(BorderFactory.createEmptyBorder(0,0,0,12));
        inp.add(UiKit.sectionLbl("Current Patient")); inp.add(Box.createVerticalStrut(6));
        inp.add(new JScrollPane(current)); inp.add(Box.createVerticalStrut(10));
        inp.add(UiKit.sectionLbl("Historical Twin")); inp.add(Box.createVerticalStrut(6));
        inp.add(new JScrollPane(historical)); inp.add(Box.createVerticalStrut(10));
        inp.add(patientId); inp.add(Box.createVerticalStrut(10));
        inp.add(runBtn);
        return buildSplit(inp, outputArea);
    }

    private JPanel buildReferralTab() {
        JTextArea profileArea  = UiKit.textArea("Patient profile / clinical notes...", 6);
        JTextField specialistF = UiKit.field("Specialist type (e.g. Pulmonologist)");
        JTextField patientId   = UiKit.field("Patient ID (numbers only, optional)");
        JTextArea outputArea   = UiKit.outputArea();
        JButton runBtn         = UiKit.primaryBtn("Generate Referral Memo");

        runBtn.addActionListener(e -> {
            runBtn.setEnabled(false); runBtn.setText("Generating...");
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("patientProfile", profileArea.getText());
            body.put("specialist",     specialistF.getText().trim());
            String pid = patientId.getText().trim();
            if (pid.matches("\\d+")) body.put("patientId", pid);
            SwingWorker<JsonNode, Void> w = new SwingWorker<>() {
                @Override protected JsonNode doInBackground() throws Exception { return ApiClient.generateReferral(body); }
                @Override protected void done() {
                    try { outputArea.setText(pretty(get())); }
                    catch (Exception ex) { outputArea.setText("Error: " + ex.getMessage()); }
                    runBtn.setEnabled(true); runBtn.setText("Generate Referral Memo");
                }
            };
            w.execute();
        });
        return buildSplit(buildStack("Patient Profile", profileArea, "Specialist", specialistF, patientId, runBtn), outputArea);
    }

    // =========================================================================
    // UniRad3s Tabs
    // =========================================================================
    private JPanel buildSpotTab() {
        String[] modalities  = {"FLAIR","T1ce","T1","T2","CT","CXR"};
        JComboBox<String> mc = UiKit.combo(modalities);
        JTextField patientId = UiKit.field("Patient ID (numbers only, optional)");
        JLabel fileLabel     = UiKit.hintLbl("No image selected");
        JTextArea output     = UiKit.outputArea();
        JButton chooseBtn    = UiKit.ghostBtn("Choose Image");
        JButton runBtn       = UiKit.primaryBtn("Run SPOT Analysis");
        final File[] sel     = {null};

        chooseBtn.addActionListener(e -> {
            File f = UiKit.chooseImage(this);
            if (f != null) { sel[0] = f; fileLabel.setText("[OK] " + f.getName()); fileLabel.setForeground(Theme.OK); }
        });
        runBtn.addActionListener(e -> {
            if (sel[0] == null) { JOptionPane.showMessageDialog(this, "Select an image first."); return; }
            runBtn.setEnabled(false); runBtn.setText("Analysing...");
            String mod = (String) mc.getSelectedItem();
            String pid = patientId.getText().trim();
            Long pidLong = pid.matches("\\d+") ? Long.parseLong(pid) : null;
            File img = sel[0];
            SwingWorker<JsonNode, Void> w = new SwingWorker<>() {
                @Override protected JsonNode doInBackground() throws Exception { return ApiClient.spotAnalysis(img, mod, pidLong); }
                @Override protected void done() {
                    try { output.setText(pretty(get())); }
                    catch (Exception ex) { output.setText("Error: " + ex.getMessage()); }
                    runBtn.setEnabled(true); runBtn.setText("Run SPOT Analysis");
                }
            };
            w.execute();
        });

        JPanel inp = new JPanel(); inp.setLayout(new BoxLayout(inp, BoxLayout.Y_AXIS));
        inp.setOpaque(false); inp.setBorder(BorderFactory.createEmptyBorder(0,0,0,12));
        inp.add(UiKit.sectionLbl("Modality")); inp.add(Box.createVerticalStrut(6));
        inp.add(mc); inp.add(Box.createVerticalStrut(12));
        inp.add(UiKit.sectionLbl("Image")); inp.add(Box.createVerticalStrut(6));
        inp.add(fileLabel); inp.add(Box.createVerticalStrut(6));
        inp.add(chooseBtn); inp.add(Box.createVerticalStrut(10));
        inp.add(patientId); inp.add(Box.createVerticalStrut(10));
        inp.add(runBtn);
        return buildSplit(inp, output);
    }

    private JPanel buildSegmentTab() {
        JTextField lesionF   = UiKit.field("Lesion type (e.g. glioma)");
        JTextField regionF   = UiKit.field("Region (e.g. brain, lung)");
        JTextField patientId = UiKit.field("Patient ID (numbers only, optional)");
        JLabel fileLabel     = UiKit.hintLbl("No image selected");
        JTextArea output     = UiKit.outputArea();
        JButton chooseBtn    = UiKit.ghostBtn("Choose Image");
        JButton runBtn       = UiKit.primaryBtn("Run SEGMENT Guide");
        final File[] sel     = {null};

        chooseBtn.addActionListener(e -> {
            File f = UiKit.chooseImage(this);
            if (f != null) { sel[0] = f; fileLabel.setText("[OK] " + f.getName()); fileLabel.setForeground(Theme.OK); }
        });
        runBtn.addActionListener(e -> {
            if (sel[0] == null) { JOptionPane.showMessageDialog(this, "Select an image first."); return; }
            runBtn.setEnabled(false); runBtn.setText("Segmenting...");
            String pid = patientId.getText().trim();
            Long pidLong = pid.matches("\\d+") ? Long.parseLong(pid) : null;
            File img = sel[0];
            SwingWorker<JsonNode, Void> w = new SwingWorker<>() {
                @Override protected JsonNode doInBackground() throws Exception {
                    return ApiClient.segmentAnalysis(img, lesionF.getText().trim(), regionF.getText().trim(), pidLong);
                }
                @Override protected void done() {
                    try { output.setText(pretty(get())); }
                    catch (Exception ex) { output.setText("Error: " + ex.getMessage()); }
                    runBtn.setEnabled(true); runBtn.setText("Run SEGMENT Guide");
                }
            };
            w.execute();
        });
        return buildSplit(buildImgInputPanel("Image", fileLabel, chooseBtn, lesionF, regionF, patientId, runBtn), output);
    }

    private JPanel buildClinicalReportTab() {
        JTextArea findingsArea = UiKit.textArea("Known findings from SPOT/SEGMENT...", 4);
        JTextField contextF    = UiKit.field("Patient context");
        JTextField patientId   = UiKit.field("Patient ID (numbers only, optional)");
        JLabel fileLabel       = UiKit.hintLbl("No image selected");
        JTextArea output       = UiKit.outputArea();
        JButton chooseBtn      = UiKit.ghostBtn("Choose Image");
        JButton runBtn         = UiKit.primaryBtn("Generate Clinical Report");
        final File[] sel       = {null};

        chooseBtn.addActionListener(e -> {
            File f = UiKit.chooseImage(this);
            if (f != null) { sel[0] = f; fileLabel.setText("[OK] " + f.getName()); fileLabel.setForeground(Theme.OK); }
        });
        runBtn.addActionListener(e -> {
            if (sel[0] == null) { JOptionPane.showMessageDialog(this, "Select an image first."); return; }
            runBtn.setEnabled(false); runBtn.setText("Generating...");
            String pid = patientId.getText().trim();
            Long pidLong = pid.matches("\\d+") ? Long.parseLong(pid) : null;
            File img = sel[0];
            SwingWorker<JsonNode, Void> w = new SwingWorker<>() {
                @Override protected JsonNode doInBackground() throws Exception {
                    return ApiClient.clinicalReport(img, findingsArea.getText(), contextF.getText().trim(), pidLong);
                }
                @Override protected void done() {
                    try { output.setText(pretty(get())); }
                    catch (Exception ex) { output.setText("Error: " + ex.getMessage()); }
                    runBtn.setEnabled(true); runBtn.setText("Generate Clinical Report");
                }
            };
            w.execute();
        });

        JPanel inp = new JPanel(); inp.setLayout(new BoxLayout(inp, BoxLayout.Y_AXIS));
        inp.setOpaque(false); inp.setBorder(BorderFactory.createEmptyBorder(0,0,0,12));
        inp.add(UiKit.sectionLbl("Known Findings")); inp.add(Box.createVerticalStrut(6));
        inp.add(new JScrollPane(findingsArea)); inp.add(Box.createVerticalStrut(10));
        inp.add(contextF); inp.add(Box.createVerticalStrut(6));
        inp.add(patientId); inp.add(Box.createVerticalStrut(10));
        inp.add(fileLabel); inp.add(Box.createVerticalStrut(6));
        inp.add(chooseBtn); inp.add(Box.createVerticalStrut(10));
        inp.add(runBtn);
        return buildSplit(inp, output);
    }

    private JPanel buildPatientSummaryTab() {
        JTextArea reportArea = UiKit.textArea("Paste clinical report here...", 8);
        JTextField patientId = UiKit.field("Patient ID (numbers only, optional)");
        JTextArea output     = UiKit.outputArea();
        JButton runBtn       = UiKit.primaryBtn("Generate Patient Summary");

        runBtn.addActionListener(e -> {
            runBtn.setEnabled(false); runBtn.setText("Simplifying...");
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("clinicalReport", reportArea.getText());
            String pid = patientId.getText().trim();
            if (pid.matches("\\d+")) body.put("patientId", pid);
            SwingWorker<JsonNode, Void> w = new SwingWorker<>() {
                @Override protected JsonNode doInBackground() throws Exception { return ApiClient.patientSummary(body); }
                @Override protected void done() {
                    try { output.setText(pretty(get())); }
                    catch (Exception ex) { output.setText("Error: " + ex.getMessage()); }
                    runBtn.setEnabled(true); runBtn.setText("Generate Patient Summary");
                }
            };
            w.execute();
        });
        return buildSplit(buildStack("Clinical Report", reportArea, "Patient ID", patientId, runBtn), output);
    }

    // =========================================================================
    // Layout helpers
    // =========================================================================
    private JPanel buildSplit(JPanel inputPanel, JTextArea output) {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setBorder(null); split.setDividerSize(6);
        split.setDividerLocation(380); split.setOpaque(false);
        JScrollPane inpScroll = new JScrollPane(inputPanel);
        inpScroll.setBorder(null); inpScroll.setOpaque(false);
        inpScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        split.setLeftComponent(inpScroll);

        JPanel outPanel = new JPanel(new BorderLayout(0, 6));
        outPanel.setOpaque(false);
        outPanel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        outPanel.add(UiKit.sectionLbl("AI Response (Ollama - Offline)"), BorderLayout.NORTH);
        outPanel.add(UiKit.scroll(output), BorderLayout.CENTER);
        JLabel disclaimer = UiKit.hintLbl("[!] AI output is advisory only. Verify with a qualified clinician.");
        disclaimer.setForeground(Theme.WARN);
        outPanel.add(disclaimer, BorderLayout.SOUTH);
        split.setRightComponent(outPanel);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Theme.BG);
        wrapper.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        wrapper.add(split, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildStack(String l1, JComponent f1, String l2, JComponent f2, JComponent... rest) {
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false); p.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 12));
        p.add(UiKit.sectionLbl(l1)); p.add(Box.createVerticalStrut(6));
        if (f1 instanceof JTextArea) p.add(new JScrollPane(f1)); else p.add(f1);
        p.add(Box.createVerticalStrut(12));
        p.add(UiKit.sectionLbl(l2)); p.add(Box.createVerticalStrut(6));
        if (f2 instanceof JTextArea) p.add(new JScrollPane(f2)); else p.add(f2);
        for (JComponent c : rest) { p.add(Box.createVerticalStrut(10)); p.add(c); }
        return p;
    }

    private JPanel buildImgInputPanel(String imgLabel, JLabel fileLabel, JButton chooseBtn, JComponent... rest) {
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false); p.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 12));
        p.add(UiKit.sectionLbl(imgLabel)); p.add(Box.createVerticalStrut(6));
        p.add(fileLabel); p.add(Box.createVerticalStrut(6));
        p.add(chooseBtn); p.add(Box.createVerticalStrut(12));
        for (JComponent c : rest) { p.add(c); p.add(Box.createVerticalStrut(8)); }
        return p;
    }

    private String pretty(JsonNode n) {
        if (n == null) return "No response";
        // Known result field names from AiController endpoints
        String[] resultKeys = {
            "profile", "annotation", "comparison", "referralMemo",
            "spotResult", "segmentResult", "clinicalReport", "patientSummary"
        };
        for (String key : resultKeys) {
            if (n.has(key) && !n.get(key).isNull()) {
                String text = n.get(key).asText("");
                if (!text.isBlank()) return text.trim();
            }
        }
        // If an error field exists, show it
        if (n.has("error")) return "Error: " + n.get("error").asText();
        // Fallback: pretty-print entire JSON
        try { return ApiClient.MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(n); }
        catch (Exception e) { return n.toString(); }
    }
}
