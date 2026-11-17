package result.dao;

import result.database.DatabaseConnection;
import result.exception.DatabaseException;
import result.model.Subject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of the SubjectDAO interface.
 */
public class SubjectDAOImpl implements SubjectDAO {

    @Override
    public boolean insert(Subject subject) {
        if (subject == null) {
            return false;
        }

        String sql = "INSERT INTO subjects (subject_code, subject_name, max_marks, pass_marks) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, subject.getSubjectCode());
            stmt.setString(2, subject.getSubjectName());
            stmt.setInt(3, subject.getMaxMarks());
            stmt.setInt(4, subject.getPassMarks());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        subject.setSubjectId(keys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to insert subject: " + subject.getSubjectCode(), e);
        }
    }

    @Override
    public boolean update(Subject subject) {
        if (subject == null) {
            return false;
        }

        String sql = "UPDATE subjects SET subject_code = ?, subject_name = ?, max_marks = ?, pass_marks = ? WHERE subject_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, subject.getSubjectCode());
            stmt.setString(2, subject.getSubjectName());
            stmt.setInt(3, subject.getMaxMarks());
            stmt.setInt(4, subject.getPassMarks());
            stmt.setInt(5, subject.getSubjectId());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                return true;
            }
            return findById(subject.getSubjectId()).isPresent();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update subject: " + subject.getSubjectCode(), e);
        }
    }

    @Override
    public boolean delete(int subjectId) {
        String sql = "DELETE FROM subjects WHERE subject_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, subjectId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete subject with ID: " + subjectId, e);
        }
    }

    @Override
    public Optional<Subject> findById(int subjectId) {
        String sql = "SELECT subject_id, subject_code, subject_name, max_marks, pass_marks FROM subjects WHERE subject_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, subjectId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSubject(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query subject by ID: " + subjectId, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Subject> findByCode(String subjectCode) {
        if (subjectCode == null || subjectCode.trim().isEmpty()) {
            return Optional.empty();
        }

        String sql = "SELECT subject_id, subject_code, subject_name, max_marks, pass_marks FROM subjects WHERE LOWER(subject_code) = LOWER(?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, subjectCode.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSubject(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to query subject by code: " + subjectCode, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Subject> findAll() {
        List<Subject> subjects = new ArrayList<>();
        String sql = "SELECT subject_id, subject_code, subject_name, max_marks, pass_marks FROM subjects ORDER BY subject_id ASC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                subjects.add(mapResultSetToSubject(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch all subjects.", e);
        }
        return subjects;
    }

    private Subject mapResultSetToSubject(ResultSet rs) throws SQLException {
        return new Subject(
                rs.getInt("subject_id"),
                rs.getString("subject_code"),
                rs.getString("subject_name"),
                rs.getInt("max_marks"),
                rs.getInt("pass_marks")
        );
    }
}
