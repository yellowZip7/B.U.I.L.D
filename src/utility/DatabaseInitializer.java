package utility;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void init() {
        Connection conn = DBConnection.getConnection();
        if (conn == null) return;

        try (Statement stmt = conn.createStatement()) {

            // Accounts table ni guys(Admin + Worker login credentials)
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS accounts (" +
                "  email    VARCHAR(100) PRIMARY KEY," +
                "  name     VARCHAR(100) NOT NULL," +
                "  password VARCHAR(100) NOT NULL," +
                "  role     VARCHAR(20)  NOT NULL" +
                ")"
            );

            // Projects table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS projects (" +
                "  project_id   VARCHAR(50)  PRIMARY KEY," +
                "  project_name VARCHAR(100) NOT NULL," +
                "  location     VARCHAR(100)," +
                "  start_date   VARCHAR(20)," +
                "  end_date     VARCHAR(20)," +
                "  budget       DOUBLE       NOT NULL," +
                "  status       VARCHAR(30)  DEFAULT 'Ongoing'" +
                ")"
            );

            // Workers table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS workers (" +
                "  worker_id   VARCHAR(50)  PRIMARY KEY," +
                "  worker_name VARCHAR(100) NOT NULL," +
                "  position    VARCHAR(100)," +
                "  email       VARCHAR(100) UNIQUE NOT NULL," +
                "  password    VARCHAR(100) NOT NULL," +
                "  salary      DOUBLE       NOT NULL DEFAULT 0.0," +
                "  status      VARCHAR(30)  NOT NULL DEFAULT 'Available'" +
                ")"
            );
            // Add salary column if it doesn't exist (for existing databases)
            try { stmt.executeUpdate("ALTER TABLE workers ADD COLUMN salary DOUBLE NOT NULL DEFAULT 0.0"); }
            catch (SQLException ignored) {}
            // Add status column if it doesn't exist (for existing databases)
            try { stmt.executeUpdate("ALTER TABLE workers ADD COLUMN status VARCHAR(30) NOT NULL DEFAULT 'Available'"); }
            catch (SQLException ignored) {}

            // Tasks table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS tasks (" +
                "  task_id    VARCHAR(50)  PRIMARY KEY," +
                "  project_id VARCHAR(50)  NOT NULL," +
                "  task_name  VARCHAR(100) NOT NULL," +
                "  status     VARCHAR(30)  DEFAULT 'Pending'," +
                "  worker_id  VARCHAR(50)  DEFAULT NULL," +
                "  FOREIGN KEY (project_id) REFERENCES projects(project_id) ON DELETE CASCADE," +
                "  FOREIGN KEY (worker_id)  REFERENCES workers(worker_id)   ON DELETE SET NULL" +
                ")"
            );

            // Resources table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS resources (" +
                "  material_id   VARCHAR(50)  PRIMARY KEY," +
                "  project_id    VARCHAR(50)  NOT NULL," +
                "  material_name VARCHAR(100) NOT NULL," +
                "  price         DOUBLE       NOT NULL," +
                "  quantity      INT          NOT NULL," +
                "  FOREIGN KEY (project_id) REFERENCES projects(project_id) ON DELETE CASCADE" +
                ")"
            );

            System.out.println("Database tables ready.");

        } catch (SQLException e) {
            System.out.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
