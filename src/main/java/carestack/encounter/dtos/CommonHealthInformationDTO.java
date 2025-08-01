package carestack.encounter.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;

/**
 * A base Data Transfer Object containing fields common across various health information records.
 * <p>
 * This class is intended to be extended by more specific DTOs (e.g., {@link DischargeSummarySections})
 * to reduce code duplication. It includes fundamental sections like patient and doctor details,
 * chief complaints, medical history, and treatment plans.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommonHealthInformationDTO {

    /**
     * Details of the patient. This section is mandatory.
     */
    @JsonProperty("Patient Details")
    @Valid
    @NotNull(message = "Patient details are required")
    private PatientDetails patientDetails;

    /**
     * A list of doctors involved in the encounter. This section is mandatory.
     */
    @JsonProperty("Doctor Details")
    @Valid
    @NotNull(message = "Doctor details are required")
    private List<DoctorDetails> doctorDetails;

    /**
     * The primary reason for the patient's visit, as reported by the patient. This field is mandatory.
     */
    @NotBlank(message = "Chief complaints cannot be empty.")
    @JsonProperty("chiefComplaints")
    private String chiefComplaints;

    /**
     * Findings from the physical examination. This section is mandatory.
     */
    @NonNull
    @JsonProperty("physicalExamination")
    private PhysicalExamination physicalExamination;

    /**
     * The patient's past medical history. This section is mandatory.
     */
    @NonNull
    @JsonProperty("medicalHistory")
    private List<MedicalHistoryItem> medicalHistory;

    /**
     * The patient's family medical history.
     */
    @JsonProperty("familyHistory")
    private List<FamilyHistoryItem> familyHistory;

    /**
     * A list of diagnoses or conditions identified.
     */
    private List<String> conditions;

    /**
     * A list of procedures performed or advised. This section is mandatory.
     */
    @NonNull
    @JsonProperty("currentProcedures")
    private List<ProcedureItem> currentProcedures;

    /**
     * A list of medications the patient is currently taking.
     */
    @JsonProperty("currentMedications")
    private List<String> currentMedications;

    /**
     * A list of medications prescribed during this encounter.
     */
    @JsonProperty("prescribedMedications")
    private List<String> prescribedMedications;

    /**
     * A list of known allergies.
     */
    private List<String> allergies;

    /**
     * A list of immunizations the patient has received.
     */
    @JsonProperty("immunizations")
    private List<String> immunizations;

    /**
     * General advice or notes given to the patient.
     */
    @JsonProperty("advisoryNotes")
    private List<String> advisoryNotes;

    /**
     * The ongoing care plan for the patient.
     */
    @JsonProperty("carePlan")
    private List<String> carePlan;

    /**
     * Instructions for patient follow-up.
     */
    @JsonProperty("followUp")
    private List<String> followUp;

}