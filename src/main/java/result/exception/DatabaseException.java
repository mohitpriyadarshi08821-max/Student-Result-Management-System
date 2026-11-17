package result.exception;

/**
 * Custom runtime exception thrown when database operations encounter errors.
 * Wraps SQLExceptions to decouple the UI and Service layers from low-level JDBC details.
 */
public class DatabaseException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}

