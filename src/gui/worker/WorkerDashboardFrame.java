package gui.worker;

import gui.Theme;
import gui.components.SidebarButton;
import main.Main;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class WorkerDashboardFrame extends JFrame {

    private JPanel contentArea;

    public WorkerDashboardFrame() {

        setTitle("B.U.I.L.D - Worker Dashboard");
        setSize(1400, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(createSidebar(), BorderLayout.WEST);
        add(createTopBar(), BorderLayout.NORTH);

        contentArea = createWelcomePanel();
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

        String[] menus = {"My Tasks", "Logout"};
        int[] choices = {1, 2};

        for (int i = 0; i < menus.length; i++) {
            final int choice = choices[i];
            SidebarButton btn = new SidebarButton(menus[i]);
            btn.addActionListener(e -> Main.workerNavigation(choice));
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

        JLabel title = new JLabel("B.U.I.L.D  — Worker Dashboard");
        title.setForeground(Theme.WHITE);
        title.setFont(Theme.HEADER_FONT);

        panel.add(title, BorderLayout.WEST);

        return panel;
    }

    private JPanel createWelcomePanel() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.PRIMARY);
        panel.setBorder(new EmptyBorder(40, 40, 40, 40));

        JLabel welcome = new JLabel("Welcome! Select an option from the sidebar.");
        welcome.setForeground(Theme.LIGHT_TEXT);
        welcome.setFont(Theme.HEADER_FONT);
        welcome.setHorizontalAlignment(SwingConstants.CENTER);

        panel.add(welcome, BorderLayout.CENTER);
        return panel;
    }
}
