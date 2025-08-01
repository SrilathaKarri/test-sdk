package carestack.organization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a document with metadata (used for file uploads).
 *
 * <p>Supported formats: <b>JPG, PNG</b> (Base64-encoded)</p>
 * <p>Maximum file size: <b>1MB</b></p>
 * <p>Content must be Base64-encoded</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Document {

    /**
     * Base64-encoded file content.
     */
    @NotNull(message = "File content (base64) is required")
    @NotBlank(message = "File content cannot be empty")
    private String value;

    /**
     * Original file name (must be between 3 and 100 characters).
     */
    @NotNull(message = "File name is required")
    @NotBlank(message = "File name cannot be empty")
    @Size(min = 3, max = 100, message = "File name must be between 3 and 100 characters")
    private String name;

}
