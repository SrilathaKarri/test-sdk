package carestack.base.errors;

import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import carestack.base.utils.LogUtil;

/**
 * Base class for handling errors in the EHR (Electronic Health Records) API.
 * It encapsulates information such as error type, status code, and message.
 * This class provides methods for logging, handling API errors, and mapping HTTP statuses to error types.
 */
@Getter
@ToString
public class EhrApiError extends RuntimeException {
    private final String statusMessage;
    private final int statusCode;
    private final ErrorType errorType;

    /**
     * Constructs a new {@link EhrApiError} with the specified error message and error type.
     *
     * @param message The error message.
     * @param errorType The error type, which determines the HTTP status code.
     */
    public EhrApiError(String message, ErrorType errorType) {
        super(message);
        this.statusMessage = errorType.getStatusMessage();
        this.statusCode = errorType.getStatusCode();
        this.errorType = errorType;
    }

    public EhrApiError(String message, String statusMessage, int statusCode, ErrorType errorType) {
        super(message);
        this.statusMessage = statusMessage;
        this.statusCode = statusCode;
        this.errorType = errorType;
    }

    /**
     * Constructs a new {@link EhrApiError} with the specified error message and HTTP status code.
     *
     * @param message The error message.
     * @param httpStatusCode The HTTP status code associated with the error.
     */
    public EhrApiError(String message, HttpStatusCode httpStatusCode) {
        super(message);
        HttpStatus httpStatus = HttpStatus.valueOf(httpStatusCode.value()); // Ensure compatibility
        this.statusMessage = httpStatus.getReasonPhrase();
        this.statusCode = httpStatus.value();
        this.errorType = mapHttpStatusToErrorType(httpStatus);
    }


    @Override
    public String toString() {
        return "EhrApiError(message=" + getMessage() +
                ", statusMessage=" + statusMessage +
                ", statusCode=" + statusCode +
                ", errorType=" + errorType + ")";
    }


    /**
     * Handles and logs the given API error, returning an appropriate {@link EhrApiError}.
     * This method can handle {@link EhrApiError} and {@link WebClientResponseException}.
     *
     * @param error The error to handle.
     * @return A new instance of {@link EhrApiError} or a wrapped {@link WebClientResponseException}.
     */
    public static RuntimeException handleAndLogApiError(Throwable error) {
        if (error instanceof EhrApiError ehrApiError) {
            LogUtil.logger.error(String.format("EhrApiError occurred: %s", error.getMessage()));
            return ehrApiError;
        } else if (error instanceof WebClientResponseException e) {
            LogUtil.logger.error(String.format("WebClientResponseException: Status - %s, Body - %s",
                    e.getStatusCode(), e.getResponseBodyAsString()));

            return new EhrApiError(e.getResponseBodyAsString(),
                    e.getStatusText(),
                    e.getRawStatusCode(), mapHttpStatusToErrorType(e.getStatusCode()));
        } else {
            LogUtil.logger.error(String.format("Unexpected Error: %s", error.getMessage()));
            return new EhrApiError(String.format("Unexpected Error: %s", error.getMessage()), ErrorType.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Maps an HTTP status code to the appropriate {@link ErrorType}.
     *
     * @param statusCode The HTTP status code.
     * @return The corresponding {@link ErrorType}.
     */
    public static ErrorType mapHttpStatusToErrorType(HttpStatusCode statusCode) {
        HttpStatus status = HttpStatus.resolve(statusCode.value());
        if (status == null) {
            return ErrorType.INTERNAL_SERVER_ERROR;
        }
        return switch (status) {
            case BAD_REQUEST -> ErrorType.VALIDATION;
            case UNAUTHORIZED -> ErrorType.AUTHENTICATION;
            case FORBIDDEN -> ErrorType.AUTHORIZATION;
            case NOT_FOUND -> ErrorType.NOT_FOUND;
            case CONFLICT -> ErrorType.CONFLICT;
            default -> ErrorType.INTERNAL_SERVER_ERROR;
        };
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this; // suppress stack trace
    }
}
