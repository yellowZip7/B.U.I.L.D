package gui.modules;

import gui.Theme;
import utility.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ReportsPanel extends JPanel {

    public ReportsPanel() {
        setLayout(new BorderLayout(0, 15));
        setBackground(Theme.PRIMARY);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel("Reports");
        title.setForeground(Theme.GOLD);
        title.setFont(Theme.HEADER_FONT);
        add(title, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(Theme.SECONDARY);
        tabs.setForeground(Theme.WHITE);
        tabs.setFont(Theme.BODY_FONT);

        tabs.addTab("Progress",             buildProgressTab());
        tabs.addTab("Financial",            buildFinancialTab());
        tabs.addTab("Resource Utilization", buildResourceTab());
        tabs.addTab("Workforce",            buildWorkforceTab());

        add(tabs, BorderLayout.CENTER);
    }

    // ── Progress Report ─────────────────────────────────────
    private JPanel buildProgressTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Theme.PRIMARY);
        panel.setBorder(new EmptyBorder(15, 0, 0, 0));

        String[] cols = {"Project", "Location", "Start Date", "End Date", "Total Tasks", "Completed", "% Done", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        JTable t = new JTable(model);
        styleTable(t);

        JButton refreshBtn = makeBtn("Refresh");
        refreshBtn.addActionListener(e -> loadProgress(model));
        JPanel btnRow = btnRow(refreshBtn);

        panel.add(new JScrollPane(t), BorderLayout.CENTER);
        panel.add(btnRow, BorderLayout.SOUTH);
        loadProgress(model);
        return panel;
    }

    private void loadProgress(DefaultTableModel model) {
        model.setRowCount(0);
        Connection conn = DBConnection.getConnection();
        if (conn == null) return;
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM projects"); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String pid = rs.getString("project_id");
                int total = 0, completed = 0;
                try (PreparedStatement tps = conn.prepareStatement("SELECT status FROM tasks WHERE project_id=?")) {
                    tps.setString(1, pid);
                    ResultSet trs = tps.executeQuery();
                    while (trs.next()) {
                        total++;
                        if ("Completed".equalsIgnoreCase(trs.getString("status"))) completed++;
                    }
                }
                int pct = total > 0 ? (completed * 100 / total) : 0;
                model.addRow(new Object[]{
                    rs.getString("project_name"), rs.getString("location"),
                    rs.getString("start_date"), rs.getString("end_date"),
                    total, completed, pct + "%", rs.getString("status")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Financial Report ────────────────────────────────────
    private JPanel buildFinancialTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Theme.PRIMARY);
        panel.setBorder(new EmptyBorder(15, 0, 0, 0));

        String[] cols = {"Project", "Remaining Budget", "Material Cost", "Tasks", "Est. Labour Cost"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        JTable t = new JTable(model);
        styleTable(t);

        JButton refreshBtn = makeBtn("Refresh");
        refreshBtn.addActionListener(e -> loadFinancial(model));
        panel.add(new JScrollPane(t), BorderLayout.CENTER);
        panel.add(btnRow(refreshBtn), BorderLayout.SOUTH);
        loadFinancial(model);
        return panel;
    }

    private void loadFinancial(DefaultTableModel model) {
        model.setRowCount(0);
        Connection conn = DBConnection.getConnection();
        if (conn == null) return;
        String sql = "SELECT p.project_id, p.project_name, p.budget, " +
                     "COALESCE(SUM(r.price * r.quantity), 0) AS mat_cost, " +
                     "COUNT(DISTINCT t.task_id) AS task_count " +
                     "FROM projects p " +
                     "LEFT JOIN resources r ON p.project_id = r.project_id " +
                     "LEFT JOIN tasks t ON p.project_id = t.project_id " +
                     "GROUP BY p.project_id, p.project_name, p.budget";
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                double matCost = rs.getDouble("mat_cost");
                int taskCount = rs.getInt("task_count");
                double estLabour = taskCount * 5000.0;
                model.addRow(new Object[]{
                    rs.getString("project_name"),
                    String.format("%.2f", rs.getDouble("budget")),
                    String.format("%.2f", matCost),
                    taskCount,
                    String.format("%.2f", estLabour)
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Resource Utilization ────────────────────────────────
    private JPanel buildResourceTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Theme.PRIMARY);
        panel.setBorder(new EmptyBorder(15, 0, 0, 0));

        String[] cols = {"Project", "Material ID", "Material Name", "Qty Remaining", "Unit Price", "Total Value"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        JTable t = new JTable(model);
        styleTable(t);

        JButton refreshBtn = makeBtn("Refresh");
        refreshBtn.addActionListener(e -> loadResourceUtil(model));
        panel.add(new JScrollPane(t), BorderLayout.CENTER);
        panel.add(btnRow(refreshBtn), BorderLayout.SOUTH);
        loadResourceUtil(model);
        return panel;
    }

    private void loadResourceUtil(DefaultTableModel model) {
        model.setRowCount(0);
        Connection conn = DBConnection.getConnection();
        if (conn == null) return;
        String sql = "SELECT p.project_name, r.material_id, r.material_name, r.quantity, r.price " +
                     "FROM resources r JOIN projects p ON r.project_id = p.project_id ORDER BY p.project_name";
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                double price = rs.getDouble("price");
                int qty = rs.getInt("quantity");
                model.addRow(new Object[]{
                    rs.getString("project_name"), rs.getString("material_id"),
                    rs.getString("material_name"), qty,
                    String.format("%.2f", price), String.format("%.2f", price * qty)
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Workforce Report ────────────────────────────────────
    private JPanel buildWorkforceTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Theme.PRIMARY);
        panel.setBorder(new EmptyBorder(15, 0, 0, 0));

        String[] cols = {"Worker", "Position", "Project", "Task", "Task Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        JTable t = new JTable(model);
        styleTable(t);

        JButton refreshBtn = makeBtn("Refresh");
        refreshBtn.addActionListener(e -> loadWorkforce(model));
        panel.add(new JScrollPane(t), BorderLayout.CENTER);
        panel.add(btnRow(refreshBtn), BorderLayout.SOUTH);
        loadWorkforce(model);
        return panel;
    }

    private void loadWorkforce(DefaultTableModel model) {
        model.setRowCount(0);
        Connection conn = DBConnection.getConnection();
        if (conn == null) return;
        String sql = "SELECT w.worker_name, w.position, p.project_name, t.task_name, t.status " +
                     "FROM workers w " +
                     "LEFT JOIN tasks t ON w.worker_id = t.worker_id " +
                     "LEFT JOIN projects p ON t.project_id = p.project_id " +
                     "ORDER BY w.worker_name";
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String proj = rs.getString("project_name");
                String task = rs.getString("task_name");
                String stat = rs.getString("status");
                model.addRow(new Object[]{
                    rs.getString("worker_name"), rs.getString("position"),
                    proj != null ? proj : "—",
                    task != null ? task : "No task assigned",
                    stat != null ? stat : "—"
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Helpers ─────────────────────────────────────────────
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

    private JPanel btnRow(JButton... btns) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        p.setBackground(Theme.PRIMARY);
        for (JButton b : btns) p.add(b);
        return p;
    }
}
