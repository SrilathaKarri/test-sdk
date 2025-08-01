package carestack.documentLinking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
@AllArgsConstructor
@NoArgsConstructor
public class PhysicalExamination {

    /**
     * The patient's blood pressure measurement. This field is mandatory.
     */
    @NotNull
    @Valid
    @JsonProperty("bloodPressure")
    private VitalSign bloodPressure;

    /**
     * The patient's heart rate measurement. This field is mandatory.
     */
    @NotNull
    @Valid
    @JsonProperty("heartRate")
    private VitalSign heartRate;

    /**
     * The patient's respiratory rate measurement. This field is mandatory.
     */
    @NotNull
    @Valid
    @JsonProperty("respiratoryRate")
    private VitalSign respiratoryRate;

    /**
     * The patient's body temperature measurement. This field is mandatory.
     */
    @NotNull
    @Valid
    private VitalSign temperature;

    /**
     * The patient's blood oxygen saturation level. This field is mandatory.
     */
    @NotNull
    @Valid
    @JsonProperty("oxygenSaturation")
    private VitalSign oxygenSaturation;

    /**
     * The patient's height measurement. This field is mandatory.
     */
    @NotNull
    @Valid
    private VitalSign height;

    /**
     * The patient's weight measurement. This field is mandatory.
     */
    @NotNull
    @Valid
    private VitalSign weight;
}