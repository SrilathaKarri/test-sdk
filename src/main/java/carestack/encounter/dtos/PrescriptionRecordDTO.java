package carestack.encounter.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents a request to process a Prescription Record.
 * <p>
 * This DTO acts as a wrapper for prescription data, which can be provided
 * either as a list of file URLs ({@code caseSheets}) for unstructured data processing
 * or as a structured {@code payload} ({@link PrescriptionSections}) for direct
 * FHIR bundle generation.
 * </p>
 *
 * @see HealthInformationDTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionRecordDTO implements HealthInformationDTO {

    /**
     * A list of URLs pointing to the unstructured prescription documents.
     * Used when the AI service needs to perform data extraction.
     */
    @JsonAlias("caseSheets")
    private List<String> caseSheets;

    /**
     * A structured representation of the prescription's content.
     * Used when the data is already extracted and can be directly converted into a FHIR bundle.
     */
    @Valid
    private PrescriptionSections payload;

    @Override
    public Object getPayload() {
        return payload;
    }

    @Override
    public void setPayload(Object payload) {
        ObjectMapper objectMapper = new ObjectMapper();
        this.payload = (payload instanceof PrescriptionSections)
                ? (PrescriptionSections) payload
                : objectMapper.convertValue(payload, PrescriptionSections.class);
    }
}