package gui.modules;

import gui.Theme;
import utility.DBConnection;
import utility.ValidationHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ProjectManagementPanel extends JPanel {

    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField searchField;

    public ProjectManagementPanel() {
        setLayout(new BorderLayout(0, 15));
        setBackground(Theme.PRIMARY);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel("Project Management");
        title.setForeground(Theme.GOLD);
        title.setFont(Theme.HEADER_FONT);
        add(title, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Name", "Location", "Start Date", "End Date", "Budget", "Status"};
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
        searchField.putClientProperty("JTextField.placeholderText", "Search by ID, name, location, or status...");
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

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnPanel.setBackground(Theme.PRIMARY);
        JButton addBtn    = makeBtn("Add Project");
        JButton updateBtn = makeBtn("Update Project");
        JButton deleteBtn = makeBtn("Delete Project");
        JButton refreshBtn = makeBtn("Refresh");
        btnPanel.add(addBtn);
        btnPanel.add(updateBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(refreshBtn);
        add(btnPanel, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> showAddDialog());
        updateBtn.addActionListener(e -> showUpdateDialog());
        deleteBtn.addActionListener(e -> deleteSelected());
        refreshBtn.addActionListener(e -> loadProjects());

        loadProjects();
    }

    private void filterTable() {
        String query = searchField.getText().trim().toLowerCase();
        tableModel.setRowCount(0);
        Connection conn = DBConnection.getConnection();
        if (conn == null) return;
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM projects");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String id     = rs.getString("project_id");
                String name   = rs.getString("project_name");
                String loc    = rs.getString("location");
                String sDate  = rs.getString("start_date");
                String eDate  = rs.getString("end_date");
                String budget = String.format("%.2f", rs.getDouble("budget"));
                String status = rs.getString("status");
                if (query.isEmpty() || id.toLowerCase().contains(query) || name.toLowerCase().contains(query)
                        || loc.toLowerCase().contains(query) || status.toLowerCase().contains(query)) {
                    tableModel.addRow(new Object[]{id, name, loc, sDate, eDate, budget, status});
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading projects: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadProjects() {
        searchField.setText("");
        filterTable();
    }

    private void showAddDialog() {
        JTextField idField    = new JTextField();
        JTextField nameField  = new JTextField();
        JTextField locField   = new JTextField();
        JTextField sDateField = new JTextField("YYYY-MM-DD");
        JTextField eDateField = new JTextField("YYYY-MM-DD");
        JTextField budgetField = new JTextField();

        Object[] fields = {
            "Project ID:",    idField,
            "Project Name:",  nameField,
            "Location:",      locField,
            "Start Date:",    sDateField,
            "End Date:",      eDateField,
            "Budget:",        budgetField
        };

        int result = JOptionPane.showConfirmDialog(this, fields, "Add Project", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        String id     = idField.getText().trim();
        String name   = nameField.getText().trim();
        String loc    = locField.getText().trim();
        String sDate  = sDateField.getText().trim();
        String eDate  = eDateField.getText().trim();
        String budStr = budgetField.getText().trim();

        if (id.isEmpty() || name.isEmpty() || loc.isEmpty() || sDate.isEmpty() || eDate.isEmpty() || budStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!ValidationHelper.isValidDate(sDate) || !ValidationHelper.isValidDate(eDate)) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Use YYYY-MM-DD.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!ValidationHelper.isEndDateValid(sDate, eDate)) {
            JOptionPane.showMessageDialog(this, "End date cannot be before start date.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        double budget;
        try { budget = Double.parseDouble(budStr); if (budget < 0) throw new NumberFormatException(); }
        catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Budget must be a positive number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Connection conn = DBConnection.getConnection();
        if (conn == null) return;
        // Check duplicate ID
        try (PreparedStatement chk = conn.prepareStatement("SELECT 1 FROM projects WHERE project_id=?")) {
            chk.setString(1, id);
            if (chk.executeQuery().next()) {
                JOptionPane.showMessageDialog(this, "Project ID already exists.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (SQLException ex) { showDbError(ex); return; }

        String sql = "INSERT INTO projects (project_id, project_name, location, start_date, end_date, budget, status) VALUES (?,?,?,?,?,?,'Ongoing')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id); ps.setString(2, name); ps.setString(3, loc);
            ps.setString(4, sDate); ps.setString(5, eDate); ps.setDouble(6, budget);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Project created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadProjects();
        } catch (SQLException ex) { showDbError(ex); }
    }

    private void showUpdateDialog() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a project to update.", "No Selection", JOptionPane.WARNING_MESSAGE); return; }
        String id = (String) tableModel.getValueAt(row, 0);

        String[] options = {"Name", "Location", "Budget", "Status"};
        String field = (String) JOptionPane.showInputDialog(this, "Which field to update?", "Update Project",
                JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        if (field == null) return;

        String newVal;
        if ("Status".equals(field)) {
            String[] statuses = {"Ongoing", "On Hold", "Completed", "Cancelled"};
            newVal = (String) JOptionPane.showInputDialog(this, "Select new status:", "Update Status",
                    JOptionPane.PLAIN_MESSAGE, null, statuses, statuses[0]);
        } else {
            newVal = JOptionPane.showInputDialog(this, "New " + field + ":");
        }
        if (newVal == null || newVal.trim().isEmpty()) return;
        newVal = newVal.trim();

        String col;
        switch (field) {
            case "Name":    col = "project_name"; break;
            case "Location":col = "location"; break;
            case "Budget":
                try { double d = Double.parseDouble(newVal); if(d<0) throw new NumberFormatException(); }
                catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Invalid budget.", "Error", JOptionPane.WARNING_MESSAGE); return; }
                col = "budget"; break;
            case "Status":  col = "status"; break;
            default: return;
        }

        Connection conn = DBConnection.getConnection();
        if (conn == null) return;
        try (PreparedStatement ps = conn.prepareStatement("UPDATE projects SET " + col + " = ? WHERE project_id = ?")) {
            if ("budget".equals(col)) ps.setDouble(1, Double.parseDouble(newVal));
            else ps.setString(1, newVal);
            ps.setString(2, id);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Project updated!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadProjects();
        } catch (SQLException ex) { showDbError(ex); }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a project to delete.", "No Selection", JOptionPane.WARNING_MESSAGE); return; }
        String id = (String) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete project \"" + id + "\"? This also deletes its tasks and resources.", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        Connection conn = DBConnection.getConnection();
        if (conn == null) return;
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM projects WHERE project_id = ?")) {
            ps.setString(1, id);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Project deleted!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadProjects();
        } catch (SQLException ex) { showDbError(ex); }
    }

    private void styleTable(JTable t) {
        t.setBackground(Theme.SECONDARY); t.setForeground(Theme.WHITE);
        t.setFont(Theme.BODY_FONT); t.setRowHeight(28);
        t.setGridColor(Theme.PRIMARY);
        t.getTableHeader().setBackground(Theme.PRIMARY);
        t.getTableHeader().setForeground(Theme.GOLD);
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
