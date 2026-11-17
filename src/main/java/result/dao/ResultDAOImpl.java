package result.dao;

import result.database.DatabaseConnection;
import result.exception.DatabaseException;
import result.model.Result;
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
 * JDBC implementation of the ResultDAO interface.
 */
public class ResultDAOImpl implements ResultDAO {

    @Override
    public boolean insert(Result result) {
        if (result == null) {
            return false;
        }

        String sql = "INSERT INTO results (student_id, subject_id, marks_obtained, grade) VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE marks_obtained = VALUES(marks_obtained), grade = VALUES(grade)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, result.getStudentId());
            stmt.setInt(2, result.getSubjectId());
            stmt.setDouble(3, result.getMarksObtained());
            stmt.setString(4, result.getGrade());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        result.setResultId(keys.getInt(1));
                    }
                }
                return true;
            }
            // In MySQL, ON DUPLICATE KEY UPDATE with unchanged values returns 0 (success)
            return affected >= 0;
        } catch (Exception e) {
            throw new DatabaseException("Failed to insert student result.", e);
        }
    }

    @Override
    public boolean update(Result result) {
        if (result == null) {
            return false;
        }

        String sql = "UPDATE results SET marks_obtained = ?, grade = ? WHERE (student_id = ? AND subject_id = ?) OR result_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, result.getMarksObtained());
            stmt.setString(2, result.getGrade());
            stmt.setInt(3, result.getStudentId());
            stmt.setInt(4, result.getSubjectId());
            stmt.setInt(5, result.getResultId());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                return true;
            }
            return findByStudentAndSubject(result.getStudentId(), result.getSubjectId()).isPresent();
        } catch (Exception e) {
            throw new DatabaseException("Failed to update result for student ID: " + result.getStudentId(), e);
        }
    }

    @Override
    public boolean delete(int resultId) {
        String sql = "DELETE FROM results WHERE result_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, resultId);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            throw new DatabaseException("Failed to delete result with ID: " + resultId, e);
        }
    }

    @Override
    public boolean deleteByStudentId(int studentId) {
        String sql = "DELETE FROM results WHERE student_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            throw new DatabaseException("Failed to delete results for student ID: " + studentId, e);
        }
    }

    @Override
    public Optional<Result> findById(int resultId) {
        String sql = "SELECT r.result_id, r.student_id, r.subject_id, r.marks_obtained, r.grade, " +
                "s.subject_code, s.subject_name, s.max_marks, s.pass_marks " +
                "FROM results r JOIN subjects s ON r.subject_id = s.subject_id WHERE r.result_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, resultId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToResultWithSubject(rs));
                }
            }
        } catch (Exception e) {
            throw new DatabaseException("Failed to query result by ID: " + resultId, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Result> findByStudentAndSubject(int studentId, int subjectId) {
        String sql = "SELECT r.result_id, r.student_id, r.subject_id, r.marks_obtained, r.grade, " +
                "s.subject_code, s.subject_name, s.max_marks, s.pass_marks " +
                "FROM results r JOIN subjects s ON r.subject_id = s.subject_id " +
                "WHERE r.student_id = ? AND r.subject_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            stmt.setInt(2, subjectId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToResultWithSubject(rs));
                }
            }
        } catch (Exception e) {
            throw new DatabaseException("Failed to query result by student and subject.", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Result> findByStudentId(int studentId) {
        List<Result> results = new ArrayList<>();
        String sql = "SELECT r.result_id, r.student_id, r.subject_id, r.marks_obtained, r.grade, " +
                "s.subject_code, s.subject_name, s.max_marks, s.pass_marks " +
                "FROM results r JOIN subjects s ON r.subject_id = s.subject_id " +
                "WHERE r.student_id = ? ORDER BY s.subject_code ASC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapResultSetToResultWithSubject(rs));
                }
            }
        } catch (Exception e) {
            throw new DatabaseException("Failed to query results for student ID: " + studentId, e);
        }
        return results;
    }

    @Override
    public List<Result> findAll() {
        List<Result> results = new ArrayList<>();
        String sql = "SELECT r.result_id, r.student_id, r.subject_id, r.marks_obtained, r.grade, " +
                "s.subject_code, s.subject_name, s.max_marks, s.pass_marks " +
                "FROM results r JOIN subjects s ON r.subject_id = s.subject_id " +
                "ORDER BY r.result_id DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                results.add(mapResultSetToResultWithSubject(rs));
            }
        } catch (Exception e) {
            throw new DatabaseException("Failed to fetch all results.", e);
        }
        return results;
    }

    private Result mapResultSetToResultWithSubject(ResultSet rs) throws SQLException {
        Result result = new Result(
                rs.getInt("result_id"),
                rs.getInt("student_id"),
                rs.getInt("subject_id"),
                rs.getDouble("marks_obtained"),
                rs.getString("grade")
        );

        Subject subject = new Subject(
                rs.getInt("subject_id"),
                rs.getString("subject_code"),
                rs.getString("subject_name"),
                rs.getInt("max_marks"),
                rs.getInt("pass_marks")
        );
        result.setSubject(subject);
        return result;
    }
}
