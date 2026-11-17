package result.service;

import result.dao.StudentDAO;
import result.exception.DuplicateRecordException;
import result.exception.RecordNotFoundException;
import result.exception.ValidationException;
import result.model.Student;

import java.util.List;
import java.util.Optional;

/**
 * Service class handling business logic and validations for student management.
 */
public class StudentService {

    private final StudentDAO studentDAO;

    public StudentService(StudentDAO studentDAO) {
        this.studentDAO = studentDAO;
    }

    public boolean registerStudent(Student student) throws ValidationException, DuplicateRecordException {
        validateStudent(student);

        // Check if student with this roll number already exists
        if (studentDAO.findByRollNumber(student.getRollNumber().trim()).isPresent()) {
            throw new DuplicateRecordException("A student with Roll Number '" + student.getRollNumber().trim() + "' already exists.");
        }

        return studentDAO.insert(student);
    }

    public boolean updateStudent(Student student) throws ValidationException, RecordNotFoundException {
        validateStudent(student);

        Optional<Student> existingOpt = Optional.empty();
        if (student.getStudentId() > 0) {
            existingOpt = studentDAO.findById(student.getStudentId());
        }
        if (!existingOpt.isPresent()) {
            existingOpt = studentDAO.findByRollNumber(student.getRollNumber().trim());
        }

        if (!existingOpt.isPresent()) {
            throw new RecordNotFoundException("Student with Roll Number '" + student.getRollNumber() + "' was not found.");
        }

        student.setStudentId(existingOpt.get().getStudentId());
        return studentDAO.update(student);
    }

    public boolean deleteStudent(int studentId) throws RecordNotFoundException {
        Optional<Student> existing = studentDAO.findById(studentId);
        if (!existing.isPresent()) {
            throw new RecordNotFoundException("Student with ID " + studentId + " was not found.");
        }
        return studentDAO.delete(studentId);
    }

    public Student getStudentByRollNumber(String rollNumber) throws RecordNotFoundException {
        if (rollNumber == null || rollNumber.trim().isEmpty()) {
            throw new RecordNotFoundException("Please provide a valid Roll Number.");
        }

        return studentDAO.findByRollNumber(rollNumber.trim())
                .orElseThrow(() -> new RecordNotFoundException("No student found with Roll Number: " + rollNumber.trim()));
    }

    public List<Student> getAllStudents() {
        return studentDAO.findAll();
    }

    public List<Student> searchStudents(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return studentDAO.findAll();
        }
        return studentDAO.searchByKeyword(keyword.trim());
    }

    private void validateStudent(Student student) throws ValidationException {
        if (student == null) {
            throw new ValidationException("Student data cannot be empty.");
        }
        if (student.getRollNumber() == null || student.getRollNumber().trim().isEmpty()) {
            throw new ValidationException("Roll Number is required.");
        }
        if (student.getFirstName() == null || student.getFirstName().trim().isEmpty()) {
            throw new ValidationException("First Name is required.");
        }
        if (student.getLastName() == null || student.getLastName().trim().isEmpty()) {
            throw new ValidationException("Last Name is required.");
        }
        if (student.getCourse() == null || student.getCourse().trim().isEmpty()) {
            throw new ValidationException("Course is required.");
        }
        if (student.getSemester() < 1 || student.getSemester() > 12) {
            throw new ValidationException("Semester must be between 1 and 12.");
        }
    }
}
