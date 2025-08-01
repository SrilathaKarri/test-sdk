package carestack.encounter.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the collected findings from a physical examination.
 * <p>
 * This DTO aggregates a standard set of vital signs, such as blood pressure,
 * heart rate, and temperature. Each vital sign is represented by a {@link VitalSign} object.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PhysicalExamination {

    /**
     * The patient's blood pressure measurement.
     */
    @JsonProperty("bloodPressure")
    private VitalSign bloodPressure;

    /**
     * The patient's heart rate measurement.
     */
    @JsonProperty("heartRate")
    private VitalSign heartRate;

    /**
     * The patient's respiratory rate measurement.
     */
    @JsonProperty("respiratoryRate")
    private VitalSign respiratoryRate;

    /**
     * The patient's body temperature measurement.
     */
    private VitalSign temperature;

    /**
     * The patient's blood oxygen saturation level.
     */
    @JsonProperty("oxygenSaturation")
    private VitalSign oxygenSaturation;

    /**
     * The patient's height measurement.
     */
    private VitalSign height;

    /**
     * The patient's weight measurement.
     */
    private VitalSign weight;
}