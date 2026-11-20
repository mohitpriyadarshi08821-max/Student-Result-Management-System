package result.dao;

import result.database.DatabaseConnection;
import result.database.DataSyncBridge;
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
 * JDBC implementation of the SubjectDAO interface with in-memory fallback support.
 */
public class SubjectDAOImpl implements SubjectDAO {

    @Override
    public boolean insert(Subject subject) {
        if (subject == null) {
            return false;
        }

        String sql = "INSERT INTO subjects (subject_code, subject_name, max_marks, pass_marks, credits) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, subject.getSubjectCode());
            stmt.setString(2, subject.getSubjectName());
            stmt.setInt(3, subject.getMaxMarks());
            stmt.setInt(4, subject.getPassMarks());
            stmt.setInt(5, subject.getCredits());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        subject.setSubjectId(keys.getInt(1));
                    }
                }
            }
            InMemoryDataStore.getInstance().putSubjectDirect(subject);
            DataSyncBridge.exportMySQLToDisk(conn);
            return true;
        } catch (Exception e) {
            return InMemoryDataStore.getInstance().saveSubject(subject);
        }
    }

    @Override
    public boolean update(Subject subject) {
        if (subject == null) {
            return false;
        }

        String sql = "UPDATE subjects SET subject_code = ?, subject_name = ?, max_marks = ?, pass_marks = ?, credits = ? WHERE subject_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, subject.getSubjectCode());
            stmt.setString(2, subject.getSubjectName());
            stmt.setInt(3, subject.getMaxMarks());
            stmt.setInt(4, subject.getPassMarks());
            stmt.setInt(5, subject.getCredits());
            stmt.setInt(6, subject.getSubjectId());

            stmt.executeUpdate();
            InMemoryDataStore.getInstance().putSubjectDirect(subject);
            DataSyncBridge.exportMySQLToDisk(conn);
            return true;
        } catch (Exception e) {
            return InMemoryDataStore.getInstance().saveSubject(subject);
        }
    }

    @Override
    public boolean delete(int subjectId) {
        String sql = "DELETE FROM subjects WHERE subject_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, subjectId);
            stmt.executeUpdate();
            InMemoryDataStore.getInstance().deleteSubjectDirect(subjectId);
            DataSyncBridge.exportMySQLToDisk(conn);
            return true;
        } catch (Exception e) {
            return InMemoryDataStore.getInstance().deleteSubject(subjectId);
        }
    }

    @Override
    public Optional<Subject> findById(int subjectId) {
        String sql = "SELECT subject_id, subject_code, subject_name, max_marks, pass_marks, credits FROM subjects WHERE subject_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, subjectId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSubject(rs));
                }
            }
        } catch (Exception e) {
            return InMemoryDataStore.getInstance().findSubjectById(subjectId);
        }
        return InMemoryDataStore.getInstance().findSubjectById(subjectId);
    }

    @Override
    public Optional<Subject> findByCode(String subjectCode) {
        if (subjectCode == null || subjectCode.trim().isEmpty()) {
            return Optional.empty();
        }

        String sql = "SELECT subject_id, subject_code, subject_name, max_marks, pass_marks, credits FROM subjects WHERE LOWER(subject_code) = LOWER(?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, subjectCode.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSubject(rs));
                }
            }
        } catch (Exception e) {
            return InMemoryDataStore.getInstance().findSubjectByCode(subjectCode);
        }
        return InMemoryDataStore.getInstance().findSubjectByCode(subjectCode);
    }

    @Override
    public List<Subject> findAll() {
        List<Subject> subjects = new ArrayList<>();
        String sql = "SELECT subject_id, subject_code, subject_name, max_marks, pass_marks, credits FROM subjects ORDER BY subject_id ASC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                subjects.add(mapResultSetToSubject(rs));
            }
            return subjects;
        } catch (Exception e) {
            return InMemoryDataStore.getInstance().getAllSubjects();
        }
    }

    private Subject mapResultSetToSubject(ResultSet rs) throws SQLException {
        int credits = 4;
        try {
            credits = rs.getInt("credits");
            if (credits <= 0) credits = 4;
        } catch (SQLException ignored) {
            credits = 4;
        }
        return new Subject(
                rs.getInt("subject_id"),
                rs.getString("subject_code"),
                rs.getString("subject_name"),
                rs.getInt("max_marks"),
                rs.getInt("pass_marks"),
                credits
        );
    }
}
