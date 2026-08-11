package utility;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Thread-safe database connection provider.
 * Returns a fresh connection per call instead of sharing a single static
 * connection, which would fail under concurrent Swing event-thread usage.
 */
public class DBConnection {

    private static final String HOST    = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "build_db";
    private static final String URL     = HOST + DB_NAME + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER    = "root";
    private static final String PASSWORD = "";

    static {
        createDatabaseIfNotExists();
    }

    private DBConnection() {}   // static utility – no instances

    private static void createDatabaseIfNotExists() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(HOST + "?useSSL=false&serverTimezone=UTC", USER, PASSWORD);
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
                System.out.println("Database checked/created successfully.");
            }
        } catch (Exception e) {
            System.out.println("Error creating database: " + e.getMessage());
        }
    }

    /**
     * Returns a new connection each time.
     * Callers must close it (use try-with-resources).
     */
    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC Driver not found.");
            e.printStackTrace();
            return null;
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
