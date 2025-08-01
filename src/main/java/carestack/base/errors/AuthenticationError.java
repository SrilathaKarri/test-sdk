package carestack.base.errors;

/**
 * Represents an authentication error that occurs when the user is not properly authenticated.
 * This error is typically used when the user fails to provide valid credentials.
 * It extends from {@link EhrApiError} and uses the {@link ErrorType#AUTHENTICATION} type.

 */
public class AuthenticationError extends EhrApiError {

    /**
     * Constructs a new {@link AuthenticationError} with the default message: "Authentication failed".
     * The error type is automatically set to {@link ErrorType#AUTHENTICATION}.
     */
    public AuthenticationError() {
        super("Authentication failed", ErrorType.AUTHENTICATION);
    }

    /**
     * Constructs a new {@link AuthenticationError} with the specified error message.
     * The error type is automatically set to {@link ErrorType#AUTHENTICATION}.
     *
     * @param message The error message.
     */
    public AuthenticationError(String message) {
        super(message, ErrorType.AUTHENTICATION);
    }
}
