package gui.modules;

import gui.Theme;
import utility.DBConnection;
import utility.PasswordUtil;
import utility.ValidationHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class WorkforceManagementPanel extends JPanel {

    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField searchField;

    public WorkforceManagementPanel() {
        setLayout(new BorderLayout(0, 15));
        setBackground(Theme.PRIMARY);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel("Workforce Management");
        title.setForeground(Theme.GOLD);
        title.setFont(Theme.HEADER_FONT);
        add(title, BorderLayout.NORTH);

        String[] cols = {"Worker ID", "Name", "Position", "Email", "Assigned Tasks", "Status"};
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
        searchField.putClientProperty("JTextField.placeholderText", "Search by name, ID, position, email, or status...");
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
        JButton addBtn      = makeBtn("Add Worker");
        JButton assignBtn   = makeBtn("Update Assignment");
        JButton removeBtn   = makeBtn("Remove Worker");
        JButton refreshBtn  = makeBtn("Refresh");
        btnPanel.add(addBtn); btnPanel.add(assignBtn); btnPanel.add(removeBtn); btnPanel.add(refreshBtn);
        add(btnPanel, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> showAddDialog());
        assignBtn.addActionListener(e -> showAssignDialog());
        removeBtn.addActionListener(e -> removeSelected());
        refreshBtn.addActionListener(e -> loadWorkers());

        loadWorkers();
    }

    private void filterTable() {
        String query = searchField.getText().trim().toLowerCase();
        tableModel.setRowCount(0);
        Connection conn = DBConnection.getConnection();
        if (conn == null) return;
        String sql = "SELECT w.worker_id, w.worker_name, w.position, w.email, w.status, " +
                     "COUNT(t.task_id) AS task_count " +
                     "FROM workers w LEFT JOIN tasks t ON w.worker_id = t.worker_id " +
                     "GROUP BY w.worker_id, w.worker_name, w.position, w.email, w.status";
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String id   = rs.getString("worker_id");
                String name = rs.getString("worker_name");
                String pos  = rs.getString("position");
                String email= rs.getString("email");
                String stat = rs.getString("status");
                int count   = rs.getInt("task_count");
                if (query.isEmpty() || id.toLowerCase().contains(query) || name.toLowerCase().contains(query)
                        || pos.toLowerCase().contains(query) || email.toLowerCase().contains(query)
                        || stat.toLowerCase().contains(query)) {
                    tableModel.addRow(new Object[]{id, name, pos, email, count, stat});
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading workers: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadWorkers() {
        searchField.setText("");
        filterTable();
    }

    private void showAddDialog() {
        JTextField idField     = new JTextField();
        JTextField nameField   = new JTextField();
        JTextField posField    = new JTextField();
        JTextField emailField  = new JTextField();
        JPasswordField passField = new JPasswordField();
        JTextField salaryField = new JTextField();

        Object[] fields = {
            "Worker ID:", idField,
            "Worker Name:", nameField,
            "Position:", posField,
            "Email:", emailField,
            "Password:", passField,
            "Salary:", salaryField
        };

        int result = JOptionPane.showConfirmDialog(this, fields, "Add Worker", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        String id       = idField.getText().trim();
        String name     = nameField.getText().trim();
        String position = posField.getText().trim();
        String email    = emailField.getText().trim();
        String password = new String(passField.getPassword());
        String salaryStr = salaryField.getText().trim();

        if (id.isEmpty() || name.isEmpty() || position.isEmpty() || email.isEmpty() || password.isEmpty() || salaryStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        double salary;
        try { salary = Double.parseDouble(salaryStr); if (salary < 0) throw new NumberFormatException(); }
        catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Salary must be a valid positive number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!ValidationHelper.isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Invalid email format.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (ValidationHelper.isDuplicateEmail(email)) {
            JOptionPane.showMessageDialog(this, "Email already registered.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Connection conn = DBConnection.getConnection();
        if (conn == null) return;

        String hashedPassword = PasswordUtil.hash(password);

        // Check duplicate worker ID
        try (PreparedStatement chk = conn.prepareStatement("SELECT 1 FROM workers WHERE worker_id=?")) {
            chk.setString(1, id);
            if (chk.executeQuery().next()) {
                JOptionPane.showMessageDialog(this, "Worker ID already exists.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (SQLException e) { showDbError(e); return; }

        try {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO workers (worker_id, worker_name, position, email, password, salary) VALUES (?,?,?,?,?,?)")) {
                ps.setString(1, id); ps.setString(2, name); ps.setString(3, position);
                ps.setString(4, email); ps.setString(5, hashedPassword); ps.setDouble(6, salary);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO accounts (email, name, password, role) VALUES (?,?,?,'Worker')")) {
                ps.setString(1, email); ps.setString(2, name); ps.setString(3, hashedPassword);
                ps.executeUpdate();
            }
            JOptionPane.showMessageDialog(this, "Worker added!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadWorkers();
        } catch (SQLException e) { showDbError(e); }
    }

    private void showAssignDialog() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a worker to reassign.", "No Selection", JOptionPane.WARNING_MESSAGE); return; }
        String wid = (String) tableModel.getValueAt(row, 0);
        String wName = (String) tableModel.getValueAt(row, 1);

        Connection conn = DBConnection.getConnection();
        if (conn == null) return;

        // Pick a task that is not yet assigned to this worker
        JComboBox<String> taskBox = new JComboBox<>();
        java.util.Map<String, String> taskMap = new java.util.LinkedHashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT t.task_id, t.task_name, p.project_name FROM tasks t JOIN projects p ON t.project_id=p.project_id WHERE t.worker_id IS NULL OR t.worker_id <> ?")) {
            ps.setString(1, wid);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String display = rs.getString("project_name") + " › " + rs.getString("task_name") + " (" + rs.getString("task_id") + ")";
                taskMap.put(display, rs.getString("task_id"));
                taskBox.addItem(display);
            }
        } catch (SQLException e) { showDbError(e); return; }

        if (taskBox.getItemCount() == 0) {
            JOptionPane.showMessageDialog(this, "No unassigned tasks available.", "No Tasks", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int result = JOptionPane.showConfirmDialog(this, new Object[]{"Assign task to " + wName + ":", taskBox},
                "Update Assignment", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        String tid = taskMap.get((String) taskBox.getSelectedItem());
        try {
            try (PreparedStatement ps = conn.prepareStatement("UPDATE tasks SET worker_id = ? WHERE task_id = ?")) {
                ps.setString(1, wid); ps.setString(2, tid);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("UPDATE workers SET status = 'Assigned' WHERE worker_id = ?")) {
                ps.setString(1, wid);
                ps.executeUpdate();
            }
            JOptionPane.showMessageDialog(this, "Assignment updated!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadWorkers();
        } catch (SQLException e) { showDbError(e); }
    }

    private void removeSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a worker to remove.", "No Selection", JOptionPane.WARNING_MESSAGE); return; }
        String wid   = (String) tableModel.getValueAt(row, 0);
        String wName = (String) tableModel.getValueAt(row, 1);
        String email = (String) tableModel.getValueAt(row, 3);

        int confirm = JOptionPane.showConfirmDialog(this, "Remove worker \"" + wName + "\"? Their tasks will be unassigned.", "Confirm Remove", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        Connection conn = DBConnection.getConnection();
        if (conn == null) return;
        try {
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM workers WHERE worker_id = ?")) {
                ps.setString(1, wid); ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM accounts WHERE email = ?")) {
                ps.setString(1, email); ps.executeUpdate();
            }
            JOptionPane.showMessageDialog(this, "Worker removed!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadWorkers();
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
