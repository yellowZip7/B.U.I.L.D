package gui.admin;

import gui.Theme;
import gui.components.SidebarButton;
import gui.components.StatsCard;
import main.Main;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AdminDashboardFrame extends JFrame {

    private JPanel contentArea;

    public AdminDashboardFrame() {

        setTitle("B.U.I.L.D - Admin Dashboard");
        setSize(1400, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(createSidebar(), BorderLayout.WEST);
        add(createTopBar(), BorderLayout.NORTH);

        contentArea = createDashboardPanel();
        add(contentArea, BorderLayout.CENTER);
    }

    public void setContentPanel(JPanel panel) {
        getContentPane().remove(contentArea);
        contentArea = panel;
        getContentPane().add(contentArea, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JPanel createSidebar() {

        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(240, 0));
        panel.setBackground(Theme.PRIMARY);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(25, 20, 25, 20));

        String[] menus = {
                "Dashboard",
                "Projects",
                "Tasks",
                "Resources",
                "Workers",
                "Budget",
                "Reports",
                "Logout"
        };

        int[] choices = {0, 1, 2, 3, 4, 5, 6, 7};

        for (int i = 0; i < menus.length; i++) {
            final int choice = choices[i];
            SidebarButton btn = new SidebarButton(menus[i]);
            btn.addActionListener(e -> {
                if (choice == 0) {
                    setContentPanel(createDashboardPanel());
                } else {
                    Main.adminNavigation(choice);
                }
            });
            panel.add(btn);
            panel.add(Box.createRigidArea(new Dimension(0, 15)));
        }

        return panel;
    }

    private JPanel createTopBar() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.SECONDARY);
        panel.setPreferredSize(new Dimension(0, 80));
        panel.setBorder(new EmptyBorder(0, 25, 0, 25));

        JLabel title = new JLabel("B.U.I.L.D  — Admin Dashboard");
        title.setForeground(Theme.WHITE);
        title.setFont(Theme.HEADER_FONT);

        JLabel subtitle = new JLabel("Building Unified Infrastructure & Labor Dashboard");
        subtitle.setForeground(Theme.LIGHT_TEXT);
        subtitle.setFont(Theme.BODY_FONT);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.add(title);
        textPanel.add(subtitle);

        panel.add(textPanel, BorderLayout.WEST);

        return panel;
    }

    private JPanel createDashboardPanel() {

        JPanel panel = new JPanel(new GridLayout(1, 4, 20, 20));
        panel.setBackground(Theme.PRIMARY);
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));

        panel.add(new StatsCard("Projects",  queryCount("SELECT COUNT(*) FROM projects")));
        panel.add(new StatsCard("Tasks",     queryCount("SELECT COUNT(*) FROM tasks")));
        panel.add(new StatsCard("Workers",   queryCount("SELECT COUNT(*) FROM workers")));
        panel.add(new StatsCard("Resources", queryCount("SELECT COUNT(*) FROM resources")));

        return panel;
    }

    private String queryCount(String sql) {
        try (java.sql.Connection conn = utility.DBConnection.getConnection()) {
            if (conn == null) return "—";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sql);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return String.valueOf(rs.getInt(1));
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return "—";
    }
}
