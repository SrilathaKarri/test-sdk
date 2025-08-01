package carestack.documentLinking.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single vital sign measurement, consisting of a value and its unit.
 * <p>
 * This is a generic DTO used to model various clinical observations like
 * blood pressure, heart rate, temperature, etc., in a standardized way.
 * It is typically embedded within other DTOs such as {@link PhysicalExamination}.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VitalSign {

    /**
     * The numerical or textual value of the vital sign measurement (e.g., "120", "80/120").
     * This field is mandatory and cannot be blank.
     */
    @NotBlank(message = "Vital sign value cannot be blank")
    private String value;

    /**
     * The unit of measurement for the vital sign's value (e.g., "mmHg", "bpm", "°C").
     * This field is mandatory and cannot be blank.
     */
    @NotBlank(message = "Vital sign unit cannot be blank")
    private String unit;
}