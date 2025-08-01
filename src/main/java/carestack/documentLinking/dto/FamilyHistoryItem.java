package carestack.documentLinking.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single entry in a patient's family medical history.
 * <p>
 * This DTO captures a specific medical condition and the family member's
 * relation to the patient (e.g., Mother, Father).
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FamilyHistoryItem {

    /**
     * The family relationship of the individual with the condition (e.g., "Mother", "Father", "Sibling").
     * This field is mandatory.
     */
    @NotBlank
    private String relation;

    /**
     * The medical condition or disease affecting the family member (e.g., "Hypertension", "Diabetes").
     * This field is mandatory.
     */
    @NotBlank
    private String condition;
}