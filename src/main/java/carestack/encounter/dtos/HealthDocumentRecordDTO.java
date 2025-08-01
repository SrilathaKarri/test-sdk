package carestack.encounter.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents a generic health document record where the structure is not predefined.
 * <p>
 * This DTO is used for health documents that may not fit a specific template
 * (like a discharge summary or OP consultation). It primarily relies on the
 * {@code caseSheets} for AI-based data extraction, and its {@code payload}
 * is typically expected to be null or handled generically.
 * </p>
 *
 * @see HealthInformationDTO
 */
@Data
@NoArgsConstructor
public class HealthDocumentRecordDTO implements HealthInformationDTO {

    /**
     * A list of URLs pointing to the unstructured health documents.
     */
    @JsonAlias("caseSheets")
    private List<String> caseSheets;

    /**
     * A flexible payload object. For this DTO, it is typically null, as processing
     * relies on the unstructured data in {@code caseSheets}.
     */
    @Valid
    private Object payload = null;

    @Override
    public Object getPayload() {
        return payload;
    }

    @Override
    public void setPayload(Object payload) {
        this.payload = payload;
    }
}