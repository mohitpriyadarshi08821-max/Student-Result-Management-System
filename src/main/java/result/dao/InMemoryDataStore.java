package result.dao;

import result.database.DataSyncBridge;
import result.model.Result;
import result.model.Student;
import result.model.StudentResultSummary;
import result.model.Subject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe centralized in-memory data store for the Student Result Management System.
 * Serves as the single source of truth across all application panels (Student Management,
 * Subject Management, Result Entry, and View Results & Marksheets), ensuring immediate
 * and seamless synchronization in browser WebAssembly mode and offline environments.
 */
public class InMemoryDataStore {

    private static final InMemoryDataStore INSTANCE = new InMemoryDataStore();

    private final Map<Integer, Student> students = new ConcurrentHashMap<>();
    private final Map<Integer, Subject> subjects = new ConcurrentHashMap<>();
    private final Map<Integer, Result> results = new ConcurrentHashMap<>();
    private final AtomicInteger nextResultId = new AtomicInteger(100);

    public static InMemoryDataStore getInstance() {
        return INSTANCE;
    }

    private InMemoryDataStore() {
        seedInitialData();
    }

    private void seedInitialData() {
        // 1. Reference Students (IDs 1 - 7)
        putStudent(new Student(1, "2510001283", "Utkarsh", "Mishra", "utkarsh.mishra@example.com", "B.Tech Computer Science", 4, "Male", LocalDate.now().minusYears(19)));
        putStudent(new Student(2, "2510001205", "Abhishek", "Kumar", "abhishek.kumar@example.com", "B.Tech Information Technology", 4, "Male", LocalDate.now().minusYears(19)));
        putStudent(new Student(3, "2510001348", "Aditya", "Saini", "aditya.saini@example.com", "BCA", 2, "Male", LocalDate.now().minusYears(18)));
        putStudent(new Student(4, "2510001320", "Mohit", "Priyadarshi", "mohit.priyadarshi@example.com", "B.Tech Computer Science", 3, "Male", LocalDate.now().minusYears(19)));
        putStudent(new Student(5, "2510001331", "Moulik", "Tiwari", "moulik.tiwari@example.com", "BCA", 2, "Male", LocalDate.now().minusYears(19)));
        putStudent(new Student(6, "2510001319", "Adarsh", "Kumar", "adarsh.kumar@example.com", "B.Tech IT", 4, "Male", LocalDate.now().minusYears(20)));
        putStudent(new Student(7, "2510001212", "Akshay", "Bharadwaj", "akshay.bharadwaj@example.com", "MCA", 2, "Male", LocalDate.now().minusYears(19)));

        // 2. Curriculum Subjects (IDs 1 - 6)
        putSubject(new Subject(1, "CS107", "Computer Architecture And Organization", 100, 35, 4));
        putSubject(new Subject(2, "CS102", "Database Management Systems", 100, 35, 4));
        putSubject(new Subject(3, "CS103", "Data Structures & Algorithms", 100, 35, 4));
        putSubject(new Subject(4, "CS104", "Computer Networks", 100, 35, 4));
        putSubject(new Subject(5, "CS105", "Operating Systems", 100, 35, 4));
        putSubject(new Subject(6, "CS106", "Artificial Intelligence", 100, 35, 4));

        // 3. Initial Examination Results matching live database
        // Mohit Priyadarshi (Student 4, Roll 2510001320) - 6 subjects, 533/600 (89%, SGPA 9.33, A+)
        seedResult(17, 4, 1, 90.0, "A+");
        seedResult(18, 4, 2, 90.0, "A+");
        seedResult(19, 4, 3, 88.0, "A");
        seedResult(20, 4, 4, 91.0, "A+");
        seedResult(21, 4, 5, 81.0, "A");
        seedResult(22, 4, 6, 93.0, "A+");

        // Utkarsh Mishra (Student 1, Roll 2510001283)
        seedResult(45, 1, 1, 94.0, "A+");
        seedResult(2, 1, 2, 84.0, "A-");
        seedResult(3, 1, 3, 85.0, "A-");
        seedResult(46, 1, 4, 79.0, "B");
        seedResult(47, 1, 5, 62.0, "C");
        seedResult(125, 1, 6, 75.0, "B");

        // Abhishek Kumar (Student 2, Roll 2510001205)
        seedResult(29, 2, 1, 88.0, "A");
        seedResult(5, 2, 2, 76.0, "B");
        seedResult(30, 2, 3, 79.0, "B");
        seedResult(31, 2, 4, 77.0, "B");
        seedResult(32, 2, 5, 84.0, "A");
        seedResult(33, 2, 6, 81.0, "A");

        // Aditya Saini (Student 3, Roll 2510001348)
        seedResult(24, 3, 1, 77.0, "B");
        seedResult(7, 3, 2, 90.0, "A");
        seedResult(25, 3, 3, 66.0, "C");
        seedResult(26, 3, 4, 79.0, "B");
        seedResult(27, 3, 5, 34.0, "F");
        seedResult(28, 3, 6, 80.0, "A");

        // Moulik Tiwari (Student 5, Roll 2510001331)
        seedResult(40, 5, 1, 80.0, "A");
        seedResult(9, 5, 2, 86.0, "A-");
        seedResult(41, 5, 3, 77.0, "B");
        seedResult(42, 5, 4, 85.0, "A");
        seedResult(43, 5, 5, 65.0, "C");
        seedResult(44, 5, 6, 72.0, "B");

        // Adarsh Kumar (Student 6, Roll 2510001319)
        seedResult(14, 6, 1, 88.0, "A");
        seedResult(10, 6, 2, 87.0, "A");
        seedResult(11, 6, 3, 69.0, "C");
        seedResult(12, 6, 4, 80.0, "A");
        seedResult(13, 6, 5, 66.0, "C");
        seedResult(23, 6, 6, 88.0, "A");

        // Akshay Bharadwaj (Student 7, Roll 2510001212)
        seedResult(34, 7, 1, 76.0, "B");
        seedResult(35, 7, 2, 80.0, "A");
        seedResult(36, 7, 3, 79.0, "B");
        seedResult(37, 7, 4, 83.0, "A");
        seedResult(38, 7, 5, 90.0, "A+");
        seedResult(39, 7, 6, 83.0, "A");
    }

