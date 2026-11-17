package result.model;

/**
 * Entity representing marks obtained by a student in a specific subject.
 */
public class Result {

    private int resultId;
    private int studentId;
    private int subjectId;
    private double marksObtained;
    private String grade;

    // Optional navigation/display helper objects
    private Student student;
    private Subject subject;

    public Result() {
    }

    public Result(int resultId, int studentId, int subjectId, double marksObtained, String grade) {
        this.resultId = resultId;
        this.studentId = studentId;
        this.subjectId = subjectId;
        this.marksObtained = marksObtained;
        this.grade = grade;
    }

    public int getResultId() {
        return resultId;
    }

    public void setResultId(int resultId) {
        this.resultId = resultId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    public double getMarksObtained() {
        return marksObtained;
    }

    public void setMarksObtained(double marksObtained) {
        this.marksObtained = marksObtained;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    @Override
    public String toString() {
        return "Result{" +
                "resultId=" + resultId +
                ", studentId=" + studentId +
                ", subjectId=" + subjectId +
                ", marksObtained=" + marksObtained +
                ", grade='" + grade + '\'' +
                '}';
    }
}

