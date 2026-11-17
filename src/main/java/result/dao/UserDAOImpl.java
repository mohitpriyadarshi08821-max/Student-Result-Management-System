package result.dao;

import result.database.DatabaseConnection;
import result.exception.DatabaseException;
import result.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

/**
 * JDBC implementation of the UserDAO interface.
 */
public class UserDAOImpl implements UserDAO {

    @Override
    public Optional<User> findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return Optional.empty();
        }

        String trimmed = username.trim();
        String sql = "SELECT user_id, username, password, full_name, role FROM users WHERE LOWER(username) = LOWER(?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, trimmed);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("full_name"),
                            rs.getString("role")
                    );
                    return Optional.of(user);
                }
            }

            // Safe fallback: If users table is empty and 'admin' is logging in, seed default admin account safely
            if ("admin".equalsIgnoreCase(trimmed)) {
                try (Statement countStmt = conn.createStatement();
                     ResultSet countRs = countStmt.executeQuery("SELECT COUNT(*) FROM users")) {
                    if (countRs.next() && countRs.getInt(1) == 0) {
                        User defaultAdmin = new User(1, "admin", "admin123", "System Administrator", "ADMIN");
                        insert(defaultAdmin);
                        return Optional.of(defaultAdmin);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database error while searching for user: " + username, e);
        }
        return Optional.empty();
    }

    @Override
    public boolean insert(User user) {
        if (user == null) {
            return false;
        }

        String sql = "INSERT INTO users (username, password, full_name, role) VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE password = VALUES(password), full_name = VALUES(full_name), role = VALUES(role)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getFullName());
            stmt.setString(4, user.getRole());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        user.setUserId(keys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new DatabaseException("Database error while inserting user: " + user.getUsername(), e);
        }
    }

    @Override
    public boolean verifyCredentials(String username, String password) {
        Optional<User> userOpt = findByUsername(username);
        return userOpt.isPresent()
                && userOpt.get().getPassword() != null
                && userOpt.get().getPassword().equals(password);
    }
}
