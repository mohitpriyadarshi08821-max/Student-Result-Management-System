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
    private int totalCredits;
    private double totalCreditPoints;
    private double sgpa;
    private String division = "N/A";

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

    public int getTotalCredits() {
        return totalCredits;
    }

    public void setTotalCredits(int totalCredits) {
        this.totalCredits = totalCredits;
    }

    public double getTotalCreditPoints() {
        return totalCreditPoints;
    }

    public void setTotalCreditPoints(double totalCreditPoints) {
        this.totalCreditPoints = totalCreditPoints;
    }

    public double getSgpa() {
        return sgpa;
    }

    public void setSgpa(double sgpa) {
        this.sgpa = sgpa;
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }
}

