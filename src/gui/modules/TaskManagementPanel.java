package gui.modules;

import gui.Theme;
import utility.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class TaskManagementPanel extends JPanel {

    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField searchField;

    public TaskManagementPanel() {
        setLayout(new BorderLayout(0, 15));
        setBackground(Theme.PRIMARY);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel("Task Management");
        title.setForeground(Theme.GOLD);
        title.setFont(Theme.HEADER_FONT);
        add(title, BorderLayout.NORTH);

        String[] cols = {"Task ID", "Project", "Task Name", "Status", "Assigned Worker"};
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
        searchField.putClientProperty("JTextField.placeholderText", "Search by task ID, project, task name, status, or worker...");
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
        JButton addBtn    = makeBtn("Add Task");
        JButton statusBtn = makeBtn("Update Status");
        JButton assignBtn = makeBtn("Assign Worker");
        JButton deleteBtn = makeBtn("Delete Task");
        JButton refreshBtn = makeBtn("Refresh");
        btnPanel.add(addBtn); btnPanel.add(statusBtn); btnPanel.add(assignBtn);
        btnPanel.add(deleteBtn); btnPanel.add(refreshBtn);
        add(btnPanel, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> showAddDialog());
        statusBtn.addActionListener(e -> showUpdateStatusDialog());
        assignBtn.addActionListener(e -> showAssignWorkerDialog());
        deleteBtn.addActionListener(e -> deleteSelected());
        refreshBtn.addActionListener(e -> loadTasks());

        loadTasks();
    }

    private void filterTable() {
        String query = searchField.getText().trim().toLowerCase();
        tableModel.setRowCount(0);
        Connection conn = DBConnection.getConnection();
        if (conn == null) return;
        String sql = "SELECT t.task_id, p.project_name, t.task_name, t.status, w.worker_name " +
                     "FROM tasks t " +
                     "JOIN projects p ON t.project_id = p.project_id " +
                     "LEFT JOIN workers w ON t.worker_id = w.worker_id";
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String tid     = rs.getString("task_id");
                String project = rs.getString("project_name");
                String tname   = rs.getString("task_name");
                String status  = rs.getString("status");
                String worker  = rs.getString("worker_name");
                String workerDisplay = worker != null ? worker : "Unassigned";
                if (query.isEmpty() || tid.toLowerCase().contains(query) || project.toLowerCase().contains(query)
                        || tname.toLowerCase().contains(query) || status.toLowerCase().contains(query)
                        || workerDisplay.toLowerCase().contains(query)) {
                    tableModel.addRow(new Object[]{tid, project, tname, status, workerDisplay});
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading tasks: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTasks() {
        searchField.setText("");
        filterTable();
    }

    private void showAddDialog() {
        // Build project dropdown
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

        JTextField taskIdField   = new JTextField();
        JTextField taskNameField = new JTextField();

        Object[] fields = {
            "Project:", projectBox,
            "Task ID:", taskIdField,
            "Task Name:", taskNameField
        };

        int result = JOptionPane.showConfirmDialog(this, fields, "Add Task", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        String pid      = projectMap.get((String) projectBox.getSelectedItem());
        String taskId   = taskIdField.getText().trim();
        String taskName = taskNameField.getText().trim();

        if (taskId.isEmpty() || taskName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check duplicate task ID
        try (PreparedStatement chk = conn.prepareStatement("SELECT 1 FROM tasks WHERE task_id=?")) {
            chk.setString(1, taskId);
            if (chk.executeQuery().next()) {
                JOptionPane.showMessageDialog(this, "Task ID already exists.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (SQLException e) { showDbError(e); return; }

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO tasks (task_id, project_id, task_name, status) VALUES (?,?,?,'Pending')")) {
            ps.setString(1, taskId); ps.setString(2, pid); ps.setString(3, taskName);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Task added!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadTasks();
        } catch (SQLException e) { showDbError(e); }
    }

    private void showUpdateStatusDialog() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a task to update.", "No Selection", JOptionPane.WARNING_MESSAGE); return; }
        String tid = (String) tableModel.getValueAt(row, 0);

        String[] statuses = {"Pending", "In Progress", "Completed"};
        String newStatus = (String) JOptionPane.showInputDialog(this, "Select new status for task \"" + tid + "\":",
                "Update Status", JOptionPane.PLAIN_MESSAGE, null, statuses, statuses[0]);
        if (newStatus == null) return;

        Connection conn = DBConnection.getConnection();
        if (conn == null) return;
        try (PreparedStatement ps = conn.prepareStatement("UPDATE tasks SET status = ? WHERE task_id = ?")) {
            ps.setString(1, newStatus); ps.setString(2, tid);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Status updated!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadTasks();
        } catch (SQLException e) { showDbError(e); }
    }

    private void showAssignWorkerDialog() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a task to assign.", "No Selection", JOptionPane.WARNING_MESSAGE); return; }
        String tid = (String) tableModel.getValueAt(row, 0);

        Connection conn = DBConnection.getConnection();
        if (conn == null) return;
        JComboBox<String> workerBox = new JComboBox<>();
        java.util.Map<String, String> workerMap = new java.util.LinkedHashMap<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT worker_id, worker_name FROM workers");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String display = rs.getString("worker_name") + " (" + rs.getString("worker_id") + ")";
                workerMap.put(display, rs.getString("worker_id"));
                workerBox.addItem(display);
            }
        } catch (SQLException e) { showDbError(e); return; }

        if (workerBox.getItemCount() == 0) {
            JOptionPane.showMessageDialog(this, "No workers available.", "No Workers", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int result = JOptionPane.showConfirmDialog(this, new Object[]{"Select Worker:", workerBox},
                "Assign Worker to Task " + tid, JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        String wid = workerMap.get((String) workerBox.getSelectedItem());
        try {
            try (PreparedStatement ps = conn.prepareStatement("UPDATE tasks SET worker_id = ? WHERE task_id = ?")) {
                ps.setString(1, wid); ps.setString(2, tid);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("UPDATE workers SET status = 'Assigned' WHERE worker_id = ?")) {
                ps.setString(1, wid);
                ps.executeUpdate();
            }
            JOptionPane.showMessageDialog(this, "Worker assigned!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadTasks();
        } catch (SQLException e) { showDbError(e); }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a task to delete.", "No Selection", JOptionPane.WARNING_MESSAGE); return; }
        String tid = (String) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete task \"" + tid + "\"?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        Connection conn = DBConnection.getConnection();
        if (conn == null) return;
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM tasks WHERE task_id = ?")) {
            ps.setString(1, tid);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Task deleted!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadTasks();
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
