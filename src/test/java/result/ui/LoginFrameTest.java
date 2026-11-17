package result.ui;

import result.dao.UserDAO;
import result.exception.ValidationException;
import result.model.User;
import result.service.AuthService; 

import java.awt.GraphicsEnvironment;
import java.util.Optional;

/**
 * Pure Java SE verification suite for LoginFrame and AuthService.
 */
public class LoginFrameTest {

    static class MockUserDAO implements UserDAO {
        private final User user = new User(1, "admin", "admin123", "Administrator", "ADMIN");

        @Override
        public Optional<User> findByUsername(String username) {
            if ("admin".equals(username)) {
                return Optional.of(user);
            }
            return Optional.empty();
        }

        @Override
        public boolean insert(User user) {
            return true;
        }

        @Override
        public boolean verifyCredentials(String username, String password) {
            return "admin".equals(username) && "admin123".equals(password);
        }
    }

    public static void main(String[] args) throws Exception {
        MockUserDAO mockUserDAO = new MockUserDAO();
        AuthService authService = new AuthService(mockUserDAO);

        testAuthSuccess(authService);
        testAuthFailure(authService);
        testValidationEmptyFields(authService);

        if (!GraphicsEnvironment.isHeadless()) {
            testLoginFrameComponents(authService);
            System.out.println("GUI components verified in display environment.");
        } else {
            System.out.println("Headless environment detected: AuthService & validation logic verified.");
        }

        System.out.println("ALL LOGIN & AUTH TESTS PASSED SUCCESSFULLY!");
    }

    private static void testAuthSuccess(AuthService authService) throws ValidationException {
        boolean result = authService.login("admin", "admin123");
        if (!result || !authService.isAuthenticated()) {
            throw new AssertionError("Authentication failed for valid credentials!");
        }
        if (!"admin".equals(authService.getCurrentUser().get().getUsername())) {
            throw new AssertionError("Current user username does not match!");
        }
        authService.logout();
    }

    private static void testAuthFailure(AuthService authService) throws ValidationException {
        boolean result = authService.login("admin", "wrong_pass");
        if (result || authService.isAuthenticated()) {
            throw new AssertionError("Authentication succeeded for invalid credentials!");
        }
    }

    private static void testValidationEmptyFields(AuthService authService) {
        try {
            authService.login("", "pass");
            throw new AssertionError("Expected ValidationException for blank username");
        } catch (ValidationException expected) {
            // Success
        }

        try {
            authService.login("admin", "");
            throw new AssertionError("Expected ValidationException for blank password");
        } catch (ValidationException expected) {
            // Success
        }
    }

    private static void testLoginFrameComponents(AuthService authService) {
        LoginFrame frame = new LoginFrame(authService);
        if (frame.getUsernameField() == null) {
            throw new AssertionError("Username field was not initialized");
        }
        if (frame.getPasswordField() == null) {
            throw new AssertionError("Password field was not initialized");
        }
        if (frame.getLoginButton() == null) {
            throw new AssertionError("Login button was not initialized");
        }
        if (frame.getExitButton() == null) {
            throw new AssertionError("Exit button was not initialized");
        }
        if (frame.getStatusLabel() == null) {
            throw new AssertionError("Status label was not initialized");
        }
        if (frame.getAuthService() != authService) {
            throw new AssertionError("AuthService was not properly wired");
        }
        if (!frame.isResizable()) {
            throw new AssertionError("LoginFrame should be resizable to allow maximize button to function");
        }
    }
}
