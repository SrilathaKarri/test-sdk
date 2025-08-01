package carestack.encounter.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;
import java.util.Map;

/**
 * Defines the structured sections of a Wellness Record.
 * <p>
 * This DTO is used as the {@code payload} within a {@link WellnessRecordDTO}
 * when providing pre-structured data. It captures a wide range of health and
 * lifestyle metrics, from vital signs to physical activity and women's health.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WellnessRecordSections {

    /**
     * Details of the patient to whom the record belongs. This section is mandatory.
     */
    @Valid
    @NotNull
    @JsonAlias("Patient Details")
    private PatientDetails patientDetails;

    /**
     * A list of doctors or practitioners associated with the wellness record.
     * This section is mandatory.
     */
    @Valid
    @NotNull
    @JsonAlias("Doctor Details")
    private List<DoctorDetails> doctorDetails;

    /**
     * A map of the patient's vital signs. This section is mandatory.
     */
    @NonNull
    @JsonProperty("vitalSigns")
    private Map<String, VitalSign> vitalSigns;

    /**
     * A map of the patient's body measurements (e.g., height, weight, BMI).
     * This section is mandatory.
     */
    @NonNull
    @JsonProperty("bodyMeasurements")
    private Map<String, VitalSign> bodyMeasurements;

    /**
     * A map of the patient's physical activities. This section is mandatory.
     */
    @NonNull
    @JsonProperty("physicalActivities")
    private Map<String, VitalSign> physicalActivities;

    /**
     * A map of health metrics specific to women's health. This section is mandatory.
     */
    @NonNull
    @JsonProperty("womenHealth")
    private Map<String, VitalSign> womenHealth;

    /**
     * A map of the patient's lifestyle habits (e.g., smoking, alcohol consumption).
     * This section is mandatory.
     */
    @NonNull
    @JsonProperty("lifeStyle")
    private Map<String, VitalSign> lifeStyle;

    /**
     * A map for any other miscellaneous wellness metrics. This section is mandatory.
     */
    @NonNull
    private Map<String, VitalSign> others;
}