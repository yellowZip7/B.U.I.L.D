package gui;

import gui.components.RoundedButton;
import utility.DBConnection;
import utility.PasswordUtil;
import utility.ValidationHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SignUpFrame extends JFrame {

    public SignUpFrame() {

        setTitle("B.U.I.L.D - Create Account");
        setSize(1000, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(1, 2));

        // Left branding panel
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(Theme.PRIMARY);
        leftPanel.setLayout(new BorderLayout());

        JLabel logo = new JLabel("B.U.I.L.D", SwingConstants.CENTER);
        logo.setForeground(Theme.GOLD);
        logo.setFont(Theme.TITLE_FONT);
        leftPanel.add(logo, BorderLayout.CENTER);

        // Right form panel
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(Theme.SECONDARY);
        rightPanel.setBorder(new EmptyBorder(60, 80, 60, 80));
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        JLabel headerLabel = new JLabel("Create Account");
        headerLabel.setForeground(Theme.WHITE);
        headerLabel.setFont(Theme.HEADER_FONT);

        JTextField nameField     = new JTextField();
        JTextField emailField    = new JTextField();
        JPasswordField passField = new JPasswordField();
        JPasswordField confField = new JPasswordField();
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"Admin", "Worker"});
        roleBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        roleBox.setBackground(Theme.PRIMARY);
        roleBox.setForeground(Theme.WHITE);
        roleBox.setFont(Theme.BODY_FONT);
        roleBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        styleField(nameField);
        styleField(emailField);
        styleField(passField);
        styleField(confField);

        RoundedButton signUpButton = new RoundedButton("CREATE ACCOUNT");
        signUpButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        JLabel backLabel = new JLabel("Already have an account? Sign In");
        backLabel.setForeground(Theme.LIGHT_TEXT);
        backLabel.setFont(Theme.BODY_FONT);
        backLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        backLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        backLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                dispose();
            }
        });

        rightPanel.add(headerLabel);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        addLabeledField(rightPanel, "Full Name", nameField);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        addLabeledField(rightPanel, "Email", emailField);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        addLabeledField(rightPanel, "Password", passField);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        addLabeledField(rightPanel, "Confirm Password", confField);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        JLabel roleLabel = new JLabel("Role");
        roleLabel.setForeground(Theme.LIGHT_TEXT);
        roleLabel.setFont(Theme.BODY_FONT);
        roleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(roleLabel);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        rightPanel.add(roleBox);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        rightPanel.add(signUpButton);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        rightPanel.add(backLabel);

        signUpButton.addActionListener(e -> {
            String name  = nameField.getText().trim();
            String email = emailField.getText().trim();
            String pass  = new String(passField.getPassword());
            String conf  = new String(confField.getPassword());
            String role  = (String) roleBox.getSelectedItem();

            // Validation
            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (pass.length() < 6) {
                JOptionPane.showMessageDialog(this, "Password must be at least 6 characters.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!ValidationHelper.isValidEmail(email)) {
                JOptionPane.showMessageDialog(this, "Invalid email format.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (ValidationHelper.isDuplicateEmail(email)) {
                JOptionPane.showMessageDialog(this, "Email already registered.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!pass.equals(conf)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Hash password before storing
            String hashedPassword = PasswordUtil.hash(pass);

            try (Connection conn = DBConnection.getConnection()) {
                if (conn == null) {
                    JOptionPane.showMessageDialog(this, "Cannot connect to database.", "Connection Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String sql = "INSERT INTO accounts (email, name, password, role) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, email);
                    ps.setString(2, name);
                    ps.setString(3, hashedPassword);   // store hash, never plain text
                    ps.setString(4, role);
                    ps.executeUpdate();
                }
                // If worker, also insert into workers table
                if ("Worker".equals(role)) {
                    String wid  = "W-" + System.currentTimeMillis();
                    String wSql = "INSERT INTO workers (worker_id, worker_name, position, email, password) VALUES (?,?,?,?,?)";
                    try (PreparedStatement ps2 = conn.prepareStatement(wSql)) {
                        ps2.setString(1, wid);
                        ps2.setString(2, name);
                        ps2.setString(3, "Unassigned");
                        ps2.setString(4, email);
                        ps2.setString(5, hashedPassword);   // store hash here too
                        ps2.executeUpdate();
                    }
                }
                JOptionPane.showMessageDialog(this, "Account created! You may now sign in.", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        add(leftPanel);
        add(rightPanel);
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
    }

    private void addLabeledField(JPanel parent, String labelText, JTextField field) {
        JLabel label = new JLabel(labelText);
        label.setForeground(Theme.LIGHT_TEXT);
        label.setFont(Theme.BODY_FONT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(label);
        parent.add(Box.createRigidArea(new Dimension(0, 4)));
        parent.add(field);
    }
}
