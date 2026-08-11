package gui;

import gui.components.RoundedButton;
import main.Main;
import utility.DBConnection;
import utility.PasswordUtil;
import utility.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginFrame extends JFrame {

    public LoginFrame() {

        setTitle("B.U.I.L.D System");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(1, 2));

        // ── Left branding panel ──────────────────────────────────
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(Theme.PRIMARY);
        leftPanel.setLayout(new BorderLayout());

        JLabel logo = new JLabel("B.U.I.L.D", SwingConstants.CENTER);
        logo.setForeground(Theme.GOLD);
        logo.setFont(Theme.TITLE_FONT);

        JLabel tagline = new JLabel("Building Unified Infrastructure & Labor Dashboard", SwingConstants.CENTER);
        tagline.setForeground(Theme.LIGHT_TEXT);
        tagline.setFont(Theme.BODY_FONT);

        JPanel brandPanel = new JPanel();
        brandPanel.setOpaque(false);
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);
        brandPanel.add(logo);
        brandPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        brandPanel.add(tagline);

        leftPanel.add(brandPanel, BorderLayout.CENTER);

        // ── Right form panel ─────────────────────────────────────
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(Theme.SECONDARY);
        rightPanel.setBorder(new EmptyBorder(80, 80, 80, 80));
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        JLabel signInLabel = new JLabel("Sign In");
        signInLabel.setForeground(Theme.WHITE);
        signInLabel.setFont(Theme.HEADER_FONT);
        signInLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        styleField(usernameField);
        styleField(passwordField);

        RoundedButton loginButton = new RoundedButton("LOGIN");
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel signUpLink = new JLabel("Don't have an account? Sign Up");
        signUpLink.setForeground(Theme.LIGHT_TEXT);
        signUpLink.setFont(Theme.BODY_FONT);
        signUpLink.setAlignmentX(Component.LEFT_ALIGNMENT);
        signUpLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        signUpLink.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                Main.openSignUp();
            }
        });

        rightPanel.add(signInLabel);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        addLabeledField(rightPanel, "Email", usernameField);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        addLabeledField(rightPanel, "Password", passwordField);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        rightPanel.add(loginButton);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        rightPanel.add(signUpLink);

        // ── Login action ─────────────────────────────────────────
        loginButton.addActionListener(e -> attemptLogin(usernameField.getText().trim(),
                new String(passwordField.getPassword())));

        // Allow Enter key to trigger login
        passwordField.addActionListener(e -> attemptLogin(usernameField.getText().trim(),
                new String(passwordField.getPassword())));

        add(leftPanel);
        add(rightPanel);
    }

    private void attemptLogin(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Email and password are required.", "Login Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "Cannot connect to database.", "Connection Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Fetch stored hash + role + name for the given email
            String sql = "SELECT password, role, name FROM accounts WHERE email = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, email);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    String role       = rs.getString("role");
                    String name       = rs.getString("name");

                    if (!PasswordUtil.verify(password, storedHash)) {
                        JOptionPane.showMessageDialog(this, "Invalid email or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Start session
                    SessionManager.login(email, role, name);

                    if ("Admin".equalsIgnoreCase(role)) {
                        dispose();
                        Main.openAdminDashboard();
                    } else if ("Worker".equalsIgnoreCase(role)) {
                        dispose();
                        Main.openWorkerDashboard();
                    } else {
                        JOptionPane.showMessageDialog(this, "Unknown role: " + role, "Login Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid email or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void styleField(JTextField field) {
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setBackground(Theme.PRIMARY);
        field.setForeground(Theme.WHITE);
        field.setCaretColor(Theme.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.GOLD, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        field.setFont(Theme.BODY_FONT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void addLabeledField(JPanel parent, String labelText, JTextField field) {
        JLabel label = new JLabel(labelText);
        label.setForeground(Theme.LIGHT_TEXT);
        label.setFont(Theme.BODY_FONT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(label);
        parent.add(Box.createRigidArea(new Dimension(0, 4)));
        parent.add(field);
    }
}
