package carestack.base.errors;

/**
 * Represents a validation error that occurs during API operations.
 * This error is typically used when the input provided to the API does not meet the validation criteria.
 * It extends from {@link EhrApiError} and uses the {@link ErrorType#VALIDATION} type.

 */
public class ValidationError extends EhrApiError {
    /**
     * Constructs a new {@link ValidationError} with the specified error message.
     * The error type is automatically set to {@link ErrorType#VALIDATION}.
     *
     * @param message The error message.
     */
    public ValidationError(String message) {
        super(message, ErrorType.VALIDATION);
    }

}
