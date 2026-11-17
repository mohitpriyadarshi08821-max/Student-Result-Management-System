package result.exception;

/**
 * Exception thrown when a requested record (e.g. Student, Subject, or Result)
 * cannot be located in the database.
 */
public class RecordNotFoundException extends Exception {

    private static final long serialVersionUID = 1L;

    public RecordNotFoundException(String message) {
        super(message);
    }

    public RecordNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