    public void putStudentDirect(Student s) {
        if (s != null) students.put(s.getStudentId(), s);
    }

    public void deleteStudentDirect(int id) {
        students.remove(id);
        results.values().removeIf(r -> r.getStudentId() == id);
    }

    public void putSubjectDirect(Subject s) {
        if (s != null) subjects.put(s.getSubjectId(), s);
    }

    public void deleteSubjectDirect(int id) {
        subjects.remove(id);
        results.values().removeIf(r -> r.getSubjectId() == id);
    }

    public void saveResultDirect(Result r) {
        if (r != null) {
            attachSubject(r);
            results.put(r.getResultId(), r);
            if (r.getResultId() >= nextResultId.get()) {
                nextResultId.set(r.getResultId() + 1);
            }
        }
    }

    public void deleteResultDirect(int id) {
        results.remove(id);
    }

    private void putStudent(Student s) {
        if (s != null) students.put(s.getStudentId(), s);
    }

    private void putSubject(Subject s) {
        if (s != null) subjects.put(s.getSubjectId(), s);
    }

    private void seedResult(int resultId, int studentId, int subjectId, double marks, String grade) {
        Result r = new Result(resultId, studentId, subjectId, marks, grade);
        Subject sub = subjects.get(subjectId);
        r.setSubject(sub);
        results.put(resultId, r);
        if (resultId >= nextResultId.get()) {
            nextResultId.set(resultId + 1);
        }
    }

    /**
     * Atomically replaces the stored dataset from a synchronization snapshot
     * without triggering recursive sync notifications.
     */
    public synchronized void loadSnapshot(List<Student> newStudents, List<Subject> newSubjects, List<Result> newResults) {
        if (newStudents != null) {
            students.clear();
            for (Student s : newStudents) {
                if (s != null) students.put(s.getStudentId(), s);
            }
        }
        if (newSubjects != null) {
            subjects.clear();
            for (Subject sub : newSubjects) {
                if (sub != null) subjects.put(sub.getSubjectId(), sub);
            }
        }
        if (newResults != null) {
            results.clear();
            for (Result r : newResults) {
                if (r != null) {
                    attachSubject(r);
                    results.put(r.getResultId(), r);
                    if (r.getResultId() >= nextResultId.get()) {
                        nextResultId.set(r.getResultId() + 1);
                    }
                }
            }
        }
    }

    // ==================== Student Operations ====================

    public List<Student> getAllStudents() {
        List<Student> list = new ArrayList<>(students.values());
        list.sort(Comparator.comparingInt(Student::getStudentId));
        return list;
    }

    public Optional<Student> findStudentById(int id) {
        return Optional.ofNullable(students.get(id));
    }

