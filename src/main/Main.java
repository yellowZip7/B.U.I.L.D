package main;

import gui.LoginFrame;
import gui.SignUpFrame;

import gui.admin.AdminDashboardFrame;
import gui.worker.WorkerDashboardFrame;

import gui.modules.ProjectManagementPanel;
import gui.modules.TaskManagementPanel;
import gui.modules.ResourceManagementPanel;
import gui.modules.WorkforceManagementPanel;
import gui.modules.BudgetMonitoringPanel;
import gui.modules.ReportsPanel;

import gui.worker.WorkerTasksPanel;

import model.Account;
import console.SignIn;
import utility.DatabaseInitializer;

public class Main {

    public static boolean isRunning = true;

    // =========================================
    // ORIGINAL VARIABLES
    // =========================================

    public static Account[] acc = new Account[100];

    // CURRENT LOGGED USER
    public static Account currentUser;

    // Console sign-in reference (used by MainMenu logout)
    public static SignIn signIn = new SignIn();

    // GUI FRAMES
    public static LoginFrame loginFrame;
    public static SignUpFrame signUpFrame;

    public static AdminDashboardFrame adminDashboard;
    public static WorkerDashboardFrame workerDashboard;

    // =========================================
    // MAIN METHOD
    // =========================================

    public static void main(String[] args) {

        // Initialize DB tables on startup
        DatabaseInitializer.init();

        javax.swing.SwingUtilities.invokeLater(() -> {

            openLogin();

        });
    }

    // =========================================
    // LOGIN SCREEN
    // =========================================

    public static void openLogin() {

        if (loginFrame != null) {
            loginFrame.dispose();
        }
        loginFrame = new LoginFrame();
        loginFrame.setVisible(true);
    }

    // =========================================
    // SIGN UP SCREEN
    // =========================================

    public static void openSignUp() {

        if (signUpFrame != null) {
            signUpFrame.dispose();
        }
        signUpFrame = new SignUpFrame();
        signUpFrame.setVisible(true);
    }

    // =========================================
    // ADMIN DASHBOARD
    // =========================================

    public static void openAdminDashboard() {

        adminDashboard = new AdminDashboardFrame();
        adminDashboard.setVisible(true);

    }

    // =========================================
    // WORKER DASHBOARD
    // =========================================

    public static void openWorkerDashboard() {

        workerDashboard = new WorkerDashboardFrame();
        workerDashboard.setVisible(true);

    }

    // =========================================
    // ADMIN NAVIGATION → GUI VERSION
    // =========================================

    public static void adminNavigation(int choice) {

        switch(choice){

            // =====================================
            // PROJECT MANAGEMENT
            // =====================================

            case 1: {

                adminDashboard.setContentPanel(
                        new ProjectManagementPanel()
                );

                break;
            }

            // =====================================
            // TASK MANAGEMENT
            // =====================================

            case 2: {

                adminDashboard.setContentPanel(
                        new TaskManagementPanel()
                );

                break;
            }

            // =====================================
            // RESOURCE MANAGEMENT
            // =====================================

            case 3: {

                adminDashboard.setContentPanel(
                        new ResourceManagementPanel()
                );

                break;
            }

            // =====================================
            // WORKFORCE MANAGEMENT
            // =====================================

            case 4: {

                adminDashboard.setContentPanel(
                        new WorkforceManagementPanel()
                );

                break;
            }

            // =====================================
            // BUDGET MONITORING
            // =====================================

            case 5: {

                adminDashboard.setContentPanel(
                        new BudgetMonitoringPanel()
                );

                break;
            }

            // =====================================
            // REPORTS
            // =====================================

            case 6: {

                adminDashboard.setContentPanel(
                        new ReportsPanel()
                );

                break;
            }

            // =====================================
            // LOGOUT
            // =====================================

            case 7: {

                adminLogout();

                break;
            }
        }
    }

    // =========================================
    // WORKER NAVIGATION
    // =========================================

    public static void workerNavigation(int choice){

        switch(choice){

            // =====================================
            // VIEW ASSIGNED TASKS
            // =====================================

            case 1: {

                workerDashboard.setContentPanel(
                        new WorkerTasksPanel()
                );

                break;
            }

            // =====================================
            // WORKER LOGOUT
            // =====================================

            case 2: {

                workerLogout();

                break;
            }
        }
    }

    // =========================================
    // ADMIN LOGOUT
    // =========================================

    public static void adminLogout(){

        utility.SessionManager.logout();

        if(adminDashboard != null){
            adminDashboard.dispose();
        }

        openLogin();
    }

    // =========================================
    // WORKER LOGOUT
    // =========================================

    public static void workerLogout(){

        utility.SessionManager.logout();

        if(workerDashboard != null){
            workerDashboard.dispose();
        }

        openLogin();
    }
}
