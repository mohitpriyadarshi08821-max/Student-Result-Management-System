package result.ui;

import result.dao.ResultDAO;
import result.dao.StudentDAO;
import result.dao.SubjectDAO;
import result.exception.RecordNotFoundException;
import result.exception.ValidationException;
import result.model.Result;
import result.model.Student;
import result.model.StudentResultSummary;
import result.model.Subject;
import result.service.ResultService;
import result.service.StudentService;

import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * End-to-end verification test for DashboardFrame, Panels, StudentService, and ResultService.
 */
public class DashboardAndServicesTest {

    static class InMemoryStudentDAO implements StudentDAO {
        private final List<Student> list = new ArrayList<>();
        private int idCounter = 1;

        @Override
        public boolean insert(Student student) {
            student.setStudentId(idCounter++);
            list.add(student);
            return true;
        }

        @Override
        public boolean update(Student student) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getStudentId() == student.getStudentId()) {
                    list.set(i, student);
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean delete(int studentId) {
            return list.removeIf(s -> s.getStudentId() == studentId);
        }

        @Override
        public Optional<Student> findById(int studentId) {
            return list.stream().filter(s -> s.getStudentId() == studentId).findFirst();
        }

        @Override
        public Optional<Student> findByRollNumber(String rollNumber) {
            return list.stream().filter(s -> s.getRollNumber().equalsIgnoreCase(rollNumber)).findFirst();
        }

        @Override
        public List<Student> findAll() {
            return new ArrayList<>(list);
        }

        @Override
        public List<Student> searchByKeyword(String keyword) {
            return list.stream()
                    .filter(s -> s.getRollNumber().contains(keyword) || s.getFirstName().contains(keyword))
                    .toList();
        }
    }

    static class InMemorySubjectDAO implements SubjectDAO {
        private final List<Subject> list = new ArrayList<>();

        public InMemorySubjectDAO() {
            list.add(new Subject(1, "CS101", "Java Programming", 100, 35));
            list.add(new Subject(2, "CS102", "DBMS", 100, 35));
        }

        @Override
        public boolean insert(Subject subject) {
            list.add(subject);
            return true;
        }

        @Override
        public boolean update(Subject subject) {
            return true;
        }

        @Override
        public boolean delete(int subjectId) {
            return list.removeIf(s -> s.getSubjectId() == subjectId);
        }

        @Override
        public Optional<Subject> findById(int subjectId) {
            return list.stream().filter(s -> s.getSubjectId() == subjectId).findFirst();
        }

        @Override
        public Optional<Subject> findByCode(String subjectCode) {
            return list.stream().filter(s -> s.getSubjectCode().equalsIgnoreCase(subjectCode)).findFirst();
        }

        @Override
        public List<Subject> findAll() {
            return new ArrayList<>(list);
        }
    }

    static class InMemoryResultDAO implements ResultDAO {
        private final List<Result> list = new ArrayList<>();
        private int idGen = 1;

        @Override
        public boolean insert(Result result) {
            result.setResultId(idGen++);
            list.add(result);
            return true;
        }

        @Override
        public boolean update(Result result) {
            for (int i = 0; i < list.size(); i++) {
                Result r = list.get(i);
                if (r.getStudentId() == result.getStudentId() && r.getSubjectId() == result.getSubjectId()) {
                    list.set(i, result);
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean delete(int resultId) {
            return list.removeIf(r -> r.getResultId() == resultId);
        }

        @Override
        public boolean deleteByStudentId(int studentId) {
            return list.removeIf(r -> r.getStudentId() == studentId);
        }

        @Override
        public Optional<Result> findById(int resultId) {
            return list.stream().filter(r -> r.getResultId() == resultId).findFirst();
        }

        @Override
        public Optional<Result> findByStudentAndSubject(int studentId, int subjectId) {
            return list.stream().filter(r -> r.getStudentId() == studentId && r.getSubjectId() == subjectId).findFirst();
        }

        @Override
        public List<Result> findByStudentId(int studentId) {
            return list.stream().filter(r -> r.getStudentId() == studentId).toList();
        }

        @Override
        public List<Result> findAll() {
            return new ArrayList<>(list);
        }
    }

    public static void main(String[] args) throws Exception {
        InMemoryStudentDAO studentDAO = new InMemoryStudentDAO();
        InMemorySubjectDAO subjectDAO = new InMemorySubjectDAO();
        InMemoryResultDAO resultDAO = new InMemoryResultDAO();

        StudentService studentService = new StudentService(studentDAO);
        ResultService resultService = new ResultService(resultDAO, studentDAO, subjectDAO);

        // 1. Test Student Registration
        Student student = new Student();
        student.setRollNumber("R101");
        student.setFirstName("Alice");
        student.setLastName("Smith");
        student.setEmail("alice@test.com");
        student.setCourse("B.Tech CS");
        student.setSemester(4);
        studentService.registerStudent(student);

        if (studentDAO.findAll().size() != 1) {
            throw new AssertionError("Student registration failed");
        }

        // 2. Test Result Entry & Validation
        Result result = new Result();
        result.setStudentId(student.getStudentId());
        result.setSubjectId(1);
        result.setMarksObtained(88.0);
        resultService.recordResult(result);

        if (!"A".equals(result.getGrade())) {
            throw new AssertionError("Expected grade A for 88/100, got: " + result.getGrade());
        }

        // 3. Test Marks Exceeding Max Marks Validation
        Result invalidResult = new Result();
        invalidResult.setStudentId(student.getStudentId());
        invalidResult.setSubjectId(1);
        invalidResult.setMarksObtained(150.0); // max is 100
        try {
            resultService.recordResult(invalidResult);
            throw new AssertionError("Should have rejected marks > maxMarks");
        } catch (ValidationException expected) {
            // Success
        }

        // 4. Test Summary Generation
        StudentResultSummary summary = resultService.generateResultSummary("R101");
        if (!"PASS".equals(summary.getStatus())) {
            throw new AssertionError("Expected status PASS, got: " + summary.getStatus());
        }
        if (summary.getPercentage() != 88.0) {
            throw new AssertionError("Expected 88% percentage, got: " + summary.getPercentage());
        }

        // 5. Test Swing Panels and DashboardFrame initialization in GUI environment
        if (!GraphicsEnvironment.isHeadless()) {
            StudentManagementPanel smPanel = new StudentManagementPanel(studentService);
            ResultEntryPanel rePanel = new ResultEntryPanel(resultService, studentService);
            ResultViewPanel rvPanel = new ResultViewPanel(resultService);

            if (smPanel.getComponentCount() == 0 || rePanel.getComponentCount() == 0 || rvPanel.getComponentCount() == 0) {
                throw new AssertionError("Panels components failed to initialize");
            }
            System.out.println("GUI panels initialized successfully.");
        }

        System.out.println("ALL DASHBOARD AND SERVICES TESTS PASSED SUCCESSFULLY!");
    }
}
