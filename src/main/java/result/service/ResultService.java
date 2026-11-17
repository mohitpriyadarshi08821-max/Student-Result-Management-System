package result.service;

import result.dao.ResultDAO;
import result.dao.StudentDAO;
import result.dao.SubjectDAO;
import result.exception.RecordNotFoundException;
import result.exception.ValidationException;
import result.model.Result;
import result.model.Student;
import result.model.StudentResultSummary;
import result.model.Subject;

import java.util.List;
import java.util.Optional;

/**
 * Service class handling business logic for marks calculation, grade determination,
 * and marksheet compilation.
 */
public class ResultService {

    private final ResultDAO resultDAO;
    private final StudentDAO studentDAO;
    private final SubjectDAO subjectDAO;

    public ResultService(ResultDAO resultDAO, StudentDAO studentDAO, SubjectDAO subjectDAO) {
        this.resultDAO = resultDAO;
        this.studentDAO = studentDAO;
        this.subjectDAO = subjectDAO;
    }

    public boolean recordResult(Result result) throws ValidationException {
        if (result == null) {
            throw new ValidationException("Result information cannot be empty.");
        }
        if (result.getStudentId() <= 0) {
            throw new ValidationException("A valid student must be selected.");
        }
        if (result.getSubjectId() <= 0) {
            throw new ValidationException("A valid subject must be selected.");
        }

        // Retrieve subject to validate maximum allowed marks
        Subject subject = subjectDAO.findById(result.getSubjectId())
                .orElseThrow(() -> new ValidationException("The selected subject does not exist in the curriculum."));

        if (result.getMarksObtained() < 0) {
            throw new ValidationException("Marks obtained cannot be negative.");
        }
        if (result.getMarksObtained() > subject.getMaxMarks()) {
            throw new ValidationException("Marks (" + result.getMarksObtained() + ") cannot exceed maximum marks ("
                    + subject.getMaxMarks() + ") for " + subject.getSubjectName() + ".");
        }

        // Calculate and assign letter grade based on marks and max marks
        String grade = calculateGrade(result.getMarksObtained(), subject.getMaxMarks());
        result.setGrade(grade);

        // Check if a result already exists for this student and subject
        Optional<Result> existing = resultDAO.findByStudentAndSubject(result.getStudentId(), result.getSubjectId());
        if (existing.isPresent()) {
            result.setResultId(existing.get().getResultId());
            return resultDAO.update(result);
        } else {
            return resultDAO.insert(result);
        }
    }

    public StudentResultSummary generateResultSummary(String rollNumber) throws RecordNotFoundException {
        if (rollNumber == null || rollNumber.trim().isEmpty()) {
            throw new RecordNotFoundException("Please enter a valid Roll Number to view results.");
        }

        Student student = studentDAO.findByRollNumber(rollNumber.trim())
                .orElseThrow(() -> new RecordNotFoundException("Student with Roll Number '" + rollNumber.trim() + "' was not found."));

        List<Result> results = resultDAO.findByStudentId(student.getStudentId());
        if (results.isEmpty()) {
            throw new RecordNotFoundException("No exam results have been recorded for "
                    + student.getFullName() + " (Roll No: " + rollNumber.trim() + ").");
        }

        double totalObtained = 0.0;
        double totalMax = 0.0;
        int totalCredits = 0;
        double totalCreditPoints = 0.0;
        boolean hasFailedSubject = false;

        for (Result r : results) {
            Subject sub = r.getSubject();
            if (sub == null) {
                sub = subjectDAO.findById(r.getSubjectId()).orElse(null);
                r.setSubject(sub);
            }

            double max = sub != null ? sub.getMaxMarks() : 100;
            int pass = sub != null ? sub.getPassMarks() : 35;
            int credits = sub != null ? sub.getCredits() : 4;

            totalObtained += r.getMarksObtained();
            totalMax += max;
            totalCredits += credits;

            double gradePoint = calculateGradePoint(r.getMarksObtained(), max);
            totalCreditPoints += (credits * gradePoint);

            if (r.getMarksObtained() < pass) {
                hasFailedSubject = true;
            }
        }

        StudentResultSummary summary = new StudentResultSummary(student, results);
        summary.setTotalMarksObtained(totalObtained);
        summary.setTotalMaxMarks(totalMax);
        summary.setTotalCredits(totalCredits);
        summary.setTotalCreditPoints(totalCreditPoints);

        double percentage = totalMax > 0 ? (totalObtained / totalMax) * 100.0 : 0.0;
        summary.setPercentage(Math.round(percentage * 100.0) / 100.0);
        summary.setOverallGrade(calculateOverallGrade(summary.getPercentage()));
        boolean isPass = !hasFailedSubject && percentage >= 35.0;
        summary.setStatus(isPass ? "PASS" : "FAIL");

        double sgpa = totalCredits > 0 ? (totalCreditPoints / totalCredits) : 0.0;
        summary.setSgpa(Math.round(sgpa * 100.0) / 100.0);

        if (isPass) {
            if (summary.getSgpa() >= 8.5) {
                summary.setDivision("First Class with Distinction");
            } else if (summary.getSgpa() >= 6.5) {
                summary.setDivision("First Class");
            } else if (summary.getSgpa() >= 5.0) {
                summary.setDivision("Second Class");
            } else {
                summary.setDivision("Pass Class");
            }
        } else {
            summary.setDivision("Fail / Reappear");
        }

        return summary;
    }

    public double calculateGradePoint(double marks, double maxMarks) {
        if (maxMarks <= 0) return 0.0;
        double pct = (marks / maxMarks) * 100.0;
        if (pct >= 90.0) return 10.0;
        if (pct >= 80.0) return 9.0;
        if (pct >= 70.0) return 8.0;
        if (pct >= 60.0) return 7.0;
        if (pct >= 55.0) return 6.0;
        if (pct >= 45.0) return 5.0;
        if (pct >= 40.0) return 4.0;
        return 0.0;
    }

    public String calculateGrade(double marks, double maxMarks) {
        if (maxMarks <= 0) {
            return "N/A";
        }
        double percentage = (marks / maxMarks) * 100.0;
        if (percentage >= 90) return "A+";
        if (percentage >= 80) return "A";
        if (percentage >= 70) return "B";
        if (percentage >= 60) return "C";
        if (percentage >= 50) return "D";
        if (percentage >= 35) return "E";
        return "F";
    }

    private String calculateOverallGrade(double percentage) {
        if (percentage >= 90) return "A+";
        if (percentage >= 80) return "A";
        if (percentage >= 70) return "B";
        if (percentage >= 60) return "C";
        if (percentage >= 50) return "D";
        if (percentage >= 35) return "E";
        return "F";
    }

    public List<Subject> getAllSubjects() {
        return subjectDAO.findAll();
    }

    public List<Result> getResultsByStudentId(int studentId) {
        return resultDAO.findByStudentId(studentId);
    }

    public boolean deleteResult(int resultId) {
        return resultDAO.delete(resultId);
    }
}
