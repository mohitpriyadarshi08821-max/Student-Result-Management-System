package result.ui;

import result.dao.ResultDAO;
import result.dao.StudentDAO;
import result.dao.SubjectDAO;
import result.dao.UserDAO;
import result.model.Result;
import result.model.Student;
import result.model.StudentResultSummary;
import result.model.Subject;
import result.model.User;
import result.service.AuthService;
import result.service.ResultService;
import result.service.StudentService;
import result.service.SubjectService;

import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Pure Java verification suite for DashboardFrame, StudentManagementPanel,
 * SubjectManagementPanel, ResultEntryPanel, and ResultViewPanel.
 */
public class DashboardTest {

    static class MockStudentDAO implements StudentDAO {
        private final List<Student> students = new ArrayList<>();

        public MockStudentDAO() {
            students.add(new Student(1, "CS2026-001", "Alice", "Smith", "alice@example.com", "B.Tech Computer Science", 4, "Female", null));
            students.add(new Student(2, "CS2026-002", "Bob", "Jones", "bob@example.com", "BCA", 2, "Male", null));
        }

        @Override
        public boolean insert(Student student) {
            student.setStudentId(students.size() + 1);
            students.add(student);
            return true;
        }

        @Override
        public boolean update(Student student) {
            for (int i = 0; i < students.size(); i++) {
                if (students.get(i).getStudentId() == student.getStudentId()) {
                    students.set(i, student);
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean delete(int studentId) {
            return students.removeIf(s -> s.getStudentId() == studentId);
        }

        @Override
        public Optional<Student> findById(int studentId) {
            return students.stream().filter(s -> s.getStudentId() == studentId).findFirst();
        }

        @Override
        public Optional<Student> findByRollNumber(String rollNumber) {
            return students.stream().filter(s -> s.getRollNumber().equalsIgnoreCase(rollNumber)).findFirst();
        }

        @Override
        public List<Student> findAll() {
            return new ArrayList<>(students);
        }

        @Override
        public List<Student> searchByKeyword(String keyword) {
            List<Student> res = new ArrayList<>();
            for (Student s : students) {
                if (s.getRollNumber().contains(keyword) || s.getFirstName().contains(keyword)) {
                    res.add(s);
                }
            }
            return res;
        }
    }

    static class MockSubjectDAO implements SubjectDAO {
        private final List<Subject> subjects = new ArrayList<>();

        public MockSubjectDAO() {
            subjects.add(new Subject(1, "CS101", "Java Programming", 100, 35));
            subjects.add(new Subject(2, "CS102", "DBMS", 100, 35));
        }

        @Override
        public boolean insert(Subject subject) {
            subject.setSubjectId(subjects.size() + 1);
            subjects.add(subject);
            return true;
        }

        @Override
        public boolean update(Subject subject) {
            for (int i = 0; i < subjects.size(); i++) {
                if (subjects.get(i).getSubjectId() == subject.getSubjectId()) {
                    subjects.set(i, subject);
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean delete(int subjectId) {
            return subjects.removeIf(s -> s.getSubjectId() == subjectId);
        }

        @Override
        public Optional<Subject> findById(int subjectId) {
            return subjects.stream().filter(s -> s.getSubjectId() == subjectId).findFirst();
        }

        @Override
        public Optional<Subject> findByCode(String subjectCode) {
            return subjects.stream().filter(s -> s.getSubjectCode().equalsIgnoreCase(subjectCode)).findFirst();
        }

        @Override
        public List<Subject> findAll() {
            return new ArrayList<>(subjects);
        }
    }

    static class MockResultDAO implements ResultDAO {
        private final List<Result> results = new ArrayList<>();

        public MockResultDAO() {
            Result r1 = new Result(1, 1, 1, 88.0, "A");
            r1.setSubject(new Subject(1, "CS101", "Java Programming", 100, 35));
            results.add(r1);
        }

        @Override
        public boolean insert(Result result) {
            results.add(result);
            return true;
        }

        @Override
        public boolean update(Result result) {
            return true;
        }

        @Override
        public boolean delete(int resultId) {
            return results.removeIf(r -> r.getResultId() == resultId);
        }

        @Override
        public boolean deleteByStudentId(int studentId) {
            return results.removeIf(r -> r.getStudentId() == studentId);
        }

        @Override
        public Optional<Result> findById(int resultId) {
            return results.stream().filter(r -> r.getResultId() == resultId).findFirst();
        }

        @Override
        public Optional<Result> findByStudentAndSubject(int studentId, int subjectId) {
            return results.stream().filter(r -> r.getStudentId() == studentId && r.getSubjectId() == subjectId).findFirst();
        }

        @Override
        public List<Result> findByStudentId(int studentId) {
            List<Result> res = new ArrayList<>();
            for (Result r : results) {
                if (r.getStudentId() == studentId) {
                    res.add(r);
                }
            }
            return res;
        }

        @Override
        public List<Result> findAll() {
            return new ArrayList<>(results);
        }
    }

    static class MockUserDAO implements UserDAO {
        @Override
        public Optional<User> findByUsername(String username) {
            return Optional.of(new User(1, "admin", "admin123", "Administrator", "ADMIN"));
        }

        @Override
        public boolean insert(User user) {
            return true;
        }

        @Override
        public boolean verifyCredentials(String username, String password) {
            return true;
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Running Dashboard and Panels Verification Tests...");

        MockStudentDAO studentDAO = new MockStudentDAO();
        MockSubjectDAO subjectDAO = new MockSubjectDAO();
        MockResultDAO resultDAO = new MockResultDAO();
        MockUserDAO userDAO = new MockUserDAO();

        StudentService studentService = new StudentService(studentDAO);
        SubjectService subjectService = new SubjectService(subjectDAO);
        ResultService resultService = new ResultService(resultDAO, studentDAO, subjectDAO);
        AuthService authService = new AuthService(userDAO);
        authService.login("admin", "admin123");

        // Test StudentService logic
        testStudentService(studentService);

        // Test SubjectService logic
        testSubjectService(subjectService);

        // Test ResultService logic
        testResultService(resultService);

        // Test UI panels if display environment available
        if (!GraphicsEnvironment.isHeadless()) {
            testDashboardUI(authService, studentService, subjectService, resultService);
            System.out.println("Dashboard UI components and 4 panels verified successfully!");
        } else {
            System.out.println("Headless environment detected - skipping GUI frame rendering.");
        }

        System.out.println("ALL DASHBOARD AND PANEL TESTS PASSED SUCCESSFULLY!");
    }

    private static void testStudentService(StudentService service) throws Exception {
        List<Student> all = service.getAllStudents();
        if (all.size() != 2) {
            throw new AssertionError("Expected 2 initial students, found: " + all.size());
        }

        Student s = new Student(0, "CS2026-003", "Charlie", "Brown", "charlie@example.com", "IT", 3, "Male", null);
        boolean saved = service.registerStudent(s);
        if (!saved || service.getAllStudents().size() != 3) {
            throw new AssertionError("Failed to register new student.");
        }

        Student retrieved = service.getStudentByRollNumber("CS2026-003");
        if (!"Charlie".equals(retrieved.getFirstName())) {
            throw new AssertionError("Retrieved student mismatch.");
        }

        service.deleteStudent(retrieved.getStudentId());
        if (service.getAllStudents().size() != 2) {
            throw new AssertionError("Failed to delete student.");
        }
    }

    private static void testSubjectService(SubjectService service) throws Exception {
        List<Subject> all = service.getAllSubjects();
        if (all.size() != 2) {
            throw new AssertionError("Expected 2 initial subjects, found: " + all.size());
        }

        Subject s = new Subject(0, "CS103", "Networks", 100, 35);
        boolean saved = service.registerSubject(s);
        if (!saved || service.getAllSubjects().size() != 3) {
            throw new AssertionError("Failed to register new subject.");
        }

        Subject retrieved = service.getSubjectByCode("CS103");
        if (!"Networks".equals(retrieved.getSubjectName())) {
            throw new AssertionError("Retrieved subject mismatch.");
        }

        service.deleteSubject(retrieved.getSubjectId());
        if (service.getAllSubjects().size() != 2) {
            throw new AssertionError("Failed to delete subject.");
        }
    }

    private static void testResultService(ResultService service) throws Exception {
        StudentResultSummary summary = service.generateResultSummary("CS2026-001");
        if (summary == null || summary.getResults().isEmpty()) {
            throw new AssertionError("Failed to generate summary for CS2026-001");
        }
        if (!"PASS".equals(summary.getStatus())) {
            throw new AssertionError("Expected PASS status for CS2026-001");
        }
        if (!"A".equals(summary.getOverallGrade())) {
            throw new AssertionError("Expected Grade A, got: " + summary.getOverallGrade());
        }
    }

    private static void testDashboardUI(AuthService authService, StudentService studentService, SubjectService subjectService, ResultService resultService) {
        DashboardFrame frame = new DashboardFrame(authService, studentService, subjectService, resultService);

        if (frame.getTabbedPane() == null || frame.getTabbedPane().getTabCount() != 4) {
            throw new AssertionError("Dashboard tabbedPane must have 4 tabs, found: " +
                    (frame.getTabbedPane() != null ? frame.getTabbedPane().getTabCount() : 0));
        }

        if (frame.getStudentManagementPanel() == null) {
            throw new AssertionError("StudentManagementPanel not initialized in DashboardFrame");
        }

        if (frame.getSubjectManagementPanel() == null) {
            throw new AssertionError("SubjectManagementPanel not initialized in DashboardFrame");
        }

        if (frame.getResultEntryPanel() == null) {
            throw new AssertionError("ResultEntryPanel not initialized in DashboardFrame");
        }

        if (frame.getResultViewPanel() == null) {
            throw new AssertionError("ResultViewPanel not initialized in DashboardFrame");
        }

        // Test navigation helpers
        frame.navigateToResultEntry("CS2026-001");
        if (frame.getTabbedPane().getSelectedIndex() != 2) {
            throw new AssertionError("Failed to navigate to Result Entry tab");
        }

        frame.navigateToResultView("CS2026-001");
        if (frame.getTabbedPane().getSelectedIndex() != 3) {
            throw new AssertionError("Failed to navigate to Result View tab");
        }

        frame.navigateToSubjectManagement();
        if (frame.getTabbedPane().getSelectedIndex() != 1) {
            throw new AssertionError("Failed to navigate to Subject Management tab");
        }

        frame.dispose();
    }
}
