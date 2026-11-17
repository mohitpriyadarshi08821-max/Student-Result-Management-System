package result.service;

import result.model.Result;
import result.model.Student;
import result.model.StudentResultSummary;
import result.model.Subject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility service providing high-quality PDF and HTML marksheet document generation
 * and file downloading for the Student Result Management System.
 */
public class MarksheetExporter {

    private MarksheetExporter() {
        // Prevent instantiation of utility class
    }

    /**
     * Exports the student marksheet to a PDF file.
     */
    public static boolean exportToPdf(StudentResultSummary summary, File targetFile) throws IOException {
        if (summary == null || targetFile == null) {
            return false;
        }
        byte[] pdfBytes = generatePdf(summary);
        File parent = targetFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (FileOutputStream fos = new FileOutputStream(targetFile)) {
            fos.write(pdfBytes);
            fos.flush();
        }
        return true;
    }

    /**
     * Exports the student marksheet to a print-ready HTML file.
     */
    public static boolean exportToHtml(StudentResultSummary summary, File targetFile) throws IOException {
        if (summary == null || targetFile == null) {
            return false;
        }
        String html = generateHtml(summary);
        File parent = targetFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (FileOutputStream fos = new FileOutputStream(targetFile)) {
            fos.write(html.getBytes(StandardCharsets.UTF_8));
            fos.flush();
        }
        return true;
    }

