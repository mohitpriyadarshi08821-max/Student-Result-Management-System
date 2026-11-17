package result.exception;

/**
 * Exception thrown when a requested record (e.g. Student, Subject, or Result)
 * cannot be located in the database.
 */
public class RecordNotFoundException extends Exception {

    public RecordNotFoundException(String message) {
        super(message);
    }

    public RecordNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

