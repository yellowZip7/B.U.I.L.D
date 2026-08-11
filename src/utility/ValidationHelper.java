package utility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public class ValidationHelper {

    // RFC 5322-inspired regex – far stricter than the old contains("@") check
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$"
    );

    private ValidationHelper() {}

    /** Returns true only for well-formed email addresses. */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /** Returns true if the email is already in the accounts table. */
    public static boolean isDuplicateEmail(String email) {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return false;
            String sql = "SELECT 1 FROM accounts WHERE email = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, email);
                ResultSet rs = ps.executeQuery();
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("Error checking duplicate email: " + e.getMessage());
            return false;
        }
    }

    /** Returns true for dates in ISO format (yyyy-MM-dd). */
    public static boolean isValidDate(String date) {
        try {
            LocalDate.parse(date);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /** Returns true if end date is on or after start date. */
    public static boolean isEndDateValid(String start, String end) {
        try {
            LocalDate s = LocalDate.parse(start);
            LocalDate e = LocalDate.parse(end);
            return !e.isBefore(s);
        } catch (Exception e) {
            return false;
        }
    }
}
