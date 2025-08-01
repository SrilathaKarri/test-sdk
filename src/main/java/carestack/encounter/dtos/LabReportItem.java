package carestack.encounter.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.Map;

/**
 * Represents the detailed findings of a laboratory report.
 * <p>
 * This DTO extends the basic {@link InvestigationItem} by adding fields for
 * the report's category and conclusion.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LabReportItem {

    /**
     * A map of specific observations from the lab report. The key is the name
     * of the observation (e.g., "Cholesterol"), and the value is a {@link VitalSign}
     * object containing the measured value and unit. This field is mandatory.
     */
    @NonNull
    @JsonProperty(required = true)
    private Map<String, VitalSign> observations;

    /**
     * The status of the lab report (e.g., "final", "preliminary").
     * This field is mandatory.
     */
    @NotBlank(message = "Status cannot be empty.")
    @JsonProperty(required = true)
    private String status;

    /**
     * The date when the lab report was recorded. This field is mandatory.
     */
    @NotBlank(message = "Recorded date cannot be empty.")
    @JsonProperty(required = true)
    private String recordedDate;

    /**
     * The category of the lab report (e.g., "Hematology", "Chemistry").
     * This field is mandatory.
     */
    @NotEmpty
    @JsonProperty("category")
    private String category;

    /**
     * The overall conclusion or summary of the lab report findings.
     * This field is mandatory.
     */
    @NotEmpty
    @JsonProperty("conclusion")
    private String conclusion;
}