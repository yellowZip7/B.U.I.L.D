package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

import utility.DBConnection;
import utility.InputHelper;
import utility.ValidationHelper;

public class Account {
    public Scanner sc = new Scanner(System.in);
    public boolean isRunning = true;
    private String name;
    private String role;
    private String email;
    private String pass;

    public void signUp() {

        String tempPass = null;

        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.printf("║ %-36s ║\n", "ADMIN SIGN UP");
        System.out.println("╚══════════════════════════════════════╝");

        System.out.print("Full name: ");
        String tempName = InputHelper.getString(sc);

        String tempEmail;

        while (true) {
            System.out.print("Email: ");
            tempEmail = InputHelper.getString(sc);

            if (!ValidationHelper.isValidEmail(tempEmail)) {
                System.out.println("Invalid email format! Example: user@gmail.com");
                continue;
            }

            if (ValidationHelper.isDuplicateEmail(tempEmail)) {
                System.out.println("Email already exists.");
                continue;
            }

            break;
        }

        isRunning = true;

        while (isRunning) {
            System.out.print("Password: ");
            String temp4 = InputHelper.getString(sc);

            System.out.print("Confirm password: ");
            String temp5 = InputHelper.getString(sc);

            if (temp4.equals(temp5)) {
                tempPass = temp5;
                isRunning = false;
            } else {
                System.out.println("Password does not match. Try Again.\n");
            }
        }

        System.out.println("╔══════════════════════════════╗");
        System.out.printf("║ %-28s ║\n", "USER DETAILS");
        System.out.println("╠══════════════════════════════╣");
        System.out.printf("║ Name: %-22s ║\n", tempName);
        System.out.printf("║ Role: %-22s ║\n", "Admin");
        System.out.printf("║ Email: %-21s ║\n", tempEmail);
        System.out.println("╚══════════════════════════════╝");

        System.out.print("Create new account? [1]Yes [2]No: ");
        int ch = InputHelper.getInt(sc);

        if (ch == 1) {
            name  = tempName;
            role  = "Admin";
            email = tempEmail;
            pass  = tempPass;

            // Persist to database
            if (saveToDatabase()) {
                System.out.println("\n╔══════════════════════════════════════╗");
                System.out.printf("║ %-36s ║\n", "Account created successfully!");
                System.out.println("╚══════════════════════════════════════╝");
            } else {
                System.out.println("\n╔══════════════════════════════════════╗");
                System.out.printf("║ %-36s ║\n", "Failed to save account to database.");
                System.out.println("╚══════════════════════════════════════╝");
            }

        } else {
            System.out.println("\n╔══════════════════════════════════════╗");
            System.out.printf("║ %-36s ║\n", "Account not created!");
            System.out.println("╚══════════════════════════════════════╝");
        }
    }

    private boolean saveToDatabase() {
        Connection conn = DBConnection.getConnection();
        if (conn == null) return false;

        String sql = "INSERT INTO accounts (email, name, password, role) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, name);
            ps.setString(3, pass);
            ps.setString(4, role);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Database error saving account: " + e.getMessage());
            return false;
        }
    }


    public boolean saveWorkerAccount() {
        return saveToDatabase();
    }

    public String getPass()  { return pass;  }
    public String getEmail() { return email; }
    public String getRole()  { return role;  }

    public void setName(String name)   { this.name = name;   }
    public void setEmail(String email) { this.email = email; }
    public void setPass(String pass)   { this.pass = pass;   }
    public void setRole(String role)   { this.role = role;   }
}