    /**
     * Generates standard ISO 32000-1 / PDF 1.4 compliant document bytes for the marksheet.
     */
    public static byte[] generatePdf(StudentResultSummary summary) throws IOException {
        if (summary == null) {
            throw new IllegalArgumentException("StudentResultSummary cannot be null");
        }

        StringBuilder sb = new StringBuilder();

        double pageWidth = 612;  // Letter width
        double pageHeight = 792; // Letter height
        double margin = 40;
        double contentWidth = pageWidth - (2 * margin); // 532 pt

        // 1. Header Banner Box (#0C1726 - Dark Navy)
        sb.append("q\n");
        sb.append("0.05 0.09 0.15 rg\n"); // #0C1726
        sb.append(String.format(java.util.Locale.US, "%.1f %.1f %.1f %.1f re f\n", margin, pageHeight - margin - 70, contentWidth, 70.0));

        // Teal accent bar (#0D9488)
        sb.append("0.05 0.58 0.53 rg\n"); // #0D9488
        sb.append(String.format(java.util.Locale.US, "%.1f %.1f %.1f %.1f re f\n", margin, pageHeight - margin - 74, contentWidth, 4.0));
        sb.append("Q\n");

        // Header Title
        sb.append("BT\n");
        sb.append("/F2 18 Tf\n"); // Bold 18pt
        sb.append("1 1 1 rg\n"); // White
        sb.append(String.format(java.util.Locale.US, "%.1f %.1f Td\n", margin + 18, pageHeight - margin - 35));
        sb.append("(STUDENT RESULT MANAGEMENT SYSTEM) Tj\n");
        sb.append("ET\n");

        // Header Subtitle
        sb.append("BT\n");
        sb.append("/F1 10 Tf\n"); // Regular 10pt
        sb.append("0.8 0.85 0.9 rg\n"); // Light Slate
        sb.append(String.format(java.util.Locale.US, "%.1f %.1f Td\n", margin + 18, pageHeight - margin - 52));
        sb.append("(Official Statement of Marks & Academic Performance Transcript) Tj\n");
        sb.append("ET\n");

        // 2. Student Details Box (#F8FAFC with #E2E8F0 border)
        double infoY = pageHeight - margin - 170;
        double infoHeight = 85;
        sb.append("q\n");
        sb.append("0.97 0.98 0.99 rg\n"); // #F8FAFC
        sb.append("0.89 0.91 0.94 RG\n"); // #E2E8F0
        sb.append("1 w\n");
        sb.append(String.format(java.util.Locale.US, "%.1f %.1f %.1f %.1f re B\n", margin, infoY, contentWidth, infoHeight));
        sb.append("Q\n");

        Student st = summary.getStudent();
        String studentName = st != null ? st.getFullName() : "N/A";
        String rollNumber = st != null ? st.getRollNumber() : "N/A";
        String course = st != null ? st.getCourse() : "N/A";
        int semester = st != null ? st.getSemester() : 1;
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"));

        // Column 1
        drawPdfText(sb, "F2", 10, 0.1, 0.15, 0.25, margin + 16, infoY + infoHeight - 24, "Student Name:");
        drawPdfText(sb, "F1", 10, 0.1, 0.15, 0.25, margin + 95, infoY + infoHeight - 24, studentName);

        drawPdfText(sb, "F2", 10, 0.1, 0.15, 0.25, margin + 16, infoY + infoHeight - 44, "Roll Number:");
        drawPdfText(sb, "F1", 10, 0.1, 0.15, 0.25, margin + 95, infoY + infoHeight - 44, rollNumber);

        drawPdfText(sb, "F2", 10, 0.1, 0.15, 0.25, margin + 16, infoY + infoHeight - 64, "Academic Year:");
        drawPdfText(sb, "F1", 10, 0.1, 0.15, 0.25, margin + 95, infoY + infoHeight - 64, "2026 - 2027");

        // Column 2
        double col2X = margin + 280;
        drawPdfText(sb, "F2", 10, 0.1, 0.15, 0.25, col2X, infoY + infoHeight - 24, "Course / Program:");
        drawPdfText(sb, "F1", 10, 0.1, 0.15, 0.25, col2X + 105, infoY + infoHeight - 24, course);

        drawPdfText(sb, "F2", 10, 0.1, 0.15, 0.25, col2X, infoY + infoHeight - 44, "Semester:");
        drawPdfText(sb, "F1", 10, 0.1, 0.15, 0.25, col2X + 105, infoY + infoHeight - 44, "Semester " + semester);

        drawPdfText(sb, "F2", 10, 0.1, 0.15, 0.25, col2X, infoY + infoHeight - 64, "Date of Issue:");
        drawPdfText(sb, "F1", 10, 0.1, 0.15, 0.25, col2X + 105, infoY + infoHeight - 64, dateStr);

        // 3. Subject Results Table
        double tableTop = infoY - 20;
        double rowHeight = 24;

        // Table Header (#0F172A)
        sb.append("q\n");
        sb.append("0.06 0.09 0.16 rg\n"); // #0F172A
        sb.append(String.format(java.util.Locale.US, "%.1f %.1f %.1f %.1f re f\n", margin, tableTop - rowHeight, contentWidth, rowHeight));
        sb.append("Q\n");

        double[] colWidths = {65, 172, 45, 55, 55, 60, 45, 35};
        String[] headers = {"Code", "Subject Name", "Credits", "Max", "Pass", "Obtained", "Grade", "Status"};

        double curX = margin;
        for (int i = 0; i < headers.length; i++) {
            drawPdfText(sb, "F2", 9, 1, 1, 1, curX + 6, tableTop - 16, headers[i]);
            curX += colWidths[i];
        }

        // Table Rows
        double curY = tableTop - rowHeight;
        List<Result> results = summary.getResults() != null ? summary.getResults() : new ArrayList<>();
        for (int r = 0; r < results.size(); r++) {
            Result res = results.get(r);
            Subject sub = res.getSubject();
            curY -= rowHeight;

            // Zebra Striping
            if (r % 2 == 1) {
                sb.append("q\n");
                sb.append("0.97 0.98 0.99 rg\n"); // #F8FAFC
                sb.append(String.format(java.util.Locale.US, "%.1f %.1f %.1f %.1f re f\n", margin, curY, contentWidth, rowHeight));
                sb.append("Q\n");
            }

            // Row Bottom Border
            sb.append("q\n");
            sb.append("0.9 0.92 0.95 RG\n"); // #E2E8F0
            sb.append("0.5 w\n");
            sb.append(String.format(java.util.Locale.US, "%.1f %.1f m %.1f %.1f l s\n", margin, curY, margin + contentWidth, curY));
            sb.append("Q\n");

            String code = sub != null ? sub.getSubjectCode() : "N/A";
            String name = sub != null ? sub.getSubjectName() : "N/A";
            int credits = sub != null ? sub.getCredits() : 4;
            int max = sub != null ? sub.getMaxMarks() : 100;
            int pass = sub != null ? sub.getPassMarks() : 35;
            double obtained = res.getMarksObtained();
            String grade = res.getGrade() != null ? res.getGrade() : "-";
            String status = obtained >= pass ? "PASS" : "FAIL";

            curX = margin;
            drawPdfText(sb, "F1", 9, 0.1, 0.15, 0.25, curX + 6, curY + 7, code);
            curX += colWidths[0];
            drawPdfText(sb, "F1", 9, 0.1, 0.15, 0.25, curX + 6, curY + 7, name);
            curX += colWidths[1];
            drawPdfText(sb, "F1", 9, 0.2, 0.25, 0.35, curX + 16, curY + 7, String.valueOf(credits));
            curX += colWidths[2];
            drawPdfText(sb, "F1", 9, 0.2, 0.25, 0.35, curX + 16, curY + 7, String.valueOf(max));
            curX += colWidths[3];
            drawPdfText(sb, "F1", 9, 0.2, 0.25, 0.35, curX + 16, curY + 7, String.valueOf(pass));
            curX += colWidths[4];
            drawPdfText(sb, "F2", 9, 0.05, 0.1, 0.2, curX + 14, curY + 7, String.format(java.util.Locale.US, "%.1f", obtained));
            curX += colWidths[5];
            drawPdfText(sb, "F2", 9, 0.05, 0.1, 0.2, curX + 14, curY + 7, grade);
            curX += colWidths[6];

            if ("PASS".equalsIgnoreCase(status)) {
                drawPdfText(sb, "F2", 9, 0.08, 0.64, 0.29, curX + 4, curY + 7, "PASS");
            } else {
                drawPdfText(sb, "F2", 9, 0.86, 0.15, 0.15, curX + 4, curY + 7, "FAIL");
            }
        }

        // Table Outline
        double tableHeight = (results.size() + 1) * rowHeight;
        sb.append("q\n");
        sb.append("0.89 0.91 0.94 RG\n");
        sb.append("1 w\n");
        sb.append(String.format(java.util.Locale.US, "%.1f %.1f %.1f %.1f re s\n", margin, tableTop - tableHeight, contentWidth, tableHeight));
        sb.append("Q\n");

        // 4. Performance Summary Ribbon (#F8FAFC Box)
        double summaryY = tableTop - tableHeight - 60;
        double summaryHeight = 44;
        sb.append("q\n");
        sb.append("0.97 0.98 0.99 rg\n");
        sb.append("0.89 0.91 0.94 RG\n");
        sb.append("1 w\n");
        sb.append(String.format(java.util.Locale.US, "%.1f %.1f %.1f %.1f re B\n", margin, summaryY, contentWidth, summaryHeight));
        sb.append("Q\n");

        double sgpa = summary.getSgpa();
        if (sgpa <= 0 && summary.getResults() != null && !summary.getResults().isEmpty()) {
            int totCred = 0;
            double totPts = 0;
            for (Result r : summary.getResults()) {
                Subject sub = r.getSubject();
                int cred = (sub != null && sub.getCredits() > 0) ? sub.getCredits() : 4;
                double max = (sub != null && sub.getMaxMarks() > 0) ? sub.getMaxMarks() : 100.0;
                double pct = (r.getMarksObtained() / max) * 100.0;
                double gp = 0.0;
                if (pct >= 90.0) gp = 10.0;
                else if (pct >= 80.0) gp = 9.0;
                else if (pct >= 70.0) gp = 8.0;
                else if (pct >= 60.0) gp = 7.0;
                else if (pct >= 55.0) gp = 6.0;
                else if (pct >= 45.0) gp = 5.0;
                else if (pct >= 40.0) gp = 4.0;
                totCred += cred;
                totPts += (cred * gp);
            }
            if (totCred > 0) {
                sgpa = Math.round((totPts / totCred) * 100.0) / 100.0;
            } else if (summary.getPercentage() > 0) {
                sgpa = Math.round((summary.getPercentage() / 10.0) * 100.0) / 100.0;
            }
        }

        drawPdfText(sb, "F2", 9.5, 0.2, 0.25, 0.35, margin + 12, summaryY + 16,
                String.format(java.util.Locale.US, "Total Marks: %.1f / %.0f", summary.getTotalMarksObtained(), summary.getTotalMaxMarks()));
        drawPdfText(sb, "F2", 9.5, 0.2, 0.25, 0.35, margin + 140, summaryY + 16,
                String.format(java.util.Locale.US, "Percentage: %.2f%%", summary.getPercentage()));
        drawPdfText(sb, "F2", 9.5, 0.05, 0.58, 0.53, margin + 250, summaryY + 16,
                String.format(java.util.Locale.US, "SGPA: %.2f", sgpa));
        drawPdfText(sb, "F2", 9.5, 0.2, 0.25, 0.35, margin + 325, summaryY + 16,
                "Overall Grade: " + summary.getOverallGrade());

        // Status Badge Pill
        boolean isPass = "PASS".equalsIgnoreCase(summary.getStatus());
        sb.append("q\n");
        if (isPass) {
            sb.append("0.86 0.98 0.91 rg\n"); // #DCFCE7
        } else {
            sb.append("0.99 0.88 0.88 rg\n"); // #FEE2E2
        }
        sb.append(String.format(java.util.Locale.US, "%.1f %.1f %.1f %.1f re f\n", margin + contentWidth - 100, summaryY + 8, 92.0, 26.0));
        sb.append("Q\n");

        if (isPass) {
            drawPdfText(sb, "F2", 9.5, 0.08, 0.64, 0.29, margin + contentWidth - 88, summaryY + 17, "STATUS: PASS");
        } else {
            drawPdfText(sb, "F2", 9.5, 0.86, 0.15, 0.15, margin + contentWidth - 88, summaryY + 17, "STATUS: FAIL");
        }

        // 5. Official Verification & Signature Section
        double sigY = summaryY - 55;
        sb.append("q\n");
        sb.append("0.7 0.75 0.8 RG\n");
        sb.append("0.75 w\n");
        sb.append(String.format(java.util.Locale.US, "%.1f %.1f m %.1f %.1f l s\n", margin + 20, sigY, margin + 180, sigY));
        sb.append(String.format(java.util.Locale.US, "%.1f %.1f m %.1f %.1f l s\n", margin + contentWidth - 180, sigY, margin + contentWidth - 20, sigY));
        sb.append("Q\n");

        drawPdfText(sb, "F1", 9, 0.4, 0.45, 0.55, margin + 45, sigY - 14, "Controller of Examinations");
        drawPdfText(sb, "F1", 9, 0.4, 0.45, 0.55, margin + contentWidth - 160, sigY - 14, "Dean / Academic Director");

        drawPdfText(sb, "F1", 8, 0.6, 0.65, 0.75, margin + (contentWidth / 2) - 130, 25,
                "This document is an electronically verified official marksheet transcript.");

        byte[] streamBytes = sb.toString().getBytes(StandardCharsets.ISO_8859_1);

        // Assemble PDF Objects
        List<byte[]> objects = new ArrayList<>();
        objects.add("<< /Type /Catalog /Pages 2 0 R >>".getBytes(StandardCharsets.ISO_8859_1));
        objects.add("<< /Type /Pages /Kids [3 0 R] /Count 1 >>".getBytes(StandardCharsets.ISO_8859_1));
        objects.add(String.format(java.util.Locale.US,
                "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 %.0f %.0f] /Resources << /Font << /F1 4 0 R /F2 5 0 R >> >> /Contents 6 0 R >>",
                pageWidth, pageHeight).getBytes(StandardCharsets.ISO_8859_1));
        objects.add("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>".getBytes(StandardCharsets.ISO_8859_1));
        objects.add("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>".getBytes(StandardCharsets.ISO_8859_1));

        ByteArrayOutputStream contentObj = new ByteArrayOutputStream();
        contentObj.write(String.format(java.util.Locale.US, "<< /Length %d >>\nstream\n", streamBytes.length).getBytes(StandardCharsets.ISO_8859_1));
        contentObj.write(streamBytes);
        contentObj.write("\nendstream".getBytes(StandardCharsets.ISO_8859_1));
        objects.add(contentObj.toByteArray());

        // Build entire PDF file
        ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();
        pdfOut.write("%PDF-1.4\n".getBytes(StandardCharsets.ISO_8859_1));

        List<Integer> offsets = new ArrayList<>();
        for (int i = 0; i < objects.size(); i++) {
            offsets.add(pdfOut.size());
            pdfOut.write(String.format(java.util.Locale.US, "%d 0 obj\n", i + 1).getBytes(StandardCharsets.ISO_8859_1));
            pdfOut.write(objects.get(i));
            pdfOut.write("\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));
        }

        int xrefOffset = pdfOut.size();
        pdfOut.write(String.format(java.util.Locale.US, "xref\n0 %d\n", objects.size() + 1).getBytes(StandardCharsets.ISO_8859_1));
        pdfOut.write("0000000000 65535 f \n".getBytes(StandardCharsets.ISO_8859_1));
        for (int offset : offsets) {
            pdfOut.write(String.format(java.util.Locale.US, "%010d 00000 n \n", offset).getBytes(StandardCharsets.ISO_8859_1));
        }

        pdfOut.write(String.format(java.util.Locale.US, "trailer\n<< /Size %d /Root 1 0 R >>\n", objects.size() + 1).getBytes(StandardCharsets.ISO_8859_1));
        pdfOut.write(String.format(java.util.Locale.US, "startxref\n%d\n%%%%EOF\n", xrefOffset).getBytes(StandardCharsets.ISO_8859_1));

        return pdfOut.toByteArray();
    }

    private static void drawPdfText(StringBuilder sb, String font, double size, double r, double g, double b, double x, double y, String text) {
        sb.append("BT\n");
        sb.append(String.format(java.util.Locale.US, "/%s %.1f Tf\n", font, size));
        sb.append(String.format(java.util.Locale.US, "%.3f %.3f %.3f rg\n", r, g, b));
        sb.append(String.format(java.util.Locale.US, "%.1f %.1f Td\n", x, y));
        sb.append(String.format(java.util.Locale.US, "(%s) Tj\n", escapePdfString(text)));
        sb.append("ET\n");
    }

    private static String escapePdfString(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)");
    }

