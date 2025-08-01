package carestack.encounter.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.Map;

/**
 * Represents a single clinical investigation or lab test.
 * <p>
 * This DTO captures the results of a test, including a map of specific
 * observations (e.g., "Hemoglobin", "WBC Count"), the overall status of the test,
 * and the date it was recorded.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class InvestigationItem {

    /**
     * A map of specific observations from the investigation. The key is the name
     * of the observation (e.g., "Glucose"), and the value is a {@link VitalSign}
     * object containing the measured value and unit. This field is mandatory.
     */
    @NonNull
    @JsonProperty(required = true)
    private Map<String, VitalSign> observations;

    /**
     * The status of the investigation (e.g., "final", "preliminary", "corrected").
     * This field is mandatory.
     */
    @NotBlank(message = "Status cannot be empty.")
    @JsonProperty(required = true)
    private String status;

    /**
     * The date when the investigation was recorded, in a string format.
     * This field is mandatory.
     */
    @NotBlank(message = "Recorded date cannot be empty.")
    @JsonProperty(required = true)
    private String recordedDate;
}