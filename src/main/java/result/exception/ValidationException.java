package result.exception;

/**
 * Custom exception thrown when user input violates business validation rules
 * (e.g. invalid roll number format, negative marks, empty required fields).
 */
public class ValidationException extends Exception {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

