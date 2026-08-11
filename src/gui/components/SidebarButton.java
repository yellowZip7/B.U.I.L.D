package gui.components;

import gui.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SidebarButton extends JButton {

    public SidebarButton(String text) {

        super(text);

        setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        setBackground(Theme.SECONDARY);
        setForeground(Theme.WHITE);
        setFocusPainted(false);
        setBorderPainted(false);
        setFont(Theme.BODY_FONT);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(Theme.GOLD);
                setForeground(Theme.PRIMARY);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(Theme.SECONDARY);
                setForeground(Theme.WHITE);
            }
        });
    }
}
