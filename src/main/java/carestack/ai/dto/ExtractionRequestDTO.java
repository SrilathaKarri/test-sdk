package carestack.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * Data Transfer Object for requesting data extraction from unstructured text.
 * <p>
 * This DTO encapsulates the necessary information for the AI service to process
 * a piece of text, such as a clinical note or report, and extract structured
 * information from it based on the specified case type.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExtractionRequestDTO {

    /**
     * The raw, unstructured text content from which data needs to be extracted.
     * This could be the full text of a discharge summary, a lab report, or any other clinical document.
     * This field is mandatory and cannot be blank.
     */
    @NotBlank(message = "Input text is required")
    private String inputText;

    /**
     * A string representing the type of clinical case (e.g., "Discharge Summary", "OP_CONSULTATION").
     * This provides context to the AI service, allowing it to apply the correct
     * extraction model and logic. This field is mandatory.
     */
    @NotNull(message = "Case type is required")
    private String caseType;
}
