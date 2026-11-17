package result.ui;

import result.dao.StudentDAO;
import result.model.Student;
import result.service.StudentService;

import javax.swing.*;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SearchFieldBehaviorTest {

    public static void main(String[] args) throws Exception {
        System.out.println("Starting SearchFieldBehaviorTest...");

        StudentDAO mockStudentDAO = new StudentDAO() {
            private final List<Student> list = new ArrayList<>();
            {
                list.add(new Student(1, "2510001320", "Mohit", "Priyadarshi", "mohit@example.com", "B.Tech CS", 4, "Male", LocalDate.now().minusYears(19)));
                list.add(new Student(2, "2510001331", "Moulik", "Tiwari", "moulik@example.com", "BCA", 2, "Male", LocalDate.now().minusYears(19)));
                list.add(new Student(3, "2510001319", "Adarsh", "Kumar", "adarsh@example.com", "B.Tech IT", 4, "Male", LocalDate.now().minusYears(20)));
            }

            @Override public boolean insert(Student student) { return true; }
            @Override public boolean update(Student student) { return true; }
            @Override public boolean delete(int studentId) { return true; }
            @Override public Optional<Student> findById(int studentId) { return Optional.empty(); }
            @Override public Optional<Student> findByRollNumber(String rollNumber) { return Optional.empty(); }
            @Override public List<Student> findAll() { return new ArrayList<>(list); }
            @Override public List<Student> searchByKeyword(String keyword) {
                if (keyword == null || keyword.trim().isEmpty()) return new ArrayList<>(list);
                String k = keyword.toLowerCase().trim();
                List<Student> res = new ArrayList<>();
                for (Student s : list) {
                    if (s.getFullName().toLowerCase().contains(k) || s.getRollNumber().toLowerCase().contains(k)) {
                        res.add(s);
                    }
                }
                return res;
            }
        };

        StudentService studentService = new StudentService(mockStudentDAO);

        SwingUtilities.invokeAndWait(() -> {
            try {
                StudentManagementPanel panel = new StudentManagementPanel(studentService, null);
                StudentManagementPanel.RoundedSearchField searchField = panel.getSearchField();

                if (searchField == null) {
                    throw new RuntimeException("searchField should not be null!");
                }

                // 1. Initial State: empty text
                if (!searchField.getText().isEmpty()) {
                    throw new RuntimeException("Search field should initially be empty!");
                }

                // 2. Cursor / Focus moved to it
                searchField.dispatchEvent(new FocusEvent(searchField, FocusEvent.FOCUS_GAINED));
                // Verify text is still empty (placeholder is painted because getText().isEmpty() is true)
                if (!searchField.getText().isEmpty()) {
                    throw new RuntimeException("Search field text should still be empty after focus!");
                }

                // 3. Mouse entered (hover)
                searchField.dispatchEvent(new MouseEvent(searchField, MouseEvent.MOUSE_ENTERED, System.currentTimeMillis(), 0, 10, 10, 0, false));
                if (!searchField.getText().isEmpty()) {
                    throw new RuntimeException("Search field text should still be empty after mouse hover!");
                }

                // 4. Type search term: "Mohit"
                searchField.setText("Mohit");
                if (searchField.getText().isEmpty()) {
                    throw new RuntimeException("Search field text should not be empty after typing!");
                }

                // 5. Clear search term: ""
                searchField.setText("");
                if (!searchField.getText().isEmpty()) {
                    throw new RuntimeException("Search field text should be empty after clearing!");
                }

                System.out.println("SEARCH FIELD BEHAVIOR TEST PASSED SUCCESSFULLY!");
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        });
    }
}
