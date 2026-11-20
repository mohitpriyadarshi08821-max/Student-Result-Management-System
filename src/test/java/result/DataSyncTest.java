package result;

import result.dao.InMemoryDataStore;
import result.database.DataSyncBridge;
import result.model.Result;
import result.model.Student;
import result.model.StudentResultSummary;
import result.model.Subject;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class DataSyncTest {
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("  Testing Bidirectional Data Synchronization...  ");
        System.out.println("=================================================");

        InMemoryDataStore ds = InMemoryDataStore.getInstance();

        // Test 1: Verify all 7 reference students exist
        List<Student> students = ds.getAllStudents();
        System.out.println("[TEST 1] Initial Student Count: " + students.size());
        assert students.size() == 7 : "Expected 7 students, found " + students.size();
        for (Student s : students) {
            System.out.println("  - Student: " + s.getRollNumber() + " | " + s.getFullName() + " | " + s.getCourse());
        }

        // Test 2: Verify 6 curriculum subjects exist
        List<Subject> subjects = ds.getAllSubjects();
        System.out.println("[TEST 2] Initial Subject Count: " + subjects.size());
        assert subjects.size() == 6 : "Expected 6 subjects, found " + subjects.size();
        for (Subject sub : subjects) {
            System.out.println("  - Subject: " + sub.getSubjectCode() + " | " + sub.getSubjectName());
        }

        // Test 3: Verify Mohit Priyadarshi (Roll 2510001320) has exactly 6 subjects and 533 marks
        StudentResultSummary mohitSummary = ds.generateSummaryForRoll("2510001320");
        assert mohitSummary != null : "Summary for Mohit Priyadarshi was null!";
        System.out.println("[TEST 3] Mohit Priyadarshi Summary:");
        System.out.println("  - Total Subjects: " + mohitSummary.getResults().size());
        System.out.println("  - Total Marks: " + mohitSummary.getTotalMarksObtained() + " / " + mohitSummary.getTotalMaxMarks());
        System.out.println("  - SGPA: " + mohitSummary.getSgpa());
        System.out.println("  - Percentage: " + mohitSummary.getPercentage() + "%");
        System.out.println("  - Division: " + mohitSummary.getDivision());
        System.out.println("  - Status: " + mohitSummary.getStatus());
        assert mohitSummary.getResults().size() == 6 : "Expected 6 results for Mohit, got " + mohitSummary.getResults().size();
        assert mohitSummary.getTotalMarksObtained() == 533.0 : "Expected 533.0 marks for Mohit, got " + mohitSummary.getTotalMarksObtained();
        assert mohitSummary.getSgpa() > 9.0 : "Expected SGPA > 9.0, got " + mohitSummary.getSgpa();

        // Test 4: Serialization to JSON
        String json = DataSyncBridge.serializeToJson(students, subjects, ds.getAllResults());
        assert json.contains("\"rollNumber\":\"2510001320\"") : "JSON missing Mohit Priyadarshi";
        assert json.contains("\"subjectCode\":\"CS107\"") : "JSON missing CS107";
        System.out.println("[TEST 4] JSON serialization verified. Payload size: " + json.length() + " chars.");

        // Test 5: Snapshot load and atomic replacement
        boolean applySuccess = DataSyncBridge.applyJsonToInMemory(json);
        assert applySuccess : "applyJsonToInMemory returned false!";
        assert ds.getAllStudents().size() == 7 : "Students count mismatch after snapshot apply";
        System.out.println("[TEST 5] Snapshot load verified. Student count: " + ds.getAllStudents().size());

        // Test 6: Simulating a student addition from Website / Application
        Student testStudent = new Student(99, "2510009999", "Test", "Student", "test@test.com", "B.Tech CSE", 1, "Male", LocalDate.now().minusYears(18));
        ds.saveStudent(testStudent);
        Optional<Student> found = ds.findStudentByRoll("2510009999");
        assert found.isPresent() : "Test student not found after saveStudent";
        System.out.println("[TEST 6] Add student verified: " + found.get().getFullName() + " (Total: " + ds.getAllStudents().size() + ")");

        // Test 7: Simulating student update
        testStudent.setFirstName("UpdatedTest");
        ds.saveStudent(testStudent);
        Optional<Student> updated = ds.findStudentByRoll("2510009999");
        assert updated.isPresent() && "UpdatedTest".equals(updated.get().getFirstName()) : "Update student failed";
        System.out.println("[TEST 7] Update student verified: " + updated.get().getFullName());

        // Test 8: Simulating student deletion
        ds.deleteStudent(99);
        assert !ds.findStudentByRoll("2510009999").isPresent() : "Student 99 still exists after delete";
        assert ds.getAllStudents().size() == 7 : "Expected 7 students after deleting test student, found " + ds.getAllStudents().size();
        System.out.println("[TEST 8] Delete student verified (Total back to 7).");

        System.out.println("=================================================");
        System.out.println("  ALL 8 BIDIRECTIONAL SYNC TESTS PASSED!        ");
        System.out.println("=================================================");
    }
}
