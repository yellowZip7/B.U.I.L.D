package gui.modules;

import gui.Theme;
import utility.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class BudgetMonitoringPanel extends JPanel {

    private DefaultTableModel budgetModel;
    private DefaultTableModel payrollModel;
    private DefaultTableModel materialModel;

    public BudgetMonitoringPanel() {
        setLayout(new BorderLayout(0, 15));
        setBackground(Theme.PRIMARY);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel("Budget Monitoring");
        title.setForeground(Theme.GOLD);
        title.setFont(Theme.HEADER_FONT);
        add(title, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(Theme.SECONDARY);
        tabs.setForeground(Theme.WHITE);
        tabs.setFont(Theme.BODY_FONT);

        tabs.addTab("Project Budgets",     buildBudgetTab());
        tabs.addTab("Worker Payroll",      buildPayrollTab());
        tabs.addTab("Material Expenses",   buildMaterialTab());
        tabs.addTab("Pay Worker Salary",   buildSalaryTab());

        add(tabs, BorderLayout.CENTER);
    }

    // ── Tab 1: Project Budgets ──────────────────────────────
    private JPanel buildBudgetTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Theme.PRIMARY);
        panel.setBorder(new EmptyBorder(15, 0, 0, 0));

        String[] cols = {"Project ID", "Name", "Remaining Budget", "Status"};
        budgetModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        JTable t = new JTable(budgetModel);
        styleTable(t);
        panel.add(new JScrollPane(t), BorderLayout.CENTER);

        JButton refreshBtn = makeBtn("Refresh");
        refreshBtn.addActionListener(e -> loadBudgets());
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnRow.setBackground(Theme.PRIMARY);
        btnRow.add(refreshBtn);
        panel.add(btnRow, BorderLayout.SOUTH);

        loadBudgets();
        return panel;
    }

    private void loadBudgets() {
        budgetModel.setRowCount(0);
        Connection conn = DBConnection.getConnection();
        if (conn == null) return;
        try (PreparedStatement ps = conn.prepareStatement("SELECT project_id, project_name, budget, status FROM projects");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                budgetModel.addRow(new Object[]{
                    rs.getString("project_id"),
                    rs.getString("project_name"),
                    String.format("%.2f", rs.getDouble("budget")),
                    rs.getString("status")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Tab 2: Worker Payroll ───────────────────────────────
    private JPanel buildPayrollTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Theme.PRIMARY);
        panel.setBorder(new EmptyBorder(15, 0, 0, 0));

        String[] cols = {"Project", "Worker", "Task", "Salary"};
        payrollModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        JTable t = new JTable(payrollModel);
        styleTable(t);
        panel.add(new JScrollPane(t), BorderLayout.CENTER);

        JButton refreshBtn = makeBtn("Refresh");
        refreshBtn.addActionListener(e -> loadPayroll());
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnRow.setBackground(Theme.PRIMARY);
        btnRow.add(refreshBtn);
        panel.add(btnRow, BorderLayout.SOUTH);

        loadPayroll();
        return panel;
    }

    private void loadPayroll() {
        payrollModel.setRowCount(0);
        Connection conn = DBConnection.getConnection();
        if (conn == null) return;
        String sql = "SELECT p.project_name, w.worker_name, t.task_name, w.salary " +
                     "FROM tasks t " +
                     "JOIN projects p ON t.project_id = p.project_id " +
                     "JOIN workers w  ON t.worker_id  = w.worker_id";
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                payrollModel.addRow(new Object[]{
                    rs.getString("project_name"),
                    rs.getString("worker_name"),
                    rs.getString("task_name"),
                    String.format("%.2f", rs.getDouble("salary"))
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Tab 3: Material Expenses ────────────────────────────
    private JPanel buildMaterialTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Theme.PRIMARY);
        panel.setBorder(new EmptyBorder(15, 0, 0, 0));

        String[] cols = {"Project", "Material", "Qty", "Unit Price", "Total Cost"};
        materialModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        JTable t = new JTable(materialModel);
        styleTable(t);
        panel.add(new JScrollPane(t), BorderLayout.CENTER);

        JButton refreshBtn = makeBtn("Refresh");
        refreshBtn.addActionListener(e -> loadMaterials());
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnRow.setBackground(Theme.PRIMARY);
        btnRow.add(refreshBtn);
        panel.add(btnRow, BorderLayout.SOUTH);

        loadMaterials();
        return panel;
    }

    private void loadMaterials() {
        materialModel.setRowCount(0);
        Connection conn = DBConnection.getConnection();
        if (conn == null) return;
        String sql = "SELECT p.project_name, r.material_name, r.quantity, r.price " +
                     "FROM resources r JOIN projects p ON r.project_id = p.project_id";
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                double price = rs.getDouble("price");
                int qty = rs.getInt("quantity");
                materialModel.addRow(new Object[]{
                    rs.getString("project_name"),
                    rs.getString("material_name"),
                    qty,
                    String.format("%.2f", price),
                    String.format("%.2f", price * qty)
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Tab 4: Pay Worker Salary ────────────────────────────
    private JPanel buildSalaryTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Theme.PRIMARY);
        panel.setBorder(new EmptyBorder(15, 0, 0, 0));

        JLabel info = new JLabel("<html>Select a project and task, enter salary amount to deduct from project budget.</html>");
        info.setForeground(Theme.LIGHT_TEXT);
        info.setFont(Theme.BODY_FONT);
        panel.add(info, BorderLayout.NORTH);

        JButton payBtn = makeBtn("Pay Worker Salary");
        payBtn.addActionListener(e -> showPaySalaryDialog());
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnRow.setBackground(Theme.PRIMARY);
        btnRow.add(payBtn);
        panel.add(btnRow, BorderLayout.CENTER);
        return panel;
    }

    private void showPaySalaryDialog() {
        Connection conn = DBConnection.getConnection();
        if (conn == null) return;

        JComboBox<String> projectBox = new JComboBox<>();
        java.util.Map<String, String> projectMap = new java.util.LinkedHashMap<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT project_id, project_name, budget FROM projects");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String display = rs.getString("project_name") + " (Budget: " + String.format("%.2f", rs.getDouble("budget")) + ")";
                projectMap.put(display, rs.getString("project_id"));
                projectBox.addItem(display);
            }
        } catch (SQLException e) { JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); return; }

        if (projectBox.getItemCount() == 0) { JOptionPane.showMessageDialog(this, "No projects.", "Info", JOptionPane.INFORMATION_MESSAGE); return; }

        JComboBox<String> taskBox = new JComboBox<>();
        java.util.Map<String, String[]> taskMap = new java.util.LinkedHashMap<>();

        // Populate task box based on project selection
        projectBox.addActionListener(ev -> {
            taskBox.removeAllItems(); taskMap.clear();
            String pid = projectMap.get((String) projectBox.getSelectedItem());
            if (pid == null) return;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT t.task_id, t.task_name, w.worker_name FROM tasks t JOIN workers w ON t.worker_id=w.worker_id WHERE t.project_id=?")) {
                ps.setString(1, pid);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String display = rs.getString("task_name") + " → " + rs.getString("worker_name");
                    taskMap.put(display, new String[]{rs.getString("task_id"), rs.getString("worker_name")});
                    taskBox.addItem(display);
                }
            } catch (SQLException ex) { /* ignore */ }
        });
        // Trigger initial load
        if (projectBox.getItemCount() > 0) projectBox.setSelectedIndex(0);

        JTextField salaryField = new JTextField();

        Object[] fields = {"Project:", projectBox, "Task (Assigned Worker):", taskBox, "Salary Amount:", salaryField};
        int result = JOptionPane.showConfirmDialog(this, fields, "Pay Worker Salary", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        if (taskBox.getItemCount() == 0) { JOptionPane.showMessageDialog(this, "No assigned tasks in this project.", "Error", JOptionPane.WARNING_MESSAGE); return; }

        String pid = projectMap.get((String) projectBox.getSelectedItem());
        String[] taskInfo = taskMap.get((String) taskBox.getSelectedItem());
        double salary;
        try { salary = Double.parseDouble(salaryField.getText().trim()); if (salary <= 0) throw new NumberFormatException(); }
        catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Invalid salary amount.", "Error", JOptionPane.WARNING_MESSAGE); return; }

        // Check budget
        try {
            double budget;
            try (PreparedStatement ps = conn.prepareStatement("SELECT budget FROM projects WHERE project_id=?")) {
                ps.setString(1, pid); ResultSet rs = ps.executeQuery();
                if (!rs.next()) { JOptionPane.showMessageDialog(this, "Project not found.", "Error", JOptionPane.ERROR_MESSAGE); return; }
                budget = rs.getDouble("budget");
            }
            if (budget < salary) {
                JOptionPane.showMessageDialog(this, String.format("Insufficient budget.\nRequired: %.2f | Available: %.2f", salary, budget), "Budget Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try (PreparedStatement ps = conn.prepareStatement("UPDATE projects SET budget = budget - ? WHERE project_id = ?")) {
                ps.setDouble(1, salary); ps.setString(2, pid); ps.executeUpdate();
            }
            JOptionPane.showMessageDialog(this, String.format("Salary of %.2f paid to %s!\nRemaining budget: %.2f", salary, taskInfo[1], budget - salary), "Success", JOptionPane.INFORMATION_MESSAGE);
            loadBudgets();
        } catch (SQLException e) { JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
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
}
