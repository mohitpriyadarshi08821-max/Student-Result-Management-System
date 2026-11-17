package result.exception;

/**
 * Exception thrown when attempting to insert a record that violates a uniqueness constraint
 * (e.g. roll number already registered, duplicate subject code, or existing username).
 */
public class DuplicateRecordException extends Exception {

    public DuplicateRecordException(String message) {
        super(message);
    }

    public DuplicateRecordException(String message, Throwable cause) {
        super(message, cause);
    }
}

