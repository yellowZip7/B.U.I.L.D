package gui.components;

import gui.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class StatsCard extends JPanel {

    public StatsCard(String titleText, String valueText) {

        setLayout(new BorderLayout());
        setBackground(Theme.SECONDARY);
        setBorder(new EmptyBorder(20,20,20,20));

        JLabel title = new JLabel(titleText);
        title.setForeground(Theme.LIGHT_TEXT);
        title.setFont(Theme.BODY_FONT);

        JLabel value = new JLabel(valueText);
        value.setForeground(Theme.GOLD);
        value.setFont(new Font("Segoe UI", Font.BOLD, 42));

        add(title, BorderLayout.NORTH);
        add(value, BorderLayout.CENTER);
    }
}
