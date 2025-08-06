package carestack.encounter.documentLinking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single entry in a patient's past medical history.
 * <p>
 * This DTO is used to capture either a past medical condition or a past procedure
 * that the patient has undergone.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MedicalHistoryItem {

    /**
     * A past medical condition the patient has been diagnosed with (e.g., "Asthma").
     */
    private String condition;

    /**
     * A past surgical or medical procedure the patient has undergone (e.g., "Appendectomy").
     */
    private String procedure;
}