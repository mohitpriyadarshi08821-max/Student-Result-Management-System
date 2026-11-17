package result.model;

/**
 * Entity representing an academic subject or course offering.
 */
public class Subject {

    private int subjectId;
    private String subjectCode;
    private String subjectName;
    private int maxMarks;
    private int passMarks;

    public Subject() {
    }

    public Subject(int subjectId, String subjectCode, String subjectName, int maxMarks, int passMarks) {
        this.subjectId = subjectId;
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.maxMarks = maxMarks;
        this.passMarks = passMarks;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public int getMaxMarks() {
        return maxMarks;
    }

    public void setMaxMarks(int maxMarks) {
        this.maxMarks = maxMarks;
    }

    public int getPassMarks() {
        return passMarks;
    }

    public void setPassMarks(int passMarks) {
        this.passMarks = passMarks;
    }

    @Override
    public String toString() {
        return subjectCode + " - " + subjectName;
    }
}

