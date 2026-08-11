package gui.modules;

import gui.Theme;
import utility.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ResourceManagementPanel extends JPanel {

    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField searchField;

    public ResourceManagementPanel() {
        setLayout(new BorderLayout(0, 15));
        setBackground(Theme.PRIMARY);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel("Resource Management");
        title.setForeground(Theme.GOLD);
        title.setFont(Theme.HEADER_FONT);
        add(title, BorderLayout.NORTH);

        String[] cols = {"Material ID", "Project", "Material Name", "Price", "Quantity", "Total Value"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        styleTable(table);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBackground(Theme.SECONDARY);

        // Search bar
        searchField = new JTextField();
        searchField.setBackground(Theme.PRIMARY);
        searchField.setForeground(Theme.WHITE);
        searchField.setCaretColor(Theme.WHITE);
        searchField.setFont(Theme.BODY_FONT);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.GOLD, 1),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        searchField.putClientProperty("JTextField.placeholderText", "Search by material ID, project, or material name...");
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });

        JPanel centerPanel = new JPanel(new BorderLayout(0, 8));
        centerPanel.setOpaque(false);
        centerPanel.add(searchField, BorderLayout.NORTH);
        centerPanel.add(scroll, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnPanel.setBackground(Theme.PRIMARY);
        JButton addBtn    = makeBtn("Allocate Resource");
        JButton useBtn    = makeBtn("Use Resource");
        JButton deleteBtn = makeBtn("Delete Resource");
        JButton refreshBtn = makeBtn("Refresh");
        btnPanel.add(addBtn); btnPanel.add(useBtn); btnPanel.add(deleteBtn); btnPanel.add(refreshBtn);
        add(btnPanel, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> showAllocateDialog());
        useBtn.addActionListener(e -> showUseDialog());
        deleteBtn.addActionListener(e -> deleteSelected());
        refreshBtn.addActionListener(e -> loadResources());

        loadResources();
    }

    private void filterTable() {
        String query = searchField.getText().trim().toLowerCase();
        tableModel.setRowCount(0);
        Connection conn = DBConnection.getConnection();
        if (conn == null) return;
        String sql = "SELECT r.material_id, p.project_name, r.material_name, r.price, r.quantity " +
                     "FROM resources r JOIN projects p ON r.project_id = p.project_id";
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String mid   = rs.getString("material_id");
                String proj  = rs.getString("project_name");
                String mname = rs.getString("material_name");
                double price = rs.getDouble("price");
                int qty      = rs.getInt("quantity");
                if (query.isEmpty() || mid.toLowerCase().contains(query) || proj.toLowerCase().contains(query)
                        || mname.toLowerCase().contains(query)) {
                    tableModel.addRow(new Object[]{mid, proj, mname,
                        String.format("%.2f", price), qty, String.format("%.2f", price * qty)});
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading resources: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadResources() {
        searchField.setText("");
        filterTable();
    }

    private void showAllocateDialog() {
        Connection conn = DBConnection.getConnection();
        if (conn == null) return;
        JComboBox<String> projectBox = new JComboBox<>();
        java.util.Map<String, String> projectMap = new java.util.LinkedHashMap<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT project_id, project_name FROM projects");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String display = rs.getString("project_name") + " (" + rs.getString("project_id") + ")";
                projectMap.put(display, rs.getString("project_id"));
                projectBox.addItem(display);
            }
        } catch (SQLException e) { showDbError(e); return; }

        if (projectBox.getItemCount() == 0) {
            JOptionPane.showMessageDialog(this, "No projects exist. Create a project first.", "No Projects", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JTextField midField   = new JTextField();
        JTextField nameField  = new JTextField();
        JTextField priceField = new JTextField();
        JTextField qtyField   = new JTextField();

        Object[] fields = {
            "Project:", projectBox,
            "Material ID:", midField,
            "Material Name:", nameField,
            "Price:", priceField,
            "Quantity:", qtyField
        };

        int result = JOptionPane.showConfirmDialog(this, fields, "Allocate Resource", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        String pid  = projectMap.get((String) projectBox.getSelectedItem());
        String mid  = midField.getText().trim();
        String name = nameField.getText().trim();

        if (mid.isEmpty() || name.isEmpty() || priceField.getText().trim().isEmpty() || qtyField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double price; int qty;
        try { price = Double.parseDouble(priceField.getText().trim()); if (price < 0) throw new NumberFormatException(); }
        catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Invalid price.", "Error", JOptionPane.WARNING_MESSAGE); return; }
        try { qty = Integer.parseInt(qtyField.getText().trim()); if (qty <= 0) throw new NumberFormatException(); }
        catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Quantity must be a positive integer.", "Error", JOptionPane.WARNING_MESSAGE); return; }

        // Check duplicate material ID
        try (PreparedStatement chk = conn.prepareStatement("SELECT 1 FROM resources WHERE material_id=?")) {
            chk.setString(1, mid);
            if (chk.executeQuery().next()) {
                JOptionPane.showMessageDialog(this, "Material ID already exists.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (SQLException e) { showDbError(e); return; }

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO resources (material_id, project_id, material_name, price, quantity) VALUES (?,?,?,?,?)")) {
            ps.setString(1, mid); ps.setString(2, pid); ps.setString(3, name);
            ps.setDouble(4, price); ps.setInt(5, qty);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Resource allocated!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadResources();
        } catch (SQLException e) { showDbError(e); }
    }

    private void showUseDialog() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a resource to use.", "No Selection", JOptionPane.WARNING_MESSAGE); return; }
        String mid = (String) tableModel.getValueAt(row, 0);
        int currentQty = (int) tableModel.getValueAt(row, 4);
        String projName = (String) tableModel.getValueAt(row, 1);

        String qtyStr = JOptionPane.showInputDialog(this, "Current stock: " + currentQty + "\nQuantity to use:");
        if (qtyStr == null || qtyStr.trim().isEmpty()) return;
        int qty;
        try { qty = Integer.parseInt(qtyStr.trim()); if (qty <= 0) throw new NumberFormatException(); }
        catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Invalid quantity.", "Error", JOptionPane.WARNING_MESSAGE); return; }
        if (qty > currentQty) { JOptionPane.showMessageDialog(this, "Not enough stock.", "Error", JOptionPane.WARNING_MESSAGE); return; }

        Connection conn = DBConnection.getConnection();
        if (conn == null) return;

        // Get project ID and price, then check budget
        try {
            String infoSql = "SELECT r.price, r.project_id, p.budget FROM resources r JOIN projects p ON r.project_id=p.project_id WHERE r.material_id=?";
            double price; String pid; double budget;
            try (PreparedStatement ps = conn.prepareStatement(infoSql)) {
                ps.setString(1, mid);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) { JOptionPane.showMessageDialog(this, "Resource not found.", "Error", JOptionPane.ERROR_MESSAGE); return; }
                price = rs.getDouble("price"); pid = rs.getString("project_id"); budget = rs.getDouble("budget");
            }

            double cost = qty * price;
            if (budget < cost) {
                JOptionPane.showMessageDialog(this, String.format("Not enough project budget.\nCost: %.2f | Available: %.2f", cost, budget), "Budget Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Deduct stock
            try (PreparedStatement ps = conn.prepareStatement("UPDATE resources SET quantity = quantity - ? WHERE material_id = ?")) {
                ps.setInt(1, qty); ps.setString(2, mid); ps.executeUpdate();
            }
            // Deduct budget
            try (PreparedStatement ps = conn.prepareStatement("UPDATE projects SET budget = budget - ? WHERE project_id = ?")) {
                ps.setDouble(1, cost); ps.setString(2, pid); ps.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, String.format("Resource used!\nCost deducted: %.2f\nRemaining budget: %.2f", cost, budget - cost), "Success", JOptionPane.INFORMATION_MESSAGE);
            loadResources();
        } catch (SQLException e) { showDbError(e); }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a resource to delete.", "No Selection", JOptionPane.WARNING_MESSAGE); return; }
        String mid = (String) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete resource \"" + mid + "\"?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        Connection conn = DBConnection.getConnection();
        if (conn == null) return;
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM resources WHERE material_id = ?")) {
            ps.setString(1, mid); ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Resource deleted!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadResources();
        } catch (SQLException e) { showDbError(e); }
    }

    private void styleTable(JTable t) {
        t.setBackground(Theme.SECONDARY); t.setForeground(Theme.WHITE);
        t.setFont(Theme.BODY_FONT); t.setRowHeight(28); t.setGridColor(Theme.PRIMARY);
        t.getTableHeader().setBackground(Theme.PRIMARY); t.getTableHeader().setForeground(Theme.GOLD);
        t.getTableHeader().setFont(Theme.BODY_FONT);
        t.setSelectionBackground(Theme.GOLD); t.setSelectionForeground(Theme.PRIMARY);
    }

    private JButton makeBtn(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(Theme.GOLD); btn.setForeground(Theme.PRIMARY);
        btn.setFont(Theme.BODY_FONT); btn.setFocusPainted(false);
        btn.setBorderPainted(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void showDbError(SQLException ex) {
        JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}
