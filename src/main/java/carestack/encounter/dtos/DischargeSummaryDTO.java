package carestack.encounter.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents a request to process a Discharge Summary.
 * <p>
 * This DTO acts as a wrapper for discharge summary data, which can be provided
 * either as a list of file URLs ({@code caseSheets}) for unstructured data processing
 * or as a structured {@code payload} ({@link DischargeSummarySections}) for direct
 * FHIR bundle generation.
 * </p>
 *
 * @see HealthInformationDTO
 */
@Data
@NoArgsConstructor
public class DischargeSummaryDTO implements HealthInformationDTO {

    /**
     * A list of URLs pointing to the unstructured discharge summary documents.
     * Used when the AI service needs to perform data extraction.
     */
    @JsonAlias("caseSheets")
    private List<String> caseSheets;

    /**
     * A structured representation of the discharge summary's content.
     * Used when the data is already extracted and can be directly converted into a FHIR bundle.
     */
    @Valid
    private DischargeSummarySections payload;

    @Override
    public Object getPayload() { return payload; }

    @Override
    public void setPayload(Object payload) {
        ObjectMapper objectMapper = new ObjectMapper();
        this.payload = (payload instanceof DischargeSummarySections)
                ? (DischargeSummarySections) payload
                : objectMapper.convertValue(payload, DischargeSummarySections.class);
    }
}