package result.ui;

import result.dao.ResultDAO;
import result.dao.StudentDAO;
import result.dao.SubjectDAO;
import result.dao.UserDAO;
import result.model.Result;
import result.model.Student;
import result.model.Subject;
import result.model.User;
import result.service.AuthService;
import result.service.ResultService;
import result.service.StudentService;
import result.service.SubjectService;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RenderStudentPanel {
    public static void main(String[] args) throws Exception {
        System.out.println("Rendering StudentManagementPanel preview...");

        StudentDAO mockStudentDAO = new StudentDAO() {
            private final List<Student> list = new ArrayList<>();
            {
                list.add(new Student(1, "2510001320", "Mohit", "Priyadarshi", "mohit.priyadarshi@example.com", "B.Tech Computer Science", 4, "Male", LocalDate.now().minusYears(19)));
                list.add(new Student(2, "2510001331", "Moulik", "Tiwari", "moulik.tiwari@example.com", "BCA", 2, "Male", LocalDate.now().minusYears(19)));
                list.add(new Student(3, "2510001319", "Adarsh", "Kumar", "adarsh.kumar@example.com", "B.Tech IT", 4, "Male", LocalDate.now().minusYears(20)));
                list.add(new Student(4, "2510001212", "Akshay", "Bharadwaj", "akshay.bharadwaj@example.com", "MCA", 2, "Male", LocalDate.now().minusYears(19)));
            }

            @Override public boolean insert(Student student) { return true; }
            @Override public boolean update(Student student) { return true; }
            @Override public boolean delete(int studentId) { return true; }
            @Override public Optional<Student> findById(int studentId) { return Optional.empty(); }
            @Override public Optional<Student> findByRollNumber(String rollNumber) { return Optional.empty(); }
            @Override public List<Student> findAll() { return new ArrayList<>(list); }
            @Override public List<Student> searchByKeyword(String keyword) { return new ArrayList<>(list); }
        };

        SubjectDAO mockSubjectDAO = new SubjectDAO() {
            @Override public boolean insert(Subject subject) { return true; }
            @Override public boolean update(Subject subject) { return true; }
            @Override public boolean delete(int subjectId) { return true; }
            @Override public Optional<Subject> findById(int subjectId) { return Optional.empty(); }
            @Override public Optional<Subject> findByCode(String subjectCode) { return Optional.empty(); }
            @Override public List<Subject> findAll() { return new ArrayList<>(); }
        };

        StudentService studentService = new StudentService(mockStudentDAO);
        SubjectService subjectService = new SubjectService(mockSubjectDAO);

        ResultService resultService = new ResultService(new ResultDAO() {
            @Override public boolean insert(Result result) { return true; }
            @Override public boolean update(Result result) { return true; }
            @Override public boolean delete(int resultId) { return true; }
            @Override public boolean deleteByStudentId(int studentId) { return true; }
            @Override public Optional<Result> findById(int resultId) { return Optional.empty(); }
            @Override public Optional<Result> findByStudentAndSubject(int studentId, int subjectId) { return Optional.empty(); }
            @Override public List<Result> findByStudentId(int studentId) { return new ArrayList<>(); }
            @Override public List<Result> findAll() { return new ArrayList<>(); }
        }, mockStudentDAO, mockSubjectDAO);

        AuthService authService = new AuthService(new UserDAO() {
            @Override public Optional<User> findByUsername(String username) {
                return Optional.of(new User(1, "admin", "admin123", "Administrator", "ADMIN"));
            }
            @Override public boolean insert(User user) { return true; }
            @Override public boolean verifyCredentials(String username, String password) { return true; }
        });
        authService.login("admin", "admin123");

        SwingUtilities.invokeAndWait(() -> {
            try {
                // 1. Render StudentManagementPanel
                int width = 1180;
                int height = 750;

                StudentManagementPanel panel = new StudentManagementPanel(studentService, null);
                panel.setSize(width, height);
                panel.doLayout();

                JFrame frame = new JFrame();
                frame.setUndecorated(true);
                frame.setContentPane(panel);
                frame.setSize(width, height);
                frame.addNotify();
                frame.validate();

                BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = image.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                panel.paint(g2);
                g2.dispose();

                File out = new File("C:/Users/Mohit Priyadarshi/.gemini/antigravity/brain/2770d727-00ec-42d3-8eba-1c42f4786dec/verified_student_management_panel.png");
                ImageIO.write(image, "png", out);
                System.out.println("Saved StudentManagementPanel to: " + out.getAbsolutePath());

                // 2. Render StudentFormDialog Preview (Add New Student Record)
                StudentFormDialog addDialog = new StudentFormDialog(frame, null, null);
                addDialog.pack();
                int adw = addDialog.getWidth();
                int adh = addDialog.getHeight();
                addDialog.getContentPane().setSize(adw, adh);
                addDialog.getContentPane().doLayout();
                addDialog.getContentPane().validate();
                BufferedImage addImg = new BufferedImage(adw, adh, BufferedImage.TYPE_INT_RGB);
                Graphics2D addG = addImg.createGraphics();
                addG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                addG.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                addDialog.getContentPane().paint(addG);
                addG.dispose();
                File addOut = new File("C:/Users/Mohit Priyadarshi/.gemini/antigravity/brain/2770d727-00ec-42d3-8eba-1c42f4786dec/verified_add_student_dialog.png");
                ImageIO.write(addImg, "png", addOut);
                System.out.println("Saved Add StudentFormDialog to: " + addOut.getAbsolutePath());
                addDialog.dispose();

                // 2b. Render StudentFormDialog Preview (Update Student Record)
                Student student = studentService.getAllStudents().get(0);
                StudentFormDialog dialog = new StudentFormDialog(frame, student, null);
                dialog.pack();
                int dw = dialog.getWidth();
                int dh = dialog.getHeight();
                dialog.getContentPane().setSize(dw, dh);
                dialog.getContentPane().doLayout();
                dialog.getContentPane().validate();
                BufferedImage dImg = new BufferedImage(dw, dh, BufferedImage.TYPE_INT_RGB);
                Graphics2D dg = dImg.createGraphics();
                dg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                dg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                dialog.getContentPane().paint(dg);
                dg.dispose();
                File dOut = new File("C:/Users/Mohit Priyadarshi/.gemini/antigravity/brain/2770d727-00ec-42d3-8eba-1c42f4786dec/verified_student_dialog.png");
                ImageIO.write(dImg, "png", dOut);
                System.out.println("Saved StudentFormDialog to: " + dOut.getAbsolutePath());
                dialog.dispose();

                frame.dispose();

                // 3. Render DashboardFrame with StudentManagement tab
                DashboardFrame dashFrame = new DashboardFrame(authService, studentService, subjectService, resultService);
                dashFrame.setUndecorated(true);
                dashFrame.setSize(1240, 820);
                dashFrame.getContentPane().setSize(1240, 820);
                dashFrame.getContentPane().doLayout();
                dashFrame.addNotify();
                dashFrame.validate();

                BufferedImage dashImg = new BufferedImage(1240, 820, BufferedImage.TYPE_INT_RGB);
                Graphics2D dashG = dashImg.createGraphics();
                dashG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                dashG.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                dashFrame.getContentPane().paint(dashG);
                dashG.dispose();
                File dashOut = new File("C:/Users/Mohit Priyadarshi/.gemini/antigravity/brain/2770d727-00ec-42d3-8eba-1c42f4786dec/verified_dashboard_with_panel.png");
                ImageIO.write(dashImg, "png", dashOut);
                System.out.println("Saved DashboardFrame to: " + dashOut.getAbsolutePath());

                // 4. Render SubjectManagementPanel standalone
                SubjectManagementPanel subjectPanel = new SubjectManagementPanel(subjectService, dashFrame);
                subjectPanel.setSize(width, height);
                subjectPanel.doLayout();
                JFrame subFrame = new JFrame();
                subFrame.setUndecorated(true);
                subFrame.setContentPane(subjectPanel);
                subFrame.setSize(width, height);
                subFrame.addNotify();
                subFrame.validate();

                BufferedImage subImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                Graphics2D subG = subImg.createGraphics();
                subG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                subG.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                subjectPanel.paint(subG);
                subG.dispose();

                File subOut = new File("C:/Users/Mohit Priyadarshi/.gemini/antigravity/brain/2770d727-00ec-42d3-8eba-1c42f4786dec/verified_subject_management_panel.png");
                ImageIO.write(subImg, "png", subOut);
                System.out.println("Saved SubjectManagementPanel to: " + subOut.getAbsolutePath());
                subFrame.dispose();

                // 5. Render SubjectFormDialog
                SubjectFormDialog subDialog = new SubjectFormDialog(dashFrame, null, dashFrame);
                subDialog.pack();
                int sdw = subDialog.getWidth();
                int sdh = subDialog.getHeight();
                subDialog.getContentPane().setSize(sdw, sdh);
                subDialog.getContentPane().doLayout();
                subDialog.getContentPane().validate();
                BufferedImage sdImg = new BufferedImage(sdw, sdh, BufferedImage.TYPE_INT_RGB);
                Graphics2D sdG = sdImg.createGraphics();
                sdG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                sdG.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                subDialog.getContentPane().paint(sdG);
                sdG.dispose();
                File sdOut = new File("C:/Users/Mohit Priyadarshi/.gemini/antigravity/brain/2770d727-00ec-42d3-8eba-1c42f4786dec/verified_subject_dialog.png");
                ImageIO.write(sdImg, "png", sdOut);
                System.out.println("Saved SubjectFormDialog to: " + sdOut.getAbsolutePath());
                subDialog.dispose();

                // 6. Switch to Subject Management tab on DashboardFrame and render
                dashFrame.getTabbedPane().setSelectedIndex(1);
                dashFrame.validate();
                BufferedImage dashSubImg = new BufferedImage(1240, 820, BufferedImage.TYPE_INT_RGB);
                Graphics2D dashSubG = dashSubImg.createGraphics();
                dashSubG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                dashSubG.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                dashFrame.getContentPane().paint(dashSubG);
                dashSubG.dispose();
                File dashSubOut = new File("C:/Users/Mohit Priyadarshi/.gemini/antigravity/brain/2770d727-00ec-42d3-8eba-1c42f4786dec/verified_dashboard_subject_tab.png");
                ImageIO.write(dashSubImg, "png", dashSubOut);
                System.out.println("Saved Dashboard Subject Tab to: " + dashSubOut.getAbsolutePath());

                // 7. Switch to Result Entry tab on DashboardFrame and render
                dashFrame.getTabbedPane().setSelectedIndex(2);
                dashFrame.validate();
                BufferedImage dashResultImg = new BufferedImage(1240, 820, BufferedImage.TYPE_INT_RGB);
                Graphics2D dashResultG = dashResultImg.createGraphics();
                dashResultG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                dashResultG.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                dashFrame.getContentPane().paint(dashResultG);
                dashResultG.dispose();
                File dashResultOut = new File("C:/Users/Mohit Priyadarshi/.gemini/antigravity/brain/2770d727-00ec-42d3-8eba-1c42f4786dec/verified_dashboard_result_entry_tab.png");
                ImageIO.write(dashResultImg, "png", dashResultOut);
                System.out.println("Saved Dashboard Result Entry Tab to: " + dashResultOut.getAbsolutePath());

                // 8. Switch to View Results & Marksheets tab on DashboardFrame and render
                dashFrame.getTabbedPane().setSelectedIndex(3);
                dashFrame.validate();
                BufferedImage dashViewImg = new BufferedImage(1240, 820, BufferedImage.TYPE_INT_RGB);
                Graphics2D dashViewG = dashViewImg.createGraphics();
                dashViewG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                dashViewG.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                dashFrame.getContentPane().paint(dashViewG);
                dashViewG.dispose();
                File dashViewOut = new File("C:/Users/Mohit Priyadarshi/.gemini/antigravity/brain/2770d727-00ec-42d3-8eba-1c42f4786dec/verified_dashboard_result_view_tab.png");
                ImageIO.write(dashViewImg, "png", dashViewOut);
                System.out.println("Saved Dashboard View Results Tab to: " + dashViewOut.getAbsolutePath());

                dashFrame.dispose();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
