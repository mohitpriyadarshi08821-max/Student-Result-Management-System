package result.service;

import result.dao.UserDAO;
import result.exception.ValidationException;
import result.model.User;

import java.util.Optional;

/**
 * Service class handling authentication, credential verification, and user sessions.
 */
public class AuthService {

    private final UserDAO userDAO;
    private User currentUser;

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public boolean login(String username, String password) throws ValidationException {
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Username cannot be empty.");
        }
        if (password == null || password.isEmpty()) {
            throw new ValidationException("Password cannot be empty.");
        }

        Optional<User> userOpt = userDAO.findByUsername(username.trim());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getPassword() != null && user.getPassword().equals(password)) {
                this.currentUser = user;
                return true;
            }
        }
        return false;
    }

    public void logout() {
        this.currentUser = null;
    }

    public Optional<User> getCurrentUser() {
        return Optional.ofNullable(currentUser);
    }

    public boolean isAuthenticated() {
        return currentUser != null;
    }
}
