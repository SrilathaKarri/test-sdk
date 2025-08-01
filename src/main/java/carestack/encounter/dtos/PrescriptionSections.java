package carestack.encounter.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;

/**
 * Defines the structured sections of a Prescription Record.
 * <p>
 * This DTO is used as the {@code payload} within a {@link PrescriptionRecordDTO}
 * when providing pre-structured data. It primarily contains a list of the
 * medications that were prescribed.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PrescriptionSections {

    /**
     * A list of medications prescribed to the patient. This section is mandatory.
     */
    @NonNull
    @JsonProperty("prescribedMedications")
    private List<String> prescribedMedications;
}