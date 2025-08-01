package carestack.encounter.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single entry in a patient's past medical history.
 * <p>
 * This DTO is used to capture either a past medical condition or a past procedure
 * that the patient has undergone. The setters ensure that empty strings are
 * converted to null to avoid sending empty fields.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MedicalHistoryItem {
    /**
     * A past medical condition the patient has been diagnosed with (e.g., "Asthma").
     */
    private String condition;

    /**
     * A past surgical or medical procedure the patient has undergone (e.g., "Appendectomy").
     */
    private String procedure;

    /**
     * Sets the condition, converting an empty or whitespace-only string to null.
     * @param condition The medical condition.
     */
    public void setCondition(String condition) {
        this.condition = (condition != null && condition.trim().isEmpty()) ? null : condition;
    }

    /**
     * Sets the procedure, converting an empty or whitespace-only string to null.
     * @param procedure The medical procedure.
     */
    public void setProcedure(String procedure) {
        this.procedure = (procedure != null && procedure.trim().isEmpty()) ? null : procedure;
    }
}