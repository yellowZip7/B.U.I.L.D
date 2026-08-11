package utility;

/**
 * Holds the currently logged-in user's session data.
 * Replaces the old console.SignIn.currentUserEmail static field
 * with a proper, centralized session object.
 */
public class SessionManager {

    private static String currentEmail = null;
    private static String currentRole  = null;
    private static String currentName  = null;

    private SessionManager() {}   // static utility – no instances

    /** Called on successful login. */
    public static void login(String email, String role, String name) {
        currentEmail = email;
        currentRole  = role;
        currentName  = name;
        // Keep legacy field in sync so any console code still compiles
        console.SignIn.currentUserEmail = email;
    }

    /** Called on logout – wipes all session data. */
    public static void logout() {
        currentEmail = null;
        currentRole  = null;
        currentName  = null;
        console.SignIn.currentUserEmail = null;
    }

    public static String getEmail() { return currentEmail; }
    public static String getRole()  { return currentRole;  }
    public static String getName()  { return currentName;  }
    public static boolean isLoggedIn() { return currentEmail != null; }
}
