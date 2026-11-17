package result.ui;

import result.dao.ResultDAO;
import result.dao.StudentDAO;
import result.dao.SubjectDAO;
import result.model.Result;
import result.model.Student;
import result.model.Subject;
import result.service.ResultService;
import result.service.StudentService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ResultEntryPanelTest {

    public static void main(String[] args) {
        System.out.println("Starting ResultEntryPanel Metrics Verification Test...");

        // Setup mock DAOs and services
        StudentDAO studentDAO = new StudentDAO() {
            private final List<Student> students = new ArrayList<>();
            {
                students.add(new Student(1, "2510001320", "Mohit", "Priyadarshi", "mohit@example.com", "B.Tech Computer Science", 4, "Male", LocalDate.now().minusYears(19)));
                students.add(new Student(3, "2510001319", "Adarsh", "Kumar", "adarsh@example.com", "B.Tech IT", 4, "Male", LocalDate.now().minusYears(20)));
            }

            @Override public boolean insert(Student student) { return true; }
            @Override public boolean update(Student student) { return true; }
            @Override public boolean delete(int studentId) { return true; }
            @Override public Optional<Student> findById(int studentId) {
                return students.stream().filter(s -> s.getStudentId() == studentId).findFirst();
            }
            @Override public Optional<Student> findByRollNumber(String rollNumber) {
                return students.stream().filter(s -> s.getRollNumber().equals(rollNumber)).findFirst();
            }
            @Override public List<Student> findAll() { return new ArrayList<>(students); }
            @Override public List<Student> searchByKeyword(String keyword) { return new ArrayList<>(students); }
        };

        SubjectDAO subjectDAO = new SubjectDAO() {
            private final List<Subject> subjects = new ArrayList<>();
            {
                subjects.add(new Subject(1, "CS102", "Database Management Systems", 100, 35));
                subjects.add(new Subject(2, "CS103", "Data Structures & Algorithms", 100, 35));
                subjects.add(new Subject(3, "CS104", "Computer Networks", 100, 35));
                subjects.add(new Subject(4, "CS105", "Operating Systems", 100, 35));
                subjects.add(new Subject(5, "CS107", "Computer Architecture", 100, 35));
            }
            @Override public boolean insert(Subject subject) { return true; }
            @Override public boolean update(Subject subject) { return true; }
            @Override public boolean delete(int subjectId) { return true; }
            @Override public Optional<Subject> findById(int subjectId) {
                return subjects.stream().filter(s -> s.getSubjectId() == subjectId).findFirst();
            }
            @Override public Optional<Subject> findByCode(String subjectCode) {
                return subjects.stream().filter(s -> s.getSubjectCode().equalsIgnoreCase(subjectCode)).findFirst();
            }
            @Override public List<Subject> findAll() { return new ArrayList<>(subjects); }
        };

        List<Result> resultStore = new ArrayList<>();
        // Seed results for Adarsh Kumar (student ID 3): ALL 5 PASSING
        Result r1 = new Result(8, 3, 1, 87.0, "A");
        r1.setSubject(new Subject(1, "CS102", "Database Management Systems", 100, 35));
        Result r2 = new Result(9, 3, 2, 69.0, "C");
        r2.setSubject(new Subject(2, "CS103", "Data Structures & Algorithms", 100, 35));
        Result r3 = new Result(10, 3, 3, 80.0, "A");
        r3.setSubject(new Subject(3, "CS104", "Computer Networks", 100, 35));
        Result r4 = new Result(11, 3, 4, 66.0, "C");
        r4.setSubject(new Subject(4, "CS105", "Operating Systems", 100, 35));
        Result r5 = new Result(7, 3, 5, 88.0, "A");
        r5.setSubject(new Subject(5, "CS107", "Computer Architecture", 100, 35));
        resultStore.add(r1);
        resultStore.add(r2);
        resultStore.add(r3);
        resultStore.add(r4);
        resultStore.add(r5);

        ResultDAO resultDAO = new ResultDAO() {
            @Override public boolean insert(Result result) {
                resultStore.add(result);
                return true;
            }
            @Override public boolean update(Result result) { return true; }
            @Override public boolean delete(int resultId) {
                return resultStore.removeIf(r -> r.getResultId() == resultId);
            }
            @Override public boolean deleteByStudentId(int studentId) {
                return resultStore.removeIf(r -> r.getStudentId() == studentId);
            }
            @Override public Optional<Result> findById(int resultId) {
                return resultStore.stream().filter(r -> r.getResultId() == resultId).findFirst();
            }
            @Override public Optional<Result> findByStudentAndSubject(int studentId, int subjectId) {
                return resultStore.stream().filter(r -> r.getStudentId() == studentId && r.getSubjectId() == subjectId).findFirst();
            }
            @Override public List<Result> findByStudentId(int studentId) {
                List<Result> list = new ArrayList<>();
                for (Result r : resultStore) {
                    if (r.getStudentId() == studentId) list.add(r);
                }
                return list;
            }
            @Override public List<Result> findAll() { return new ArrayList<>(resultStore); }
        };

        StudentService studentService = new StudentService(studentDAO);
        ResultService resultService = new ResultService(resultDAO, studentDAO, subjectDAO);

        ResultEntryPanel panel = new ResultEntryPanel(resultService, studentService);

        // Verify Adarsh Kumar
        panel.setStudentRoll("2510001319");

        System.out.println("Adarsh Kumar Results Loaded:");
        System.out.println("  Total Results: " + panel.getTotalResultsCount());
        System.out.println("  Passed: " + panel.getPassedCount());
        System.out.println("  Failed: " + panel.getFailedCount());
        System.out.println("  Average: " + panel.getAverageScore());

        // Assertions for Adarsh Kumar (all passing)
        if (panel.getFailedCount() != 0) {
            throw new AssertionError("FAILED: Failed count should be 0 when student passes all subjects, but got: " + panel.getFailedCount());
        }
        if (panel.getPassedCount() != 5) {
            throw new AssertionError("FAILED: Passed count should be 5, but got: " + panel.getPassedCount());
        }
        if (panel.getTotalResultsCount() != 5) {
            throw new AssertionError("FAILED: Total count should be 5, but got: " + panel.getTotalResultsCount());
        }
        if (!"78%".equals(panel.getAverageScore())) {
            throw new AssertionError("FAILED: Average score should be 78%, but got: " + panel.getAverageScore());
        }

        // Test clearForm
        panel.clearForm();
        if (panel.getFailedCount() != 0 || panel.getPassedCount() != 0 || panel.getTotalResultsCount() != 0) {
            throw new AssertionError("FAILED: Metrics should reset to 0 after clearForm()");
        }

        System.out.println("RESULT ENTRY PANEL METRICS TEST PASSED SUCCESSFULLY!");
    }
}
