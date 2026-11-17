package result.dao;

import result.model.Result;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface defining operations for Student Marks & Results.
 */
public interface ResultDAO {

    boolean insert(Result result);

    boolean update(Result result);

    boolean delete(int resultId);

    boolean deleteByStudentId(int studentId);

    Optional<Result> findById(int resultId);

    Optional<Result> findByStudentAndSubject(int studentId, int subjectId);

    List<Result> findByStudentId(int studentId);

    List<Result> findAll();
}