    /**
     * Generates a modern, executive, print-ready HTML marksheet.
     */
    public static String generateHtml(StudentResultSummary summary) {
        if (summary == null) {
            return "<html><body><p>No marksheet available.</p></body></html>";
        }

        Student s = summary.getStudent();
        String studentName = s != null ? s.getFullName() : "N/A";
        String rollNumber = s != null ? s.getRollNumber() : "N/A";
        String course = s != null ? s.getCourse() : "N/A";
        int semester = s != null ? s.getSemester() : 1;
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
        boolean isPass = "PASS".equalsIgnoreCase(summary.getStatus());

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n");
        html.append("<meta charset=\"UTF-8\">\n");
        html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("<title>Official Marksheet - ").append(escapeHtml(studentName)).append(" (").append(escapeHtml(rollNumber)).append(")</title>\n");
        html.append("<style>\n");
        html.append("  * { box-sizing: border-box; margin: 0; padding: 0; }\n");
        html.append("  body { font-family: 'Segoe UI', -apple-system, BlinkMacSystemFont, Roboto, sans-serif; background: #f8fafc; color: #0f172a; padding: 30px; line-height: 1.5; }\n");
        html.append("  .container { max-width: 860px; margin: 0 auto; background: #ffffff; border-radius: 12px; border: 1px solid #e2e8f0; overflow: hidden; box-shadow: 0 4px 16px rgba(15,23,42,0.06); }\n");
        html.append("  .header { background: #0c1726; color: #ffffff; padding: 28px 32px; border-bottom: 4px solid #0d9488; }\n");
        html.append("  .header h1 { font-size: 22px; font-weight: 700; letter-spacing: 0.5px; }\n");
        html.append("  .header p { color: #94a3b8; font-size: 13px; margin-top: 4px; }\n");
        html.append("  .body-content { padding: 32px; }\n");
        html.append("  .info-box { background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 18px 24px; margin-bottom: 28px; display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }\n");
        html.append("  .info-row { display: flex; font-size: 13px; }\n");
        html.append("  .info-label { font-weight: 600; color: #64748b; width: 140px; }\n");
        html.append("  .info-value { font-weight: 600; color: #0f172a; }\n");
        html.append("  .metrics-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 28px; }\n");
        html.append("  .metric-card { background: #ffffff; border: 1px solid #e2e8f0; border-radius: 8px; padding: 16px; text-align: left; }\n");
        html.append("  .metric-title { font-size: 11px; font-weight: 700; color: #64748b; text-transform: uppercase; letter-spacing: 0.5px; }\n");
        html.append("  .metric-value { font-size: 22px; font-weight: 700; color: #0f172a; margin: 6px 0 2px 0; }\n");
        html.append("  .metric-sub { font-size: 11px; color: #94a3b8; }\n");
        html.append("  table { width: 100%; border-collapse: collapse; margin-bottom: 24px; font-size: 13px; }\n");
        html.append("  thead th { background: #0f172a; color: #ffffff; font-weight: 600; padding: 10px 14px; text-align: left; }\n");
        html.append("  thead th.center { text-align: center; }\n");
        html.append("  tbody td { padding: 10px 14px; border-bottom: 1px solid #e2e8f0; }\n");
        html.append("  tbody tr:nth-child(even) { background: #f8fafc; }\n");
        html.append("  tbody td.center { text-align: center; }\n");
        html.append("  .grade-pill { display: inline-block; padding: 2px 10px; border-radius: 6px; font-weight: 700; font-size: 11px; background: #dcfce7; color: #16a34a; }\n");
        html.append("  .grade-pill.navy { background: #0f2438; color: #ffffff; }\n");
        html.append("  .grade-pill.blue { background: #dbeafe; color: #2563eb; }\n");
        html.append("  .status-pill { display: inline-block; padding: 3px 12px; border-radius: 6px; font-weight: 700; font-size: 11px; }\n");
        html.append("  .status-pass { background: #dcfce7; color: #16a34a; }\n");
        html.append("  .status-fail { background: #fee2e2; color: #dc2626; }\n");
        html.append("  .summary-ribbon { background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 14px 20px; display: flex; justify-content: space-between; align-items: center; margin-bottom: 40px; font-size: 13px; font-weight: 600; }\n");
        html.append("  .signatures { display: flex; justify-content: space-between; margin-top: 50px; padding: 0 40px; }\n");
        html.append("  .signature-box { text-align: center; border-top: 1px solid #cbd5e1; padding-top: 8px; width: 220px; font-size: 12px; color: #64748b; }\n");
        html.append("  .footer-note { text-align: center; font-size: 11px; color: #94a3b8; margin-top: 35px; }\n");
        html.append("  .action-bar { max-width: 860px; margin: 0 auto 16px auto; display: flex; justify-content: flex-end; gap: 10px; }\n");
        html.append("  .btn { display: inline-flex; align-items: center; gap: 6px; padding: 8px 18px; border-radius: 6px; font-size: 13px; font-weight: 600; cursor: pointer; border: none; }\n");
        html.append("  .btn-print { background: #0d9488; color: white; }\n");
        html.append("  .btn-print:hover { background: #0f766e; }\n");
        html.append("  @media print {\n");
        html.append("    body { background: white; padding: 0; }\n");
        html.append("    .container { box-shadow: none; border: none; max-width: 100%; }\n");
        html.append("    .action-bar { display: none; }\n");
        html.append("  }\n");
        html.append("</style>\n</head>\n<body>\n");

        // Action Toolbar
        html.append("<div class=\"action-bar\">\n");
        html.append("  <button class=\"btn btn-print\" onclick=\"window.print()\">Print / Save as PDF</button>\n");
        html.append("</div>\n");

        html.append("<div class=\"container\">\n");
        html.append("  <div class=\"header\">\n");
        html.append("    <h1>STUDENT RESULT MANAGEMENT SYSTEM</h1>\n");
        html.append("    <p>Official Academic Performance Transcript & Statement of Marks</p>\n");
        html.append("  </div>\n");

        html.append("  <div class=\"body-content\">\n");

        // Student Info
        html.append("    <div class=\"info-box\">\n");
        html.append("      <div class=\"info-row\"><span class=\"info-label\">Student Name:</span><span class=\"info-value\">").append(escapeHtml(studentName)).append("</span></div>\n");
        html.append("      <div class=\"info-row\"><span class=\"info-label\">Course / Program:</span><span class=\"info-value\">").append(escapeHtml(course)).append("</span></div>\n");
        html.append("      <div class=\"info-row\"><span class=\"info-label\">Roll Number:</span><span class=\"info-value\">").append(escapeHtml(rollNumber)).append("</span></div>\n");
        html.append("      <div class=\"info-row\"><span class=\"info-label\">Semester:</span><span class=\"info-value\">Semester ").append(semester).append("</span></div>\n");
        html.append("      <div class=\"info-row\"><span class=\"info-label\">Academic Year:</span><span class=\"info-value\">2026 - 2027</span></div>\n");
        html.append("      <div class=\"info-row\"><span class=\"info-label\">Date of Issue:</span><span class=\"info-value\">").append(escapeHtml(dateStr)).append("</span></div>\n");
        html.append("    </div>\n");

        // Top 4 Metrics Cards
        List<Result> results = summary.getResults() != null ? summary.getResults() : new ArrayList<>();
        html.append("    <div class=\"metrics-grid\">\n");
        html.append("      <div class=\"metric-card\"><div class=\"metric-title\">Total Subjects</div><div class=\"metric-value\">").append(results.size()).append("</div><div class=\"metric-sub\">").append(summary.getTotalCredits()).append(" Credits</div></div>\n");
        html.append("      <div class=\"metric-card\"><div class=\"metric-title\">Total Marks</div><div class=\"metric-value\">").append(String.format(java.util.Locale.US, "%.1f", summary.getTotalMarksObtained())).append(" / ").append(String.format(java.util.Locale.US, "%.0f", summary.getTotalMaxMarks())).append("</div><div class=\"metric-sub\">Obtained / Max</div></div>\n");
        html.append("      <div class=\"metric-card\"><div class=\"metric-title\">Semester SGPA</div><div class=\"metric-value\">").append(String.format(java.util.Locale.US, "%.2f", summary.getSgpa() > 0 ? summary.getSgpa() : (summary.getPercentage() / 10.0))).append("</div><div class=\"metric-sub\">").append(String.format(java.util.Locale.US, "%.1f%% Score", summary.getPercentage())).append("</div></div>\n");
        html.append("      <div class=\"metric-card\"><div class=\"metric-title\">Overall Status</div><div class=\"metric-value\" style=\"color: ").append(isPass ? "#16a34a" : "#dc2626").append(";\">").append(isPass ? "PASS (" + summary.getOverallGrade() + ")" : "FAIL").append("</div><div class=\"metric-sub\">").append(escapeHtml(summary.getDivision() != null ? summary.getDivision() : "Standing")).append("</div></div>\n");
        html.append("    </div>\n");

        // Table
        html.append("    <table>\n");
        html.append("      <thead><tr>\n");
        html.append("        <th>Subject Code</th>\n");
        html.append("        <th>Subject Name</th>\n");
        html.append("        <th class=\"center\">Credits</th>\n");
        html.append("        <th class=\"center\">Max Marks</th>\n");
        html.append("        <th class=\"center\">Pass Marks</th>\n");
        html.append("        <th class=\"center\">Marks Obtained</th>\n");
        html.append("        <th class=\"center\">Grade</th>\n");
        html.append("        <th class=\"center\">Status</th>\n");
        html.append("      </tr></thead>\n");
        html.append("      <tbody>\n");

        for (Result r : results) {
            Subject sub = r.getSubject();
            String code = sub != null ? sub.getSubjectCode() : "N/A";
            String name = sub != null ? sub.getSubjectName() : "N/A";
            int credits = sub != null ? sub.getCredits() : 4;
            int max = sub != null ? sub.getMaxMarks() : 100;
            int pass = sub != null ? sub.getPassMarks() : 35;
            double obtained = r.getMarksObtained();
            String grade = r.getGrade() != null ? r.getGrade() : "-";
            boolean passed = obtained >= pass;

            String gradeClass = "grade-pill";
            if ("B".equalsIgnoreCase(grade)) gradeClass = "grade-pill navy";
            else if (grade.startsWith("B")) gradeClass = "grade-pill blue";

            html.append("      <tr>\n");
            html.append("        <td><strong>").append(escapeHtml(code)).append("</strong></td>\n");
            html.append("        <td>").append(escapeHtml(name)).append("</td>\n");
            html.append("        <td class=\"center\">").append(credits).append("</td>\n");
            html.append("        <td class=\"center\">").append(max).append("</td>\n");
            html.append("        <td class=\"center\">").append(pass).append("</td>\n");
            html.append("        <td class=\"center\"><strong>").append(String.format(java.util.Locale.US, "%.1f", obtained)).append("</strong></td>\n");
            html.append("        <td class=\"center\"><span class=\"").append(gradeClass).append("\">").append(escapeHtml(grade)).append("</span></td>\n");
            html.append("        <td class=\"center\"><span class=\"status-pill ").append(passed ? "status-pass" : "status-fail").append("\">").append(passed ? "PASS" : "FAIL").append("</span></td>\n");
            html.append("      </tr>\n");
        }

        html.append("      </tbody>\n");
        html.append("    </table>\n");

        // Summary Ribbon
        html.append("    <div class=\"summary-ribbon\">\n");
        html.append("      <div>Total Marks: ").append(String.format(java.util.Locale.US, "%.1f / %.0f", summary.getTotalMarksObtained(), summary.getTotalMaxMarks())).append("</div>\n");
        html.append("      <div style=\"color: #0d9488;\">SGPA: ").append(String.format(java.util.Locale.US, "%.2f", summary.getSgpa())).append(" (").append(String.format(java.util.Locale.US, "%.2f%%", summary.getPercentage())).append(")</div>\n");
        html.append("      <div>Division: ").append(escapeHtml(summary.getDivision() != null ? summary.getDivision() : summary.getOverallGrade())).append("</div>\n");
        html.append("      <div><span class=\"status-pill ").append(isPass ? "status-pass" : "status-fail").append("\">STATUS: ").append(isPass ? "PASS" : "FAIL").append("</span></div>\n");
        html.append("    </div>\n");

        // Signatures
        html.append("    <div class=\"signatures\">\n");
        html.append("      <div class=\"signature-box\">Prepared by (Tabulator)</div>\n");
        html.append("      <div class=\"signature-box\">Verified by (Head of Dept)</div>\n");
        html.append("      <div class=\"signature-box\">Controller of Examinations (CoE)</div>\n");
        html.append("    </div>\n");

        html.append("    <div class=\"footer-note\">This document is an electronically generated and certified academic transcript.</div>\n");
        html.append("  </div>\n");
        html.append("</div>\n");
        html.append("</body>\n</html>\n");

        return html.toString();
    }

    private static String escapeHtml(String str) {
        if (str == null) return "";
        return str.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
