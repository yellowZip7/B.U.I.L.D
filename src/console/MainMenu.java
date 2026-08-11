package console;

import main.Main;
import model.Project;
import utility.DBConnection;
import utility.InputHelper;
import utility.ValidationHelper;

import java.sql.*;
import java.util.Scanner;

public class MainMenu {
    public Scanner sc = new Scanner(System.in);
    public int ch;
    public int projInd;
    public Project[] project;

    // ─────────────────────────────────────────────
    //  MENUS
    // ─────────────────────────────────────────────

    public void adminMenu() {
        System.out.println("\n╔══════════════════════════════════╗");
        System.out.println("║            MAIN MENU             ║");
        System.out.println("╠══════════════════════════════════╣");
        System.out.println("║ 1. Project Management            ║");
        System.out.println("║ 2. Task Management               ║");
        System.out.println("║ 3. Resource Management           ║");
        System.out.println("║ 4. Workforce Management          ║");
        System.out.println("║ 5. Budget Monitoring             ║");
        System.out.println("║ 6. Reports                       ║");
        System.out.println("║ 7. Logout                        ║");
        System.out.println("╚══════════════════════════════════╝");
        System.out.print("Enter your choice: ");
        ch = InputHelper.getInt(sc);
    }

    public void projectManagement() {
        System.out.println("\n╔══════════════════════════════╗");
        System.out.println("║     PROJECT MANAGEMENT       ║");
        System.out.println("╠══════════════════════════════╣");
        System.out.println("║ 1. Create Project            ║");
        System.out.println("║ 2. Update Project            ║");
        System.out.println("║ 3. Delete Project            ║");
        System.out.println("║ 4. Search Project            ║");
        System.out.println("║ 5. Return to Menu            ║");
        System.out.println("╚══════════════════════════════╝");
        System.out.println("Enter your choice: ");
        ch = InputHelper.getInt(sc);
    }

    public void taskManagement() {
        System.out.println("\n╔═════════════════════╗");
        System.out.println("║  ==Task Management== ║");
        System.out.println("║[1]Add Task           ║");
        System.out.println("║[2]Update Task Status ║");
        System.out.println("║[3]Assign Worker      ║");
        System.out.println("╚══════════════════════╝");
        System.out.println("Enter your choice: ");
        ch = InputHelper.getInt(sc);
    }

    public void resourceManagement() {
        System.out.println("\n╔════════════════════════╗");
        System.out.println("║==Resources Management==║");
        System.out.println("║[1]Allocate Resources   ║");
        System.out.println("║[2]Use Resources        ║");
        System.out.println("║[3]Delete Resources     ║");
        System.out.println("╚════════════════════════╝");
        System.out.println("Enter your choice: ");
        ch = InputHelper.getInt(sc);
    }

    public void workforceManagement() {
        System.out.println("\n╔════════════════════════╗");
        System.out.println("║==Workforce Management==║");
        System.out.println("║[1]Add Worker           ║");
        System.out.println("║[2]Update Assignment    ║");
        System.out.println("║[3]Remove Worker        ║");
        System.out.println("╚════════════════════════╝");
        System.out.println("Enter your choice: ");
        ch = InputHelper.getInt(sc);
    }

    public void reports() {
        System.out.println("\n╔═════════════════════════╗");
        System.out.println("║        REPORTS MENU     ║");
        System.out.println("╠═════════════════════════╣");
        System.out.println("║ 1. Progress Report      ║");
        System.out.println("║ 2. Financial Report     ║");
        System.out.println("║ 3. Resource Utilization ║");
        System.out.println("║ 4. Workforce Report     ║");
        System.out.println("║ 5. Back                 ║");
        System.out.println("╚═════════════════════════╝");
        System.out.print("Enter your choice: ");
        ch = InputHelper.getInt(sc);
    }

    // ─────────────────────────────────────────────
    //  PROJECT MANAGEMENT
    // ─────────────────────────────────────────────

