package carestack.encounter.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * Defines the structured sections of a Discharge Summary.
 * <p>
 * This DTO extends {@link CommonHealthInformationDTO} and adds a specific field for
 * {@code investigations}. It is used as the {@code payload} within a
 * {@link DischargeSummaryDTO} when providing pre-structured data.
 * </p>
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DischargeSummarySections extends CommonHealthInformationDTO {

    /**
     * The results of investigations performed during the patient's stay. This section is mandatory.
     */
    @NonNull
    @JsonProperty(required = true)
    private InvestigationItem investigations;

}