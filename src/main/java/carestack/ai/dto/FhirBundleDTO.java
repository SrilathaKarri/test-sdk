package carestack.ai.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import carestack.base.enums.CaseType;

import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object (DTO) representing the input for FHIR bundle generation.
 * <p>
 * This DTO is used to encapsulate all necessary details for generating a FHIR bundle.
 * It supports two main workflows:
 * <ol>
 *   <li><b>File-based:</b> Uses {@code extractedData} from an AI service and a list of encrypted {@code files}.</li>
 *   <li><b>Payload-based:</b> Uses an {@code encryptedData} payload representing a structured object.</li>
 * </ol>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FhirBundleDTO {

    /**
     * An optional unique identifier for the source record being converted into a FHIR bundle.
     */
    private String recordId;

    /**
     * Raw structured data extracted from a clinical document, represented as key-value pairs.
     * <p>
     * This is typically the output of a data extraction step (e.g., from a discharge summary)
     * and serves as the primary input for FHIR resource creation.
     * Must contain at least one entry if provided.
     */
    @Size(min = 1, message = "extractedData must not be empty if provided")
    private Map<String, Object> extractedData;

    /**
     * The encrypted version of the primary clinical data payload.
     * <p>
     * This is used in workflows where the main input is a structured JSON object that
     * needs to be encrypted before being sent to the FHIR generation service.
     */
    private String encryptedData;

    /**
     * The public key used for encrypting the data.
     */
    private String publicKey;

    /**
     * Type of clinical case represented by this bundle (e.g., Discharge Summary, Radiology Report).
     * <p>
     * This field is required and provides context to the generation service.
     */
    @NotNull(message = "caseType must not be null")
    private CaseType caseType;

    /**
     * A list of encrypted file references, such as case sheets or lab reports.
     * <p>
     * This is used in file-based FHIR generation workflows to pass along references
     * to all relevant encrypted documents that support the clinical context.
     */
    private List<String> files;

}