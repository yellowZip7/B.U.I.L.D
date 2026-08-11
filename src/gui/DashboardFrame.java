package gui;

import gui.components.SidebarButton;
import gui.components.StatsCard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DashboardFrame extends JFrame {

    public DashboardFrame() {

        setTitle("B.U.I.L.D Dashboard");
        setSize(1400, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(createSidebar(), BorderLayout.WEST);
        add(createTopBar(), BorderLayout.NORTH);
        add(createDashboardPanel(), BorderLayout.CENTER);
    }

    private JPanel createSidebar() {

        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(240, 0));
        panel.setBackground(Theme.PRIMARY);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(25,20,25,20));

        String[] menus = {
                "Dashboard",
                "Projects",
                "Tasks",
                "Resources",
                "Workers",
                "Reports",
                "Logout"
        };

        for(String menu : menus) {
            SidebarButton btn = new SidebarButton(menu);
            panel.add(btn);
            panel.add(Box.createRigidArea(new Dimension(0,15)));
        }

        return panel;
    }

    private JPanel createTopBar() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.SECONDARY);
        panel.setPreferredSize(new Dimension(0, 80));

        JLabel title = new JLabel("Dashboard Overview");
        title.setForeground(Theme.WHITE);
        title.setFont(Theme.HEADER_FONT);

        panel.add(title, BorderLayout.WEST);

        return panel;
    }

    private JPanel createDashboardPanel() {

        JPanel panel = new JPanel(new GridLayout(1,4,20,20));
        panel.setBackground(Theme.PRIMARY);
        panel.setBorder(new EmptyBorder(30,30,30,30));

        panel.add(new StatsCard("Projects", "18"));
        panel.add(new StatsCard("Tasks", "64"));
        panel.add(new StatsCard("Workers", "92"));
        panel.add(new StatsCard("Resources", "143"));

        return panel;
    }
}
