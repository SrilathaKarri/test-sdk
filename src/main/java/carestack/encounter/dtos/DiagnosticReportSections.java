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
 * Defines the structured sections of a Diagnostic Report.
 * <p>
 * This DTO is used as the {@code payload} within a {@link DiagnosticReportDTO}
 * when providing pre-structured data. It includes patient and doctor details,
 * and the specific lab report findings.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DiagnosticReportSections {

    /**
     * Details of the patient to whom the report belongs. This section is mandatory.
     */
    @Valid
    @NotNull
    @JsonAlias("Patient Details")
    private PatientDetails patientDetails;

    /**
     * A list of doctors associated with the report.
     * This section is mandatory.
     */
    @Valid
    @NotNull
    @JsonAlias("Doctor Details")
    private List<DoctorDetails> doctorDetails;

    /**
     * The detailed results of the laboratory tests. This section is mandatory.
     */
    @NonNull
    @JsonProperty("labReports")
    private LabReportItem labReports;
}