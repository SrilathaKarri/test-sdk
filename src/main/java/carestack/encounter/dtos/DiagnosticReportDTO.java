package carestack.encounter.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents a request to process a Diagnostic Report.
 * <p>
 * This DTO acts as a wrapper for diagnostic report data, which can be provided
 * either as a list of file URLs ({@code caseSheets}) for unstructured data processing
 * or as a structured {@code payload} ({@link DiagnosticReportSections}) for direct
 * FHIR bundle generation.
 * </p>
 *
 * @see HealthInformationDTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticReportDTO implements HealthInformationDTO {

    /**
     * A list of URLs pointing to the  diagnostic report documents
     * Used when the AI service needs to perform data extraction.
     */
    @JsonAlias("caseSheets")
    private List<String> caseSheets;

    /**
     * A structured representation of the diagnostic report's content.
     * Used when the data is already extracted and can be directly converted into a FHIR bundle.
     */
    @Valid
    private DiagnosticReportSections payload;

    @Override
    public Object getPayload() {
        return this.payload;
    }

    @Override
    public void setPayload(Object payload) {
        ObjectMapper objectMapper = new ObjectMapper();
        this.payload = (payload instanceof DiagnosticReportSections)
                ? (DiagnosticReportSections) payload
                : objectMapper.convertValue(payload, DiagnosticReportSections.class);
    }
}