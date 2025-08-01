package carestack.base.errors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Enum representing various types of errors in the API.
 * Each error type is associated with an HTTP status code, which is used for response generation.
 * The enum includes common HTTP status codes like:
 * - SUCCESS (200 OK)
 * - VALIDATION (400 Bad Request)
 * - AUTHENTICATION (401 Unauthorized)
 * - AUTHORIZATION (403 Forbidden)
 * - NOT_FOUND (404 Not Found)
 * - INTERNAL_SERVER_ERROR (500 Internal Server Error)

 */
@Getter
@RequiredArgsConstructor
public enum ErrorType {
    SUCCESS(HttpStatus.OK),
    NO_CONTENT(HttpStatus.NO_CONTENT),
    VALIDATION(HttpStatus.BAD_REQUEST),
    AUTHENTICATION(HttpStatus.UNAUTHORIZED),
    AUTHORIZATION(HttpStatus.FORBIDDEN),
    NOT_FOUND(HttpStatus.NOT_FOUND),
    CONFLICT(HttpStatus.CONFLICT),
    CONNECTION(HttpStatus.SERVICE_UNAVAILABLE),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus httpStatus;

    /**
     * Returns the HTTP status code associated with this error type.
     *
     * @return The HTTP status code.
     */
    public int getStatusCode() {
        return httpStatus.value();
    }

    /**
     * Returns the HTTP status message associated with this error type.
     *
     * @return The HTTP status message.
     */
    public String getStatusMessage() {
        return httpStatus.getReasonPhrase();
    }
}
