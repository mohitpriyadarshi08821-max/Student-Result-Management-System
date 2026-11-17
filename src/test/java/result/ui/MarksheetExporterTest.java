package result.ui;

import result.model.Result;
import result.model.Student;
import result.model.StudentResultSummary;
import result.model.Subject;
import result.service.MarksheetExporter;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MarksheetExporterTest {

    public static void main(String[] args) throws Exception {
        System.out.println("Starting MarksheetExporter & Print Download Verification Test...");

        // Setup sample student and results
        Student student = new Student(1, "2510001320", "Mohit", "Priyadarshi", "mohit@example.com", "B.Tech Computer Science", 4, "Male", LocalDate.now().minusYears(19));
        List<Result> results = new ArrayList<>();
        Result r1 = new Result(1, 1, 1, 88.0, "B");
        r1.setSubject(new Subject(1, "CS101", "Java Programming", 100, 35));
        Result r2 = new Result(2, 1, 2, 92.0, "A");
        r2.setSubject(new Subject(2, "CS102", "Database Management Systems", 100, 35));
        results.add(r1);
        results.add(r2);

        StudentResultSummary summary = new StudentResultSummary(student, results);
        summary.setTotalMarksObtained(180.0);
        summary.setTotalMaxMarks(200.0);
        summary.setPercentage(90.0);
        summary.setOverallGrade("A+");
        summary.setStatus("PASS");

        // 1. Test PDF generation
        byte[] pdfBytes = MarksheetExporter.generatePdf(summary);
        if (pdfBytes == null || pdfBytes.length == 0) {
            throw new AssertionError("FAILED: PDF bytes should not be empty");
        }
        String pdfHeader = new String(pdfBytes, 0, 8);
        if (!pdfHeader.startsWith("%PDF-1.4")) {
            throw new AssertionError("FAILED: PDF should start with %PDF-1.4, got: " + pdfHeader);
        }
        String pdfContent = new String(pdfBytes, java.nio.charset.StandardCharsets.ISO_8859_1);
        if (!pdfContent.contains("SGPA:")) {
            throw new AssertionError("FAILED: PDF must contain SGPA in the performance summary ribbon");
        }
        if (!pdfContent.contains("Credits")) {
            throw new AssertionError("FAILED: PDF must contain Credits column");
        }
        System.out.println("PDF generation test passed! Size: " + pdfBytes.length + " bytes (SGPA verified)");

        // 2. Test PDF export to file
        File tempPdf = File.createTempFile("test_marksheet", ".pdf");
        tempPdf.deleteOnExit();
        boolean pdfExportSuccess = MarksheetExporter.exportToPdf(summary, tempPdf);
        if (!pdfExportSuccess || !tempPdf.exists() || tempPdf.length() == 0) {
            throw new AssertionError("FAILED: PDF file export failed");
        }
        System.out.println("PDF file export test passed! File: " + tempPdf.getAbsolutePath());

        // 3. Test HTML generation
        String html = MarksheetExporter.generateHtml(summary);
        if (html == null || !html.contains("STUDENT RESULT MANAGEMENT SYSTEM") || !html.contains("2510001320") || !html.contains("Mohit Priyadarshi") || !html.contains("SGPA:") || !html.contains("Credits")) {
            throw new AssertionError("FAILED: HTML does not contain expected marksheet content (SGPA or Credits missing)");
        }
        System.out.println("HTML generation test passed! HTML length: " + html.length() + " (SGPA and Credits verified)");

        // 4. Test HTML export to file
        File tempHtml = File.createTempFile("test_marksheet", ".html");
        tempHtml.deleteOnExit();
        boolean htmlExportSuccess = MarksheetExporter.exportToHtml(summary, tempHtml);
        if (!htmlExportSuccess || !tempHtml.exists() || tempHtml.length() == 0) {
            throw new AssertionError("FAILED: HTML file export failed");
        }
        System.out.println("HTML file export test passed! File: " + tempHtml.getAbsolutePath());

        System.out.println("ALL MARKSHEET EXPORT & DOWNLOAD TESTS PASSED SUCCESSFULLY!");
    }
}
