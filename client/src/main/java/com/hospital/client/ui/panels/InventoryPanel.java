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

public class InventoryPanel extends JPanel {

    private final JTable table;
    private final DefaultTableModel model;
    private final JTextField searchField;
    private final JLabel statusLbl;

    private static final String[] COLS = {
        "ID", "Medicine", "Batch", "Qty", "Unit", "Supplier", "Expiry", "Reorder Lvl", "Buy Price", "Sell Price", "Category"
    };

    public InventoryPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        // -- Header ------------------------------------------------------------
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        header.add(UiKit.titleLbl("Inventory Management"), BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        searchField = UiKit.field("Search medicines...");
        searchField.setPreferredSize(new Dimension(200, 34));
        JButton searchBtn  = UiKit.primaryBtn("Search");
        JButton allBtn     = UiKit.ghostBtn("All");
        JButton lowBtn     = UiKit.warningBtn("Low Stock");
        JButton refreshBtn = UiKit.ghostBtn("Refresh");
        searchBtn.addActionListener(e -> loadSearch(searchField.getText()));
        allBtn.addActionListener(e -> loadAll());
        lowBtn.addActionListener(e -> loadLowStock());
        refreshBtn.addActionListener(e -> loadAll());
        actions.add(searchField); actions.add(searchBtn); actions.add(allBtn);
        actions.add(lowBtn); actions.add(refreshBtn);

        if (Session.get().canWriteInventory()) {
            JButton addBtn = UiKit.successBtn("+ Add Item");
            addBtn.addActionListener(e -> openDialog(null));
            actions.add(addBtn);
        }
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

        if (Session.get().canWriteInventory()) {
            JButton editBtn   = UiKit.primaryBtn("Edit");
            JButton deleteBtn = UiKit.dangerBtn("Delete");
            editBtn.addActionListener(e -> openDialog(getSelectedId()));
            deleteBtn.addActionListener(e -> deleteSelected());
            btnGroup.add(editBtn); btnGroup.add(deleteBtn);
        } else {
            btnGroup.add(UiKit.hintLbl("View only - contact Admin to modify inventory"));
        }

        statusLbl = UiKit.hintLbl("Loading inventory...");
        bottomBar.add(btnGroup,  BorderLayout.WEST);
        bottomBar.add(statusLbl, BorderLayout.EAST);
        add(bottomBar, BorderLayout.SOUTH);

        loadAll();
    }

    private void loadAll() {
        statusLbl.setText("Loading...");
        SwingWorker<JsonNode, Void> w = new SwingWorker<>() {
            @Override protected JsonNode doInBackground() throws Exception { return ApiClient.getInventory(); }
            @Override protected void done() { try {
                populateTable(get()); 
            } catch (Exception e) {
                // TODO: handle exception
            }}
        };
        w.execute();
    }

    private void loadSearch(String q) {
        if (q.isBlank()) { loadAll(); return; }
        SwingWorker<JsonNode, Void> w = new SwingWorker<>() {
            @Override protected JsonNode doInBackground() throws Exception { return ApiClient.searchInventory(q); }
            @Override protected void done() { try {
                populateTable(get()); 
            } catch (Exception e) {
                // TODO: handle exception
            }}
        };
        w.execute();
    }

    private void loadLowStock() {
        statusLbl.setText("Loading low stock...");
        SwingWorker<JsonNode, Void> w = new SwingWorker<>() {
            @Override protected JsonNode doInBackground() throws Exception { return ApiClient.getLowStock(); }
            @Override protected void done() { try {
                populateTable(get());
            } catch (Exception e) {
                // TODO: handle exception
            } statusLbl.setText("Showing low-stock items"); }
        };
        w.execute();
    }

    private void populateTable(JsonNode data) {
        try {
            model.setRowCount(0);
            if (data != null && data.isArray()) {
                for (JsonNode item : data) {
                    model.addRow(new Object[]{
                        item.path("id").asLong(),
                        item.path("medicineName").asText(),
                        item.path("batchNumber").asText(),
                        item.path("quantity").asInt(),
                        item.path("unit").asText(),
                        item.path("supplier").asText(),
                        item.path("expiryDate").asText(),
                        item.path("reorderLevel").asInt(),
                        item.path("purchasePrice").asText(),
                        item.path("sellingPrice").asText(),
                        item.path("category").asText()
                    });
                }
            }
            // Colour-code quantity column for low stock
            table.getColumnModel().getColumn(3).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                    Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                    if (!sel) {
                        try {
                            int qty      = Integer.parseInt(v.toString());
                            int reorder  = Integer.parseInt(model.getValueAt(row, 7).toString());
                            c.setForeground(qty <= reorder ? Theme.ERR : Theme.OK);
                        } catch (Exception ignored) {}
                    }
                    return c;
                }
            });
            statusLbl.setText(model.getRowCount() + " item(s)");
        } catch (Exception ex) {
            statusLbl.setText("Error: " + ex.getMessage());
        }
    }

    private Long getSelectedId() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select an item first."); return null; }
        return (Long) model.getValueAt(row, 0);
    }

    private void deleteSelected() {
        Long id = getSelectedId();
        if (id == null) return;
        int c = JOptionPane.showConfirmDialog(this, "Delete this inventory item?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (c != JOptionPane.YES_OPTION) return;
        SwingWorker<Void, Void> w = new SwingWorker<>() {
            @Override protected Void doInBackground() throws Exception { ApiClient.deleteInventory(id); return null; }
            @Override protected void done() { loadAll(); }
        };
        w.execute();
    }

    private void openDialog(Long itemId) {
        InventoryDialog dlg = new InventoryDialog((Frame) SwingUtilities.getWindowAncestor(this), itemId);
        dlg.setVisible(true);
        if (dlg.isSaved()) loadAll();
    }
}

