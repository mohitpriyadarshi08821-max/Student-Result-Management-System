package result.dao;

import result.model.Student;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface defining persistence operations for Student entities.
 */
public interface StudentDAO {

    boolean insert(Student student);

    boolean update(Student student);

    boolean delete(int studentId);

    Optional<Student> findById(int studentId);

    Optional<Student> findByRollNumber(String rollNumber);

    List<Student> findAll();

    List<Student> searchByKeyword(String keyword);
}

