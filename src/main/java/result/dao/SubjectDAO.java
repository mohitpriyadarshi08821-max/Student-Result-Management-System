package result.dao;

import result.model.Subject;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface defining persistence operations for Subject entities.
 */
public interface SubjectDAO {

    boolean insert(Subject subject);

    boolean update(Subject subject);

    boolean delete(int subjectId);

    Optional<Subject> findById(int subjectId);

    Optional<Subject> findByCode(String subjectCode);

    List<Subject> findAll();
}

