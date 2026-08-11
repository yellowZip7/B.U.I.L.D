# B.U.I.L.D.
**B**uilding and **U**tility **I**ntegrated **L**ogistics **D**atabase

B.U.I.L.D. is a Java desktop application for managing construction/building operations. It centralizes project tracking, task assignment, workforce records, material/resource inventory, and budget monitoring behind a role-based (Admin / Worker) login system, backed by a MySQL database.

## Features

- **Authentication** — Admin sign-up and login, with worker accounts provisioned by an admin. Passwords are hashed via `PasswordUtil`, and active sessions are tracked with `SessionManager`.
- **Project Management** — Create and update projects with name, location, start/end dates, budget, and status; each project holds its own tasks and resources.
- **Task Management** — Create tasks with a deadline and priority (1–5), track status (e.g. Pending), and assign tasks to specific workers.
- **Workforce Management** — Maintain worker records (ID, name, position) and their login credentials.
- **Resource Management** — Track construction materials (ID, name, price, quantity) with stock in/out operations.
- **Budget Monitoring** — Monitor project spending against allocated budgets.
- **Reports** — Generate summary reports across projects, tasks, and resources.
- **Two dashboards** — Separate GUI dashboards for Admins (full management access) and Workers (view/update assigned tasks).
- **Console mode** — A secondary text-based console interface (`console/`) is also included alongside the Swing GUI.

## Tech Stack

- **Language:** Java (Swing for GUI)
- **Database:** MySQL, accessed via JDBC (`com.mysql.cj.jdbc.Driver`)
- **Build/IDE:** IntelliJ IDEA project (`B.U.I.L.D.iml`)

## Project Structure

```
IC124_SA3_Quilla_fixed/
├── src/
│   ├── main/
│   │   └── Main.java              # Application entry point & navigation logic
│   ├── model/                     # Core data classes
│   │   ├── Account.java
│   │   ├── Project.java
│   │   ├── Task.java
│   │   ├── Worker.java
│   │   └── Resource.java
│   ├── gui/                       # Swing UI
│   │   ├── LoginFrame.java
│   │   ├── SignUpFrame.java
│   │   ├── DashboardFrame.java
│   │   ├── Theme.java
│   │   ├── admin/
│   │   │   └── AdminDashboardFrame.java
│   │   ├── worker/
│   │   │   ├── WorkerDashboardFrame.java
│   │   │   └── WorkerTasksPanel.java
│   │   ├── modules/                # Feature panels
│   │   │   ├── ProjectManagementPanel.java
│   │   │   ├── TaskManagementPanel.java
│   │   │   ├── ResourceManagementPanel.java
│   │   │   ├── WorkforceManagementPanel.java
│   │   │   ├── BudgetMonitoringPanel.java
│   │   │   └── ReportsPanel.java
│   │   └── components/             # Reusable UI components
│   │       ├── SidebarButton.java
│   │       ├── StatsCard.java
│   │       └── RoundedButton.java
│   ├── utility/                   # Support services
│   │   ├── DBConnection.java
│   │   ├── DatabaseInitializer.java
│   │   ├── PasswordUtil.java
│   │   ├── SessionManager.java
│   │   ├── InputHelper.java
│   │   └── ValidationHelper.java
│   └── console/                   # Console-based interface
│       ├── WelcomeScreen.java
│       ├── SignIn.java
│       ├── MainMenu.java
│       └── Exit.java
└── B.U.I.L.D.iml
```

## Prerequisites

- **JDK 8+**
- **MySQL Server** running locally on port `3306`
- **MySQL Connector/J** (JDBC driver) on the classpath

## Setup & Installation

1. **Clone or extract** the project.
2. **Start MySQL** locally. The app connects with:
   - Host: `localhost:3306`
   - User: `root`
   - Password: *(empty by default)*
   
   Update the credentials in `src/utility/DBConnection.java` if your local MySQL setup differs.
3. **Database creation is automatic** — `DBConnection` creates the `build_db` database if it doesn't already exist, and `DatabaseInitializer` creates the required tables (`accounts`, `projects`, `workers`, `tasks`, `resources`) on startup.
4. **Add the MySQL Connector/J JAR** to your project's classpath (via IntelliJ's library settings or your build tool of choice).
5. **Run the application** by executing `src/main/Main.java`.

## Usage

1. On launch, the **Login screen** appears.
2. **Admins** can sign up for a new account, then log in to access the full Admin Dashboard: Project Management, Task Management, Resource Management, Workforce Management, Budget Monitoring, and Reports.
3. **Workers** log in with credentials provisioned by an admin to access the Worker Dashboard, where they can view and update their assigned tasks.
4. Use the **Logout** option in either dashboard to end the session and return to the Login screen.

## Notes

- `DBConnection` opens a fresh connection per call (rather than sharing one static connection) to safely support concurrent access from the Swing event thread.
- The console interface (`console/` package) provides an alternate, text-based way to interact with the system alongside the GUI.

## Authors

Developed as part of the IC124 course (Programming Paradigm 2).