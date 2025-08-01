package carestack.encounter.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import carestack.base.enums.CaseType;

/**
 * A wrapper Data Transfer Object for submitting a health information record.
 * <p>
 * This class encapsulates a specific {@link HealthInformationDTO} implementation
 * along with its corresponding {@link CaseType}, providing a standardized
 * request format for various services.
 * </p>
 */
@Data
@NoArgsConstructor
public class HealthInformationRequestDTO {

    /**
     * The specific health information DTO (e.g., {@link OPConsultationDTO}).
     * This field is mandatory.
     */
    @NonNull
    @JsonProperty(required = true)
    private HealthInformationDTO dto;

    /**
     * The type of clinical case, which provides context for processing the DTO.
     */
    private CaseType caseType;

    /**
     * A flexible field for any additional data that may be required.
     */
    private Object data;
}