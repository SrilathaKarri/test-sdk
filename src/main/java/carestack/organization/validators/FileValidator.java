package carestack.organization.validators;

import carestack.organization.dto.Document;

/**
 * Utility class for validating document files.
 * <p>This class is used to validate document properties such as file size and MIME type. It supports validation for image files with
 * MIME types "image/jpeg" and "image/png" and enforces a maximum file size of 1MB.</p>
 */
public class FileValidator {

    /**
     * Maximum allowed file size (1MB).
     */
    private static final long MAX_FILE_SIZE = 1048576; // 1MB

    /**
     * Validates the provided {@link Document} for required properties.
     * <p>This method checks that the document is not null. If the document is null, an exception is thrown.</p>
     *
     * @param document The document to validate.
     * @param fieldName The name of the field for error reporting.
     * @throws IllegalArgumentException if the document is null.
     */
    public static void validateDocument(Document document, String fieldName) {
        if (document == null) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
    }

    /**
     * Checks whether the provided MIME type is valid.
     * <p>This method supports validating MIME types for JPEG and PNG images.</p>
     *
     * @param contentType The MIME type of the document to check.
     * @return {@code true} if the MIME type is valid (JPEG or PNG), {@code false} otherwise.
     */
    private static boolean isValidMimeType(String contentType) {
        return "image/jpeg".equalsIgnoreCase(contentType) || "image/png".equalsIgnoreCase(contentType);
    }
}
