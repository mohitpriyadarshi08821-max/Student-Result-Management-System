package result.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Data transfer model encapsulating the complete academic performance report
 * of a student across all subjects.
 */
public class StudentResultSummary {

    private Student student;
    private List<Result> results = new ArrayList<>();
    private double totalMarksObtained;
    private double totalMaxMarks;
    private double percentage;
    private String overallGrade;
    private String status; // "PASS" or "FAIL"

    public StudentResultSummary() {
    }

    public StudentResultSummary(Student student, List<Result> results) {
        this.student = student;
        this.results = results != null ? results : new ArrayList<>();
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

    public double getTotalMarksObtained() {
        return totalMarksObtained;
    }

    public void setTotalMarksObtained(double totalMarksObtained) {
        this.totalMarksObtained = totalMarksObtained;
    }

    public double getTotalMaxMarks() {
        return totalMaxMarks;
    }

    public void setTotalMaxMarks(double totalMaxMarks) {
        this.totalMaxMarks = totalMaxMarks;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public String getOverallGrade() {
        return overallGrade;
    }

    public void setOverallGrade(String overallGrade) {
        this.overallGrade = overallGrade;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

