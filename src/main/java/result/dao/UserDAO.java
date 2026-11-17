package result.dao;

import result.model.User;
import java.util.Optional;

/**
 * Data Access Object interface for User authentication and administrative accounts.
 */
public interface UserDAO {

    Optional<User> findByUsername(String username);

    boolean insert(User user);

    boolean verifyCredentials(String username, String password);
}

