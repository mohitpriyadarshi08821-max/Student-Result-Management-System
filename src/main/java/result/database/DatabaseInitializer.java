package result.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility class to verify and initialize the required database schema,
 * tables (users, students, subjects, results), and initial administrative seed data.
 */
public class DatabaseInitializer {

    private static volatile boolean isInitialized = false;

    public static synchronized void initializeDatabase() {
        if (isInitialized) {
            return;
        }

        // Step 1: Ensure database student_result_db exists
        ensureDatabaseExists();

        // Step 2: Ensure all required tables and seed data exist
        boolean tablesReady = ensureTablesAndSeeds();
        if (tablesReady) {
            isInitialized = true;
        }
    }

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  Testing & Initializing MySQL Database Schema... ");
        System.out.println("==================================================");
        try {
            initializeDatabase();
            try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
                System.out.println("[SUCCESS] Connected to MySQL database: " + conn.getCatalog());
                System.out.println("[SUCCESS] Tables (users, students, subjects, results) are verified.");
                System.out.println("[SUCCESS] Default administrator ('admin' / 'admin123') is ready.");
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Connection check failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void ensureDatabaseExists() {
        try (Connection conn = DatabaseConnection.getInstance().getRootServerConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS student_result_db");
        } catch (SQLException e) {
            System.err.println("[INFO] Note on database creation check: " + e.getMessage());
        }
    }

    private static boolean ensureTablesAndSeeds() {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. Users table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users (" +
                    "user_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "username VARCHAR(50) NOT NULL UNIQUE, " +
                    "password VARCHAR(255) NOT NULL, " +
                    "full_name VARCHAR(100) NOT NULL, " +
                    "role VARCHAR(20) DEFAULT 'ADMIN', " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // 2. Students table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS students (" +
                    "student_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "roll_number VARCHAR(30) NOT NULL UNIQUE, " +
                    "first_name VARCHAR(50) NOT NULL, " +
                    "last_name VARCHAR(50) NOT NULL, " +
                    "email VARCHAR(100), " +
                    "course VARCHAR(50) NOT NULL, " +
                    "semester INT NOT NULL, " +
                    "gender VARCHAR(10), " +
                    "dob DATE, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // 3. Subjects table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS subjects (" +
                    "subject_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "subject_code VARCHAR(20) NOT NULL UNIQUE, " +
                    "subject_name VARCHAR(100) NOT NULL, " +
                    "max_marks INT DEFAULT 100, " +
                    "pass_marks INT DEFAULT 35)");

            // 4. Results table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS results (" +
                    "result_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "student_id INT NOT NULL, " +
                    "subject_id INT NOT NULL, " +
                    "marks_obtained DECIMAL(5, 2) NOT NULL, " +
                    "grade VARCHAR(5), " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "CONSTRAINT fk_result_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE, " +
                    "CONSTRAINT fk_result_subject FOREIGN KEY (subject_id) REFERENCES subjects(subject_id) ON DELETE CASCADE, " +
                    "CONSTRAINT uq_student_subject UNIQUE (student_id, subject_id))");

            // 5. Seed default administrator safely if user table is empty or admin missing
            try (PreparedStatement checkAdmin = conn.prepareStatement("SELECT user_id FROM users WHERE LOWER(username) = 'admin'")) {
                try (ResultSet rs = checkAdmin.executeQuery()) {
                    if (!rs.next()) {
                        try (PreparedStatement insertAdmin = conn.prepareStatement(
                                "INSERT INTO users (username, password, full_name, role) VALUES (?, ?, ?, ?)")) {
                            insertAdmin.setString(1, "admin");
                            insertAdmin.setString(2, "admin123");
                            insertAdmin.setString(3, "System Administrator");
                            insertAdmin.setString(4, "ADMIN");
                            insertAdmin.executeUpdate();
                            System.out.println("[INFO] Default administrator account ('admin' / 'admin123') initialized successfully.");
                        }
                    }
                }
            }

            // 6. Seed default curriculum subjects if subjects table is empty
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM subjects")) {
                if (rs.next() && rs.getInt("total") == 0) {
                    try (PreparedStatement insertSub = conn.prepareStatement(
                            "INSERT INTO subjects (subject_code, subject_name, max_marks, pass_marks) VALUES (?, ?, ?, ?)")) {
                        insertSubject(insertSub, "CS101", "Java Programming", 100, 35);
                        insertSubject(insertSub, "CS102", "Database Management Systems", 100, 35);
                        insertSubject(insertSub, "CS103", "Data Structures & Algorithms", 100, 35);
                        insertSubject(insertSub, "CS104", "Computer Networks", 100, 35);
                        insertSubject(insertSub, "CS105", "Operating Systems", 100, 35);
                        System.out.println("[INFO] Standard curriculum subjects initialized successfully.");
                    }
                }
            }
            return true;
        } catch (SQLException e) {
            System.err.println("[WARN] Database initialization notice: " + e.getMessage());
            return false;
        }
    }

    private static void insertSubject(PreparedStatement stmt, String code, String name, int max, int pass) throws SQLException {
        stmt.setString(1, code);
        stmt.setString(2, name);
        stmt.setInt(3, max);
        stmt.setInt(4, pass);
        stmt.executeUpdate();
    }
}
