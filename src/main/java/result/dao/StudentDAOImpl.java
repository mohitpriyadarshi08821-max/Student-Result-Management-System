package result.dao;

import result.database.DataSyncBridge;
import result.database.DatabaseConnection;
import result.exception.DatabaseException;
import result.model.Student;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of the StudentDAO interface with in-memory fallback support.
 */
public class StudentDAOImpl implements StudentDAO {

    @Override
    public boolean insert(Student student) {
        if (student == null) {
            return false;
        }

        String sql = "INSERT INTO students (roll_number, first_name, last_name, email, course, semester, gender, dob) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, student.getRollNumber());
            stmt.setString(2, student.getFirstName());
            stmt.setString(3, student.getLastName());
            stmt.setString(4, student.getEmail());
            stmt.setString(5, student.getCourse());
            stmt.setInt(6, student.getSemester());
            stmt.setString(7, student.getGender());
            if (student.getDateOfBirth() != null) {
                stmt.setDate(8, Date.valueOf(student.getDateOfBirth()));
            } else {
                stmt.setDate(8, null);
            }

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        student.setStudentId(generatedKeys.getInt(1));
                    }
                }
            }
            InMemoryDataStore.getInstance().putStudentDirect(student);
            DataSyncBridge.exportMySQLToDisk(conn);
            return true;
        } catch (Exception e) {
            return InMemoryDataStore.getInstance().saveStudent(student);
        }
    }

    @Override
    public boolean update(Student student) {
        if (student == null) {
            return false;
        }

        String sql = "UPDATE students SET roll_number = ?, first_name = ?, last_name = ?, email = ?, " +
                "course = ?, semester = ?, gender = ?, dob = ? WHERE student_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, student.getRollNumber());
            stmt.setString(2, student.getFirstName());
            stmt.setString(3, student.getLastName());
            stmt.setString(4, student.getEmail());
            stmt.setString(5, student.getCourse());
            stmt.setInt(6, student.getSemester());
            stmt.setString(7, student.getGender());
            if (student.getDateOfBirth() != null) {
                stmt.setDate(8, Date.valueOf(student.getDateOfBirth()));
            } else {
                stmt.setDate(8, null);
            }
            stmt.setInt(9, student.getStudentId());

            stmt.executeUpdate();
            InMemoryDataStore.getInstance().putStudentDirect(student);
            DataSyncBridge.exportMySQLToDisk(conn);
            return true;
        } catch (Exception e) {
            return InMemoryDataStore.getInstance().saveStudent(student);
        }
    }

    @Override
    public boolean delete(int studentId) {
        String sql = "DELETE FROM students WHERE student_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            stmt.executeUpdate();
            InMemoryDataStore.getInstance().deleteStudentDirect(studentId);
            DataSyncBridge.exportMySQLToDisk(conn);
            return true;
        } catch (Exception e) {
            return InMemoryDataStore.getInstance().deleteStudent(studentId);
        }
    }

    @Override
    public Optional<Student> findById(int studentId) {
        String sql = "SELECT student_id, roll_number, first_name, last_name, email, course, semester, gender, dob " +
                "FROM students WHERE student_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToStudent(rs));
                }
            }
        } catch (Exception e) {
            return InMemoryDataStore.getInstance().findStudentById(studentId);
        }
        return InMemoryDataStore.getInstance().findStudentById(studentId);
    }

    @Override
    public Optional<Student> findByRollNumber(String rollNumber) {
        if (rollNumber == null || rollNumber.trim().isEmpty()) {
            return Optional.empty();
        }

        String sql = "SELECT student_id, roll_number, first_name, last_name, email, course, semester, gender, dob " +
                "FROM students WHERE LOWER(roll_number) = LOWER(?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, rollNumber.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToStudent(rs));
                }
            }
        } catch (Exception e) {
            return InMemoryDataStore.getInstance().findStudentByRoll(rollNumber);
        }
        return InMemoryDataStore.getInstance().findStudentByRoll(rollNumber);
    }

    @Override
    public List<Student> findAll() {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT student_id, roll_number, first_name, last_name, email, course, semester, gender, dob " +
                "FROM students ORDER BY student_id ASC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                students.add(mapResultSetToStudent(rs));
            }
            return students;
        } catch (Exception e) {
            return InMemoryDataStore.getInstance().getAllStudents();
        }
    }

    @Override
    public List<Student> searchByKeyword(String keyword) {
        List<Student> students = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }

        String pattern = "%" + keyword.trim() + "%";
        String sql = "SELECT student_id, roll_number, first_name, last_name, email, course, semester, gender, dob " +
                "FROM students WHERE roll_number LIKE ? OR first_name LIKE ? OR last_name LIKE ? " +
                "OR email LIKE ? OR course LIKE ? ORDER BY student_id DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 1; i <= 5; i++) {
                stmt.setString(i, pattern);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    students.add(mapResultSetToStudent(rs));
                }
            }
            if (!students.isEmpty()) {
                return students;
            }
        } catch (Exception e) {
            List<Student> filtered = new ArrayList<>();
            String q = keyword.trim().toLowerCase();
            for (Student s : InMemoryDataStore.getInstance().getAllStudents()) {
                if (s.getRollNumber().toLowerCase().contains(q) || s.getFullName().toLowerCase().contains(q)) {
                    filtered.add(s);
                }
            }
            return filtered;
        }
        return students;
    }

    private Student mapResultSetToStudent(ResultSet rs) throws SQLException {
        Date dobSql = rs.getDate("dob");
        LocalDate dob = (dobSql != null) ? dobSql.toLocalDate() : null;

        return new Student(
                rs.getInt("student_id"),
                rs.getString("roll_number"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("email"),
                rs.getString("course"),
                rs.getInt("semester"),
                rs.getString("gender"),
                dob
        );
    }
}
