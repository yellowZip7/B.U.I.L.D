package console;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

import main.Main;
import utility.DBConnection;
import utility.InputHelper;

public class SignIn {
    public static String currentUserEmail;

    public boolean isRunningAdmin  = false;
    public boolean isRunningWorker = false;

    public Scanner sc = new Scanner(System.in);

    public void signIn() {
        isRunningAdmin  = false;
        isRunningWorker = false;

        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.printf("║ %-36s ║\n", "SIGN IN");
        System.out.println("╚══════════════════════════════════════╝");
        System.out.print("Enter Email: ");
        String eCheck = InputHelper.getString(sc);

        System.out.print("Enter Password: ");
        String pCheck = InputHelper.getString(sc);

        Connection conn = DBConnection.getConnection();
        if (conn == null) {
            System.out.println("Database connection unavailable.");
            return;
        }

        String sql = "SELECT role FROM accounts WHERE email = ? AND password = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, eCheck);
            ps.setString(2, pCheck);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                currentUserEmail = eCheck;

                if (role.equals("Admin")) {
                    System.out.println("\n╔══════════════════════════════════════╗");
                    System.out.printf("║ %-36s ║\n", "Successfully signed in as ADMIN!");
                    System.out.println("╚══════════════════════════════════════╝");
                    Main.isRunning  = false;
                    isRunningAdmin  = true;

                } else if (role.equals("Worker")) {
                    System.out.println("\n╔══════════════════════════════════════╗");
                    System.out.printf("║ %-36s ║\n", "Successfully signed in as WORKER!");
                    System.out.println("╚══════════════════════════════════════╝");
                    isRunningWorker = true;
                }

            } else {
                System.out.println("\nAccount not found.");
                System.out.println("If you haven't already, sign up and create an account!");
            }

        } catch (SQLException e) {
            System.out.println("Database error during sign in: " + e.getMessage());
        }
    }
}