// -- Inventory Dialog ----------------------------------------------------------
class InventoryDialog extends JDialog {

    private boolean saved = false;
    private final Long itemId;

    private JTextField nameF, batchF, qtyF, unitF, supplierF, expiryF, reorderF, buyF, sellF, catF;

    InventoryDialog(Frame parent, Long itemId) {
        super(parent, itemId == null ? "Add Inventory Item" : "Edit Inventory Item", true);
        this.itemId = itemId;
        setSize(500, 560);
        setLocationRelativeTo(parent);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.CARD);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.NAVY);
        header.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        JLabel title = new JLabel(itemId == null ? "Add New Inventory Item" : "Edit Item");
        title.setFont(Theme.F_SECTION); title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);
        root.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.CARD);
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(5, 4, 5, 4);

        nameF    = UiKit.field("Medicine name");
        batchF   = UiKit.field("Batch number");
        qtyF     = UiKit.field("Quantity");
        unitF    = UiKit.field("e.g. Tablets, Bottles");
        supplierF= UiKit.field("Supplier name");
        expiryF  = UiKit.field("YYYY-MM-DD");
        reorderF = UiKit.field("Reorder level (e.g. 10)");
        buyF     = UiKit.field("Purchase price");
        sellF    = UiKit.field("Selling price");
        catF     = UiKit.field("Category");

        String[][] rows = {
            {"Medicine Name *", null}, {"Batch Number", null}, {"Quantity *",    null},
            {"Unit",            null}, {"Supplier",    null},  {"Expiry Date",   null},
            {"Reorder Level",   null}, {"Buy Price",   null},  {"Sell Price",    null},
            {"Category",        null}
        };
        JTextField[] fields = {nameF,batchF,qtyF,unitF,supplierF,expiryF,reorderF,buyF,sellF,catF};
        for (int i = 0; i < fields.length; i++) {
            gbc.gridx=0; gbc.gridy=i; gbc.weightx=0.35; form.add(UiKit.bodyLbl(rows[i][0]), gbc);
            gbc.gridx=1; gbc.weightx=0.65; form.add(fields[i], gbc);
        }

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

        if (itemId != null) loadExisting();
    }

    private void loadExisting() {
        SwingWorker<JsonNode, Void> w = new SwingWorker<>() {
            @Override protected JsonNode doInBackground() throws Exception {
                return ApiClient.getInventory(); // simplified - load all and filter
            }
            @Override protected void done() { /* pre-populate if needed */ }
        };
        w.execute();
    }

    private void setField(JTextField f, String val) {
        if (val != null && !val.isBlank()) { f.setText(val); f.setForeground(Theme.TEXT); }
    }

    private void save() {
        if (nameF.getText().isBlank() || qtyF.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Medicine name and quantity are required."); return;
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("medicineName",  nameF.getText().trim());
        body.put("batchNumber",   batchF.getText().trim());
        body.put("quantity",      qtyF.getText().trim());
        body.put("unit",          unitF.getText().trim());
        body.put("supplier",      supplierF.getText().trim());
        body.put("expiryDate",    expiryF.getText().trim().isBlank() ? null : expiryF.getText().trim());
        body.put("reorderLevel",  reorderF.getText().trim().isBlank() ? "10" : reorderF.getText().trim());
        body.put("purchasePrice", buyF.getText().trim().isBlank() ? null : buyF.getText().trim());
        body.put("sellingPrice",  sellF.getText().trim().isBlank() ? null : sellF.getText().trim());
        body.put("category",      catF.getText().trim());

        SwingWorker<JsonNode, Void> w = new SwingWorker<>() {
            @Override protected JsonNode doInBackground() throws Exception {
                return itemId == null
                    ? ApiClient.createInventory(body)
                    : ApiClient.updateInventory(itemId, body);
            }
            @Override protected void done() {
                try { get(); saved = true; dispose(); }
                catch (Exception ex) { JOptionPane.showMessageDialog(InventoryDialog.this, "Error: " + ex.getMessage()); }
            }
        };
        w.execute();
    }

    boolean isSaved() { return saved; }
}