    public void createProject() {
        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.printf("║ %-36s ║\n", "Enter Project Details");
        System.out.println("╚══════════════════════════════════════╝");

        System.out.print("Project Name: ");
        String projName = InputHelper.getString(sc);

        System.out.print("Project ID: ");
        String projID = InputHelper.getString(sc);

        // Check duplicate ID in DB
        if (projectExists(projID)) {
            System.out.println("Project ID already exists.");
            return;
        }

        System.out.print("Location: ");
        String projLoc = InputHelper.getString(sc);

        String sDate;
        while (true) {
            System.out.print("Start Date (YYYY-MM-DD): ");
            sDate = InputHelper.getString(sc);
            if (!ValidationHelper.isValidDate(sDate)) { System.out.println("Invalid date format."); continue; }
            break;
        }

        String eDate;
        while (true) {
            System.out.print("End Date (YYYY-MM-DD): ");
            eDate = InputHelper.getString(sc);
            if (!ValidationHelper.isValidDate(eDate)) { System.out.println("Invalid date format."); continue; }
            if (!ValidationHelper.isEndDateValid(sDate, eDate)) { System.out.println("End date cannot be before start date."); continue; }
            break;
        }

        System.out.print("Budget: ");
        double budget = InputHelper.getDouble(sc);

        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.printf("║ %-36s ║\n", "PROJECT SUMMARY");
        System.out.println("╠══════════════════════════════════════╣");
        System.out.printf("║ Name: %-30s ║\n", projName);
        System.out.printf("║ ID: %-32s ║\n", projID);
        System.out.printf("║ Location: %-26s ║\n", projLoc);
        System.out.printf("║ Duration: %-26s ║\n", sDate + " - " + eDate);
        System.out.printf("║ Budget: %,28.2f ║\n", budget);
        System.out.println("╚══════════════════════════════════════╝");
        System.out.print("\nCreate project? [1]Yes [2]No: ");
        ch = InputHelper.getInt(sc);

        if (ch == 1) {
            Connection conn = DBConnection.getConnection();
            String sql = "INSERT INTO projects (project_id, project_name, location, start_date, end_date, budget, status) VALUES (?,?,?,?,?,?,'Ongoing')";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, projID);
                ps.setString(2, projName);
                ps.setString(3, projLoc);
                ps.setString(4, sDate);
                ps.setString(5, eDate);
                ps.setDouble(6, budget);
                ps.executeUpdate();
                printSuccess("Project created!");
            } catch (SQLException e) {
                System.out.println("Database error creating project: " + e.getMessage());
            }
        } else {
            System.out.println("Project cancelled.");
        }
    }

    public void updateProject() {
        System.out.println("\n╔══════════════════════════════════╗");
        System.out.println("║        UPDATE PROJECT            ║");
        System.out.println("╚══════════════════════════════════╝");
        System.out.print("Enter Project ID to update: ");
        String id = InputHelper.getString(sc);

        if (!projectExists(id)) {
            System.out.println("Project not found.");
            return;
        }

        System.out.println("\nSelect field to update:");
        System.out.println("[1] Name");
        System.out.println("[2] Location");
        System.out.println("[3] Budget");
        System.out.print("Enter choice: ");
        ch = InputHelper.getInt(sc);

        Connection conn = DBConnection.getConnection();
        String sql = null;
        String newValue = null;
        double newBudget = 0;

        switch (ch) {
            case 1:
                System.out.print("Enter new project name: ");
                newValue = InputHelper.getString(sc);
                sql = "UPDATE projects SET project_name = ? WHERE project_id = ?";
                break;
            case 2:
                System.out.print("Enter new location: ");
                newValue = InputHelper.getString(sc);
                sql = "UPDATE projects SET location = ? WHERE project_id = ?";
                break;
            case 3:
                System.out.print("Enter new budget: ");
                newBudget = InputHelper.getDouble(sc);
                sql = "UPDATE projects SET budget = ? WHERE project_id = ?";
                break;
            default:
                System.out.println("Invalid choice.");
                return;
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (ch == 3) {
                ps.setDouble(1, newBudget);
            } else {
                ps.setString(1, newValue);
            }
            ps.setString(2, id);
            ps.executeUpdate();
            printSuccess("Project updated!");
        } catch (SQLException e) {
            System.out.println("Database error updating project: " + e.getMessage());
        }
    }

    public void deleteProject() {
        System.out.println("\n╔══════════════════════════════════╗");
        System.out.println("║        DELETE PROJECT            ║");
        System.out.println("╚══════════════════════════════════╝");
        System.out.print("Enter Project ID to delete: ");
        String deleteId = InputHelper.getString(sc);

        if (!projectExists(deleteId)) {
            System.out.println("Project not found.");
            return;
        }

        System.out.print("Are you sure you want to delete this project? [1]Yes [2]No: ");
        ch = InputHelper.getInt(sc);

        if (ch == 1) {
            Connection conn = DBConnection.getConnection();
            String sql = "DELETE FROM projects WHERE project_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, deleteId);
                ps.executeUpdate();
                printSuccess("Project deleted!");
            } catch (SQLException e) {
                System.out.println("Database error deleting project: " + e.getMessage());
            }
        } else {
            System.out.println("Project not deleted.");
        }
    }

    public void searchProject() {
        System.out.println("\n╔══════════════════════════════════╗");
        System.out.println("║        SEARCH PROJECT            ║");
        System.out.println("╚══════════════════════════════════╝");
        System.out.print("Enter Project ID to search: ");
        String id = InputHelper.getString(sc);

        Connection conn = DBConnection.getConnection();
        String sql = "SELECT * FROM projects WHERE project_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                System.out.println("\n╔══════════════════════════════════╗");
                System.out.println("║        PROJECT FOUND             ║");
                System.out.println("╚══════════════════════════════════╝");
                System.out.printf("| ID: %-32s |\n",   rs.getString("project_id"));
                System.out.printf("| Name: %-30s |\n", rs.getString("project_name"));
                System.out.printf("| Location: %-26s |\n", rs.getString("location"));
                System.out.printf("| Budget: %-28.2f |\n", rs.getDouble("budget"));
                System.out.printf("| Status: %-28s |\n",   rs.getString("status"));

                // Also show tasks for this project
                String taskSql = "SELECT task_name, status FROM tasks WHERE project_id = ?";
                try (PreparedStatement tps = conn.prepareStatement(taskSql);) {
                    tps.setString(1, id);
                    try (ResultSet trs = tps.executeQuery()) {
                        boolean hasTasks = false;
                        while (trs.next()) {
                            if (!hasTasks) { System.out.println("\nTasks:"); hasTasks = true; }
                            System.out.println("- " + trs.getString("task_name") + " | Status: " + trs.getString("status"));
                        }
                    }
                }
            } else {
                System.out.println("Project not found.");
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    //  TASK MANAGEMENT
    // ─────────────────────────────────────────────

    public void addTask() {
        System.out.println("\n╔══════════════════════════════════╗");
        System.out.println("║            ADD TASK              ║");
        System.out.println("╚══════════════════════════════════╝");
        System.out.print("Enter Project ID: ");
        String pid = InputHelper.getString(sc);

        if (!projectExists(pid)) {
            System.out.println("Project not found.");
            return;
        }

        System.out.print("Task Name: ");
        String taskName = InputHelper.getString(sc);

        System.out.print("Task ID: ");
        String taskID = InputHelper.getString(sc);

        System.out.print("Deadline: ");
        InputHelper.getString(sc); // captured but not stored in current schema

        System.out.println("Priority Level:");
        System.out.println("1 Low  2 Medium  3 High");
        System.out.print("Enter choice: ");
        InputHelper.getInt(sc); // captured but not stored in current schema

        Connection conn = DBConnection.getConnection();
        String sql = "INSERT INTO tasks (task_id, project_id, task_name, status) VALUES (?, ?, ?, 'Pending')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, taskID);
            ps.setString(2, pid);
            ps.setString(3, taskName);
            ps.executeUpdate();
            printSuccess("Task added!");
        } catch (SQLException e) {
            System.out.println("Database error adding task: " + e.getMessage());
        }
    }

    public void updateTaskStatus() {
        System.out.println("\n╔══════════════════════════════════╗");
        System.out.println("║      UPDATE TASK STATUS          ║");
        System.out.println("╚══════════════════════════════════╝");
        System.out.print("Enter Project ID: ");
        String pid = InputHelper.getString(sc);

        if (!projectExists(pid)) { System.out.println("Project not found."); return; }

        System.out.print("Enter Task ID: ");
        String tid = InputHelper.getString(sc);

        System.out.println("1 Pending  2 In Progress  3 Completed");
        int choice = InputHelper.getInt(sc);

        String status;
        switch (choice) {
            case 1: status = "Pending";     break;
            case 2: status = "In Progress"; break;
            case 3: status = "Completed";   break;
            default: System.out.println("Invalid choice."); return;
        }

        Connection conn = DBConnection.getConnection();
        String sql = "UPDATE tasks SET status = ? WHERE task_id = ? AND project_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, tid);
            ps.setString(3, pid);
            int rows = ps.executeUpdate();
            if (rows > 0) printSuccess("Task updated!");
            else System.out.println("Task not found.");
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    public void assignWorkerToTask() {
        System.out.println("\n╔══════════════════════════════════╗");
        System.out.println("║           ASSIGN WORKER          ║");
        System.out.println("╚══════════════════════════════════╝");
        System.out.print("Enter Project ID: ");
        String pid = InputHelper.getString(sc);

        if (!projectExists(pid)) { System.out.println("Project not found."); return; }

        System.out.print("Enter Task ID: ");
        String tid = InputHelper.getString(sc);

        // List available workers
        printAvailableWorkers();

        System.out.print("Enter Worker Name: ");
        String wName = InputHelper.getString(sc);

        Connection conn = DBConnection.getConnection();
        String wSql = "SELECT worker_id FROM workers WHERE worker_name = ?";
        try (PreparedStatement wps = conn.prepareStatement(wSql)) {
            wps.setString(1, wName);
            ResultSet rs = wps.executeQuery();
            if (!rs.next()) { System.out.println("Worker not found."); return; }
            String wid = rs.getString("worker_id");

            String sql = "UPDATE tasks SET worker_id = ? WHERE task_id = ? AND project_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, wid);
                ps.setString(2, tid);
                ps.setString(3, pid);
                int rows = ps.executeUpdate();
                if (rows > 0) printSuccess("Worker assigned!");
                else System.out.println("Task not found.");
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    //  RESOURCE MANAGEMENT
    // ─────────────────────────────────────────────

    public void allocateResources() {
        System.out.println("\n╔══════════════════════════════════╗");
        System.out.println("║        ALLOCATE RESOURCES        ║");
        System.out.println("╚══════════════════════════════════╝");
        System.out.print("Enter Project ID: ");
        String pid = InputHelper.getString(sc);

        if (!projectExists(pid)) { System.out.println("Project not found."); return; }

        System.out.print("Material ID: ");
        String mID = InputHelper.getString(sc);

        System.out.print("Material Name: ");
        String name = InputHelper.getString(sc);

        System.out.print("Price: ");
        double price = InputHelper.getDouble(sc);

        System.out.print("Quantity: ");
        int qty = InputHelper.getInt(sc);

        Connection conn = DBConnection.getConnection();
        String sql = "INSERT INTO resources (material_id, project_id, material_name, price, quantity) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, mID);
            ps.setString(2, pid);
            ps.setString(3, name);
            ps.setDouble(4, price);
            ps.setInt(5, qty);
            ps.executeUpdate();
            printSuccess("Resource added!");
        } catch (SQLException e) {
            System.out.println("Database error adding resource: " + e.getMessage());
        }
    }

    public void useResources() {
        System.out.println("\n╔══════════════════════════════════╗");
        System.out.println("║           USE RESOURCES          ║");
        System.out.println("╚══════════════════════════════════╝");
        System.out.print("Enter Project ID: ");
        String pid = InputHelper.getString(sc);

        if (!projectExists(pid)) { System.out.println("Project not found."); return; }

        System.out.print("Enter Material ID: ");
        String mID = InputHelper.getString(sc);

        Connection conn = DBConnection.getConnection();
        String fetchSql = "SELECT price, quantity FROM resources WHERE material_id = ? AND project_id = ?";
        try (PreparedStatement fps = conn.prepareStatement(fetchSql)) {
            fps.setString(1, mID);
            fps.setString(2, pid);
            ResultSet rs = fps.executeQuery();

            if (!rs.next()) { System.out.println("Material not found."); return; }

            double price = rs.getDouble("price");
            int currentQty = rs.getInt("quantity");

            System.out.print("Enter quantity to use: ");
            int qty = InputHelper.getInt(sc);

            if (qty > currentQty) { System.out.println("Not enough stock."); return; }

            double cost = qty * price;

            // Check project budget
            double budget = getProjectBudget(pid, conn);
            if (budget < cost) { System.out.println("Not enough budget!"); return; }

            // Deduct stock
            String updateStock = "UPDATE resources SET quantity = quantity - ? WHERE material_id = ? AND project_id = ?";
            try (PreparedStatement ups = conn.prepareStatement(updateStock)) {
                ups.setInt(1, qty);
                ups.setString(2, mID);
                ups.setString(3, pid);
                ups.executeUpdate();
            }

            // Deduct budget
            String updateBudget = "UPDATE projects SET budget = budget - ? WHERE project_id = ?";
            try (PreparedStatement bps = conn.prepareStatement(updateBudget)) {
                bps.setDouble(1, cost);
                bps.setString(2, pid);
                bps.executeUpdate();
            }

            printSuccess("Resource used!");
            System.out.println("Cost deducted: " + cost);
            System.out.printf("Remaining budget: %.2f%n", budget - cost);

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    public void deleteResources() {
        System.out.println("\n╔══════════════════════════════════╗");
        System.out.println("║          DELETE RESOURCES        ║");
        System.out.println("╚══════════════════════════════════╝");
        System.out.print("Enter Project ID: ");
        String pid = InputHelper.getString(sc);

        if (!projectExists(pid)) { System.out.println("Project not found."); return; }

        System.out.print("Enter Material ID to delete: ");
        String mID = InputHelper.getString(sc);

        Connection conn = DBConnection.getConnection();
        String sql = "DELETE FROM resources WHERE material_id = ? AND project_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, mID);
            ps.setString(2, pid);
            int rows = ps.executeUpdate();
            if (rows > 0) printSuccess("Resource deleted!");
            else System.out.println("Resource not found.");
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    //  WORKFORCE MANAGEMENT
    // ─────────────────────────────────────────────

    public void assignWorker() {
        System.out.println("\n╔══════════════════════════════════╗");
        System.out.println("║            ADD WORKER            ║");
        System.out.println("╚══════════════════════════════════╝");

        System.out.print("Worker ID: ");
        String id = InputHelper.getString(sc);

        System.out.print("Worker Name: ");
        String name = InputHelper.getString(sc);

        System.out.print("Position: ");
        String position = InputHelper.getString(sc);

        System.out.print("Email: ");
        String email = InputHelper.getString(sc);

        if (ValidationHelper.isDuplicateEmail(email)) {
            System.out.println("Email already used.");
            return;
        }

        System.out.print("Password: ");
        String password = InputHelper.getString(sc);

        Connection conn = DBConnection.getConnection();

        // Insert into workers table
        String wSql = "INSERT INTO workers (worker_id, worker_name, position, email, password) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(wSql)) {
            ps.setString(1, id);
            ps.setString(2, name);
            ps.setString(3, position);
            ps.setString(4, email);
            ps.setString(5, password);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Database error adding worker: " + e.getMessage());
            return;
        }

        // Also create login account for the worker
        String aSql2 = "INSERT INTO accounts (email, name, password, role) VALUES (?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(aSql2)) {
            ps.setString(1, email);
            ps.setString(2, name);
            ps.setString(3, password);
            ps.setString(4, "Worker");
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Note: Could not create login account for worker: " + e.getMessage());
        }

        printSuccess("Worker added!");
    }

    public void updateAssignment() {
        System.out.println("\n╔══════════════════════════════════╗");
        System.out.println("║         UPDATE ASSIGNMENT        ║");
        System.out.println("╚══════════════════════════════════╝");
        System.out.print("Enter Project ID: ");
        String pid = InputHelper.getString(sc);

        if (!projectExists(pid)) { System.out.println("Project not found."); return; }

        System.out.print("Enter Task ID: ");
        String tid = InputHelper.getString(sc);

        printAvailableWorkers();

        System.out.print("Enter Worker ID: ");
        String wid = InputHelper.getString(sc);

        Connection conn = DBConnection.getConnection();
        String sql = "UPDATE tasks SET worker_id = ? WHERE task_id = ? AND project_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, wid);
            ps.setString(2, tid);
            ps.setString(3, pid);
            int rows = ps.executeUpdate();
            if (rows > 0) printSuccess("Assignment updated!");
            else System.out.println("Task not found.");
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    public void deleteWorker() {
        System.out.println("\n╔══════════════════════════════════╗");
        System.out.println("║           DELETE WORKER          ║");
        System.out.println("╚══════════════════════════════════╝");
        System.out.print("Enter worker ID: ");
        String id = InputHelper.getString(sc);

        Connection conn = DBConnection.getConnection();

        // Get the worker's email first so we can remove their account too
        String emailSql = "SELECT email FROM workers WHERE worker_id = ?";
        String workerEmail = null;
        try (PreparedStatement eps = conn.prepareStatement(emailSql)) {
            eps.setString(1, id);
            ResultSet rs = eps.executeQuery();
            if (rs.next()) workerEmail = rs.getString("email");
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return;
        }

        if (workerEmail == null) { System.out.println("Worker not found."); return; }

        String sql = "DELETE FROM workers WHERE worker_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return;
        }

        // Also remove their login account
        String accSql = "DELETE FROM accounts WHERE email = ?";
        try (PreparedStatement ps = conn.prepareStatement(accSql)) {
            ps.setString(1, workerEmail);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Note: Could not remove worker login account: " + e.getMessage());
        }

        printSuccess("Worker removed!");
    }

    // ─────────────────────────────────────────────
    //  BUDGET MONITORING
    // ─────────────────────────────────────────────

    public void budgetMonitoring() {
        System.out.println("Enter project ID: ");
        String id = InputHelper.getString(sc);

        if (!projectExists(id)) { System.out.println("Project not found."); return; }

        boolean running = true;
        while (running) {
            System.out.println("\n╔═════════════════════════╗");
            System.out.println("║  ==Budget Monitoring==   ║");
            System.out.println("║[1] Worker Salary         ║");
            System.out.println("║[2] View Worker Payroll   ║");
            System.out.println("║[3] View Material Expenses║");
            System.out.println("║[4] Back                  ║");
            System.out.println("╚══════════════════════════╝");
            System.out.print("Enter your choice: ");
            ch = InputHelper.getInt(sc);

            switch (ch) {
                case 1 -> workerSalary(id);
                case 2 -> viewWorkerPayroll(id);
                case 3 -> viewMaterialExpenses(id);
                case 4 -> running = false;
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    public void workerSalary(String pid) {
        System.out.println("\n╔══════════════════════════════════╗");
        System.out.println("║           WORKER SALARY          ║");
        System.out.println("╚══════════════════════════════════╝");

        System.out.print("Enter Task ID: ");
        String tid = InputHelper.getString(sc);

        Connection conn = DBConnection.getConnection();
        String sql = "SELECT t.task_name, w.worker_name FROM tasks t " +
                     "JOIN workers w ON t.worker_id = w.worker_id " +
                     "WHERE t.task_id = ? AND t.project_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tid);
            ps.setString(2, pid);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) { System.out.println("Task not found or no worker assigned."); return; }

            System.out.println("Worker: " + rs.getString("worker_name"));
            System.out.print("Enter salary amount: ");
            double salary = InputHelper.getDouble(sc);

            double budget = getProjectBudget(pid, conn);
            if (budget < salary) { System.out.println("Not enough project budget."); return; }

            String updateSql = "UPDATE projects SET budget = budget - ? WHERE project_id = ?";
            try (PreparedStatement ups = conn.prepareStatement(updateSql)) {
                ups.setDouble(1, salary);
                ups.setString(2, pid);
                ups.executeUpdate();
            }

            printSuccess("Salary recorded!");
            System.out.printf("Remaining Budget: %.2f%n", budget - salary);

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // Overload kept for compatibility if called with int index elsewhere
    public void workerSalary(int projIndex) {
        // Not used in JDBC version; budgetMonitoring now uses project ID strings
    }

    public void viewWorkerPayroll(String pid) {
        System.out.println("\n╔══════════════════════════════════╗");
        System.out.println("║           WORKER PAYROLL         ║");
        System.out.println("╚══════════════════════════════════╝");

        Connection conn = DBConnection.getConnection();
        String sql = "SELECT t.task_name, w.worker_name FROM tasks t " +
                     "JOIN workers w ON t.worker_id = w.worker_id " +
                     "WHERE t.project_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pid);
            ResultSet rs = ps.executeQuery();
            double fixedSalary = 5000;
            System.out.println("\n--- Payroll ---");
            boolean any = false;
            while (rs.next()) {
                any = true;
                System.out.println("Worker: " + rs.getString("worker_name") +
                        " | Task: " + rs.getString("task_name") +
                        " | Salary: " + fixedSalary);
            }
            if (!any) System.out.println("No payroll data available.");
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    public void viewWorkerPayroll(int projIndex) { /* legacy stub */ }

    public void viewMaterialExpenses(String pid) {
        System.out.println("\n╔══════════════════════════════════╗");
        System.out.println("║          MATERIAL EXPENSES       ║");
        System.out.println("╚══════════════════════════════════╝");

        Connection conn = DBConnection.getConnection();
        String sql = "SELECT material_name, price, quantity FROM resources WHERE project_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pid);
            ResultSet rs = ps.executeQuery();
            double totalCost = 0;
            System.out.println("\n--- Materials ---");
            boolean any = false;
            while (rs.next()) {
                any = true;
                double cost = rs.getDouble("price") * rs.getInt("quantity");
                totalCost += cost;
                System.out.println("Material: " + rs.getString("material_name") +
                        " | Quantity: " + rs.getInt("quantity") + " | Cost: " + cost);
            }
            if (!any) { System.out.println("No resources allocated."); return; }
            System.out.println("Total Material Expenses: " + totalCost);
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    public void viewMaterialExpenses(int projIndex) { /* legacy stub */ }

    // ─────────────────────────────────────────────
    //  REPORTS
    // ─────────────────────────────────────────────

    public void progressReport() {
        System.out.println("\n╔══════════════════════════════════╗");
        System.out.println("║      PROJECT PROGRESS REPORT     ║");
        System.out.println("╚══════════════════════════════════╝");

        Connection conn = DBConnection.getConnection();
        String sql = "SELECT * FROM projects";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            boolean any = false;
            while (rs.next()) {
                any = true;
                String pid = rs.getString("project_id");
                System.out.println("\n----------------------------");
                System.out.println("Project Name: " + rs.getString("project_name"));
                System.out.println("Project ID: "   + pid);
                System.out.println("Location: "     + rs.getString("location"));
                System.out.println("Start Date: "   + rs.getString("start_date"));
                System.out.println("End Date: "     + rs.getString("end_date"));
                System.out.println("Budget: "       + rs.getDouble("budget"));

                // Tasks
                String tSql = "SELECT t.task_name, t.status, w.worker_name " +
                              "FROM tasks t LEFT JOIN workers w ON t.worker_id = w.worker_id " +
                              "WHERE t.project_id = ?";
                try (PreparedStatement tps = conn.prepareStatement(tSql)) {
                    tps.setString(1, pid);
                    try (ResultSet trs = tps.executeQuery()) {
                    int total = 0, completed = 0;
                    System.out.println("\n--- Tasks ---");
                    boolean hasTasks = false;
                    while (trs.next()) {
                        hasTasks = true;
                        total++;
                        System.out.println("\nTask: "   + trs.getString("task_name"));
                        System.out.println("Status: " + trs.getString("status"));
                        String wn = trs.getString("worker_name");
                        System.out.println("Worker: " + (wn != null ? wn : "None"));
                        if ("Completed".equalsIgnoreCase(trs.getString("status"))) completed++;
                    }
                    if (!hasTasks) System.out.println("No tasks assigned.");
                    int percent = (total > 0) ? (completed * 100 / total) : 0;
                    System.out.println("\nProject Completion: " + percent + "%");
                    } // end ResultSet try
                }
            }
            if (!any) System.out.println("No projects available.");

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    public void financialReport() {
        System.out.println("\n╔══════════════════════════════════╗");
        System.out.println("║         FINANCIAL REPORT         ║");
        System.out.println("╚══════════════════════════════════╝");

        Connection conn = DBConnection.getConnection();
        String sql = "SELECT p.project_name, p.budget, COUNT(t.task_id) AS task_count " +
                     "FROM projects p LEFT JOIN tasks t ON p.project_id = t.project_id " +
                     "GROUP BY p.project_id, p.project_name, p.budget";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            boolean any = false;
            while (rs.next()) {
                any = true;
                double budget = rs.getDouble("budget");
                double estimated = rs.getInt("task_count") * 1000.0;
                double remaining = budget - estimated;

                System.out.println("\n-----------------------");
                System.out.println("Project: " + rs.getString("project_name"));
                System.out.println("Budget: " + budget);
                System.out.println("Estimated Spending: " + estimated);
                System.out.println("Remaining Budget: " + remaining);
            }
            if (!any) System.out.println("No projects available.");

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    public void resourceUtilizationReport() {
        System.out.println("\n╔══════════════════════════════════╗");
        System.out.println("║   RESOURCE UTILIZATION REPORT    ║");
        System.out.println("╚══════════════════════════════════╝");

        Connection conn = DBConnection.getConnection();
        String sql = "SELECT p.project_name, r.material_name, r.material_id, r.quantity, r.price " +
                     "FROM projects p LEFT JOIN resources r ON p.project_id = r.project_id " +
                     "ORDER BY p.project_id";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            String lastProject = "";
            double projectTotal = 0;
            boolean any = false;

            while (rs.next()) {
                any = true;
                String projName = rs.getString("project_name");

                if (!projName.equals(lastProject)) {
                    if (!lastProject.isEmpty()) {
                        System.out.println("Total Resources Value: " + projectTotal);
                    }
                    System.out.println("\nProject: " + projName);
                    System.out.println("\n--- Resources ---");
                    lastProject = projName;
                    projectTotal = 0;
                }

                String mName = rs.getString("material_name");
                if (mName == null) { System.out.println("No resources allocated."); continue; }

                double totalValue = rs.getDouble("price") * rs.getInt("quantity");
                projectTotal += totalValue;
                System.out.println("Material: " + mName +
                        " | ID: " + rs.getString("material_id") +
                        " | Quantity: " + rs.getInt("quantity") +
                        " | Unit Price: " + rs.getDouble("price") +
                        " | Total Value: " + totalValue);
            }
            if (!lastProject.isEmpty()) System.out.println("Total Resources Value: " + projectTotal);
            if (!any) System.out.println("No projects available.");

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    public void workforceReport() {
        System.out.println("\n╔══════════════════════════════════╗");
        System.out.println("║         WORKFORCE REPORT          ║");
        System.out.println("╚══════════════════════════════════╝");

        Connection conn = DBConnection.getConnection();
        String sql = "SELECT p.project_name, t.task_name, w.worker_name " +
                     "FROM projects p " +
                     "JOIN tasks t ON p.project_id = t.project_id " +
                     "JOIN workers w ON t.worker_id = w.worker_id " +
                     "ORDER BY p.project_id";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            String lastProject = "";
            boolean any = false;

            while (rs.next()) {
                any = true;
                String projName = rs.getString("project_name");
                if (!projName.equals(lastProject)) {
                    System.out.println("\nProject: " + projName);
                    lastProject = projName;
                }
                System.out.println("----------------------");
                System.out.println("Task: "   + rs.getString("task_name"));
                System.out.println("Worker: " + rs.getString("worker_name"));
            }
            if (!any) System.out.println("No assigned workers found.");

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    //  WORKER VIEW
    // ─────────────────────────────────────────────

    public void workerMenu() {
        System.out.println("\n╔══════════════════════╗");
        System.out.println("║    ==MAIN MENU==     ║");
        System.out.println("║[1] View Assigned Tasks║");
        System.out.println("║[2] Logout            ║");
        System.out.println("╚══════════════════════╝");
        System.out.print("Enter your choice: ");
        ch = InputHelper.getInt(sc);
    }

    public void viewAssignedTasks() {
        String currentEmail = SignIn.currentUserEmail;

        System.out.println("\n╔══════════════════════════════════╗");
        System.out.println("║          ASSIGNED TASKS          ║");
        System.out.println("╚══════════════════════════════════╝");

        Connection conn = DBConnection.getConnection();
        String sql = "SELECT p.project_name, t.task_name, t.status " +
                     "FROM tasks t " +
                     "JOIN projects p ON t.project_id = p.project_id " +
                     "JOIN workers w  ON t.worker_id  = w.worker_id " +
                     "WHERE w.email = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, currentEmail);
            ResultSet rs = ps.executeQuery();
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.println("\nProject: " + rs.getString("project_name"));
                System.out.println("Task: "    + rs.getString("task_name"));
                System.out.println("Status: "  + rs.getString("status"));
            }
            if (!found) System.out.println("No assigned tasks.");
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    //  LOGOUT
    // ─────────────────────────────────────────────

    public void adminLogout() {
        System.out.print("Are you sure you want to logout? [1]Yes [2]No: ");
        ch = InputHelper.getInt(sc);
        if (ch == 1) {
            printSuccess("You are logged out!");
            Main.signIn.isRunningAdmin = false;
            Main.isRunning = true;
        }
    }

    public void workerLogout() {
        System.out.print("Are you sure you want to logout? [1]Yes [2]No: ");
        ch = InputHelper.getInt(sc);
        if (ch == 1) {
            printSuccess("You are logged out!");
            Main.signIn.isRunningWorker = false;
            Main.isRunning = true;
        }
    }

    // ─────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────

    private boolean projectExists(String pid) {
        Connection conn = DBConnection.getConnection();
        if (conn == null) return false;
        String sql = "SELECT 1 FROM projects WHERE project_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pid);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            return false;
        }
    }

    private double getProjectBudget(String pid, Connection conn) throws SQLException {
        String sql = "SELECT budget FROM projects WHERE project_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pid);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getDouble("budget") : 0;
        }
    }

    private void printAvailableWorkers() {
        Connection conn = DBConnection.getConnection();
        String sql = "SELECT worker_id, worker_name FROM workers";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            System.out.println("\nAvailable Workers:");
            while (rs.next()) {
                System.out.println(rs.getString("worker_id") + " - " + rs.getString("worker_name"));
            }
        } catch (SQLException e) {
            System.out.println("Could not load workers: " + e.getMessage());
        }
    }

    private void printSuccess(String message) {
        System.out.println("\n╔══════════════════════════════════╗");
        System.out.println("║             SUCCESS              ║");
        System.out.println("╠══════════════════════════════════╣");
        System.out.printf("║ %-32s ║\n", message);
        System.out.println("╚══════════════════════════════════╝");
    }
}
