package gui.worker;

import gui.Theme;
import utility.DBConnection;
import utility.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class WorkerTasksPanel extends JPanel {

    public WorkerTasksPanel() {

        setLayout(new BorderLayout());
        setBackground(Theme.PRIMARY);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel("My Assigned Tasks");
        title.setForeground(Theme.GOLD);
        title.setFont(Theme.HEADER);

        add(title, BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
    }

    private JScrollPane buildTable() {

        String[] columns = {"Project", "Task", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        String currentEmail = SessionManager.getEmail();   // from SessionManager, not console.SignIn

        if (currentEmail != null) {
            String sql = "SELECT p.project_name, t.task_name, t.status " +
                         "FROM tasks t " +
                         "JOIN projects p ON t.project_id = p.project_id " +
                         "JOIN workers w  ON t.worker_id  = w.worker_id " +
                         "WHERE w.email = ?";
            try (Connection conn = DBConnection.getConnection()) {
                if (conn != null) {
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, currentEmail);
                        ResultSet rs = ps.executeQuery();
                        while (rs.next()) {
                            model.addRow(new Object[]{
                                rs.getString("project_name"),
                                rs.getString("task_name"),
                                rs.getString("status")
                            });
                        }
                    }
                }
            } catch (SQLException e) {
                model.addRow(new Object[]{"Error", e.getMessage(), ""});
            }
        }

        JTable table = new JTable(model);
        table.setBackground(Theme.SECONDARY);
        table.setForeground(Theme.WHITE);
        table.setFont(Theme.BODY_FONT);
        table.setRowHeight(30);
        table.getTableHeader().setBackground(Theme.PRIMARY);
        table.getTableHeader().setForeground(Theme.GOLD);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBackground(Theme.SECONDARY);
        return scrollPane;
    }
}