    public Optional<Student> findStudentByRoll(String roll) {
        if (roll == null) return Optional.empty();
        String trimmed = roll.trim();
        for (Student s : students.values()) {
            if (trimmed.equalsIgnoreCase(s.getRollNumber())) {
                return Optional.of(s);
            }
        }
        return Optional.empty();
    }

    public synchronized boolean saveStudent(Student s) {
        if (s == null) return false;
        if (s.getStudentId() <= 0) {
            int maxId = students.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
            s.setStudentId(maxId + 1);
        }
        students.put(s.getStudentId(), s);
        try { DataSyncBridge.notifyDataChanged(); } catch (Throwable ignored) {}
        return true;
    }

    public synchronized boolean deleteStudent(int id) {
        Student removed = students.remove(id);
        if (removed != null) {
            results.values().removeIf(r -> r.getStudentId() == id);
            try { DataSyncBridge.notifyDataChanged(); } catch (Throwable ignored) {}
            return true;
        }
        return false;
    }

    // ==================== Subject Operations ====================

    public List<Subject> getAllSubjects() {
        List<Subject> list = new ArrayList<>(subjects.values());
        list.sort(Comparator.comparing(Subject::getSubjectCode));
        return list;
    }

    public Optional<Subject> findSubjectById(int id) {
        return Optional.ofNullable(subjects.get(id));
    }

    public Optional<Subject> findSubjectByCode(String code) {
        if (code == null) return Optional.empty();
        String trimmed = code.trim();
        for (Subject s : subjects.values()) {
            if (trimmed.equalsIgnoreCase(s.getSubjectCode())) {
                return Optional.of(s);
            }
        }
        return Optional.empty();
    }

    public synchronized boolean saveSubject(Subject s) {
        if (s == null) return false;
        if (s.getSubjectId() <= 0) {
            int maxId = subjects.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
            s.setSubjectId(maxId + 1);
        }
        subjects.put(s.getSubjectId(), s);
        try { DataSyncBridge.notifyDataChanged(); } catch (Throwable ignored) {}
        return true;
    }

    public synchronized boolean deleteSubject(int id) {
        boolean removed = subjects.remove(id) != null;
        if (removed) {
            try { DataSyncBridge.notifyDataChanged(); } catch (Throwable ignored) {}
        }
        return removed;
    }

    // ==================== Result Operations ====================

    public List<Result> getAllResults() {
        List<Result> list = new ArrayList<>();
        for (Result r : results.values()) {
            attachSubject(r);
            list.add(r);
        }
        return list;
    }

    public List<Result> getResultsByStudentId(int studentId) {
        List<Result> list = new ArrayList<>();
        for (Result r : results.values()) {
            if (r.getStudentId() == studentId) {
                attachSubject(r);
                list.add(r);
            }
        }
        list.sort(Comparator.comparing(r -> r.getSubject() != null ? r.getSubject().getSubjectCode() : ""));
        return list;
    }

    public Optional<Result> findResultById(int id) {
        Result r = results.get(id);
        if (r != null) attachSubject(r);
        return Optional.ofNullable(r);
    }

    public Optional<Result> findResultByStudentAndSubject(int studentId, int subjectId) {
        for (Result r : results.values()) {
            if (r.getStudentId() == studentId && r.getSubjectId() == subjectId) {
                attachSubject(r);
                return Optional.of(r);
            }
        }
        return Optional.empty();
    }

    public synchronized boolean saveOrUpdateResult(Result result) {
        if (result == null || result.getStudentId() <= 0 || result.getSubjectId() <= 0) {
            return false;
        }

        // Check if an existing result exists for student & subject
        Result existing = null;
        for (Result r : results.values()) {
            if (r.getStudentId() == result.getStudentId() && r.getSubjectId() == result.getSubjectId()) {
                existing = r;
                break;
            }
        }

        if (existing != null) {
            existing.setMarksObtained(result.getMarksObtained());
            existing.setGrade(result.getGrade() != null ? result.getGrade() : calculateGrade(result.getMarksObtained(), 100));
            attachSubject(existing);
            result.setResultId(existing.getResultId());
            result.setGrade(existing.getGrade());
            result.setSubject(existing.getSubject());
        } else {
            int newId = result.getResultId() > 0 ? result.getResultId() : nextResultId.getAndIncrement();
            result.setResultId(newId);
            if (result.getGrade() == null || result.getGrade().isEmpty()) {
                result.setGrade(calculateGrade(result.getMarksObtained(), 100));
            }
            attachSubject(result);
            results.put(newId, result);
        }
        try { DataSyncBridge.notifyDataChanged(); } catch (Throwable ignored) {}
        return true;
    }

