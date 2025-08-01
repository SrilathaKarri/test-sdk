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

/**
 * Defines the structured sections of an Immunization Record.
 * <p>
 * This DTO is used as the {@code payload} within an {@link ImmunizationRecordDTO}
 * when providing pre-structured data. It includes patient and doctor details,
 * and a list of immunizations administered.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImmunizationRecordSections {

    /**
     * Details of the patient who received the immunizations. This section is mandatory.
     */
    @Valid
    @NotNull
    @JsonAlias("Patient Details")
    private PatientDetails patientDetails;

    /**
     * A list of doctors or practitioners associated with the immunization event.
     * This section is mandatory.
     */
    @Valid
    @NotNull
    @JsonAlias("Doctor Details")
    private List<DoctorDetails> doctorDetails;

    /**
     * A list of the immunizations administered. This section is mandatory.
     */
    @NonNull
    @JsonProperty(required = true)
    private List<String> immunizations;
}