    public synchronized boolean deleteResult(int resultId) {
        boolean removed = results.remove(resultId) != null;
        if (removed) {
            try { DataSyncBridge.notifyDataChanged(); } catch (Throwable ignored) {}
        }
        return removed;
    }

    private void attachSubject(Result r) {
        if (r != null && r.getSubject() == null) {
            Subject sub = subjects.get(r.getSubjectId());
            r.setSubject(sub);
        }
    }

    // ==================== Dynamic Marksheet Calculation ====================

    public StudentResultSummary generateSummaryForRoll(String roll) {
        Optional<Student> optStudent = findStudentByRoll(roll);
        if (!optStudent.isPresent()) {
            return null;
        }
        Student s = optStudent.get();
        List<Result> studentResults = getResultsByStudentId(s.getStudentId());
        if (studentResults.isEmpty()) {
            return null;
        }

        double totalObtained = 0.0;
        double totalMax = 0.0;
        int totalCredits = 0;
        double totalCreditPoints = 0.0;
        boolean hasFailed = false;

        for (Result r : studentResults) {
            Subject sub = r.getSubject();
            double max = sub != null ? sub.getMaxMarks() : 100.0;
            int pass = sub != null ? sub.getPassMarks() : 35;
            int credits = sub != null ? sub.getCredits() : 4;

            totalObtained += r.getMarksObtained();
            totalMax += max;
            totalCredits += credits;

            double gp = calculateGradePoint(r.getMarksObtained(), max);
            totalCreditPoints += (credits * gp);

            if (r.getMarksObtained() < pass) {
                hasFailed = true;
            }
        }

        StudentResultSummary sum = new StudentResultSummary(s, studentResults);
        sum.setTotalMarksObtained(Math.round(totalObtained * 10.0) / 10.0);
        sum.setTotalMaxMarks(totalMax);
        sum.setTotalCredits(totalCredits);
        sum.setTotalCreditPoints(Math.round(totalCreditPoints * 100.0) / 100.0);

        double pct = totalMax > 0 ? (totalObtained / totalMax) * 100.0 : 0.0;
        sum.setPercentage(Math.round(pct * 100.0) / 100.0);

        double sgpa = totalCredits > 0 ? (totalCreditPoints / totalCredits) : 0.0;
        sum.setSgpa(Math.round(sgpa * 100.0) / 100.0);

        sum.setOverallGrade(calculateOverallGrade(sum.getPercentage()));
        boolean isPass = !hasFailed && pct >= 35.0;
        sum.setStatus(isPass ? "PASS" : "FAIL");

        if (isPass) {
            if (sum.getSgpa() >= 8.5) {
                sum.setDivision("First Class with Distinction");
            } else if (sum.getSgpa() >= 6.5) {
                sum.setDivision("First Class");
            } else if (sum.getSgpa() >= 5.0) {
                sum.setDivision("Second Class");
            } else {
                sum.setDivision("Pass Class");
            }
        } else {
            sum.setDivision("Fail / Reappear");
        }

        return sum;
    }

    public static double calculateGradePoint(double marks, double maxMarks) {
        if (maxMarks <= 0) return 0.0;
        double pct = (marks / maxMarks) * 100.0;
        if (pct >= 90.0) return 10.0;
        if (pct >= 80.0) return 9.0;
        if (pct >= 70.0) return 8.0;
        if (pct >= 60.0) return 7.0;
        if (pct >= 55.0) return 6.0;
        if (pct >= 50.0) return 5.0;
        if (pct >= 40.0) return 4.0;
        return 0.0;
    }

    public static String calculateGrade(double marks, double maxMarks) {
        if (maxMarks <= 0) return "N/A";
        double percentage = (marks / maxMarks) * 100.0;
        if (percentage >= 90) return "A+";
        if (percentage >= 80) return "A";
        if (percentage >= 70) return "B";
        if (percentage >= 60) return "C";
        if (percentage >= 50) return "D";
        if (percentage >= 35) return "E";
        return "F";
    }

    public static String calculateOverallGrade(double percentage) {
        if (percentage >= 90) return "A+";
        if (percentage >= 80) return "A";
        if (percentage >= 70) return "B";
        if (percentage >= 60) return "C";
        if (percentage >= 50) return "D";
        if (percentage >= 35) return "E";
        return "F";
    }
}
