package carestack.encounter.documentLinking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object representing a structured Out-Patient (OP) Consultation record.
 * <p>
 * This class aggregates all the typical sections of a consultation note, including
 * chief complaints, examination findings, medical history, diagnoses, and treatment plans.
 * It is used as a structured payload for generating FHIR bundles.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OpConsultationDTO {

    /**
     * The primary reasons for the patient's visit, as reported by the patient.
     * This field is mandatory.
     */
    @NotBlank(message = "chiefComplaints cannot be empty")
    private String chiefComplaints;

    /**
     * The findings from the physical examination, including vital signs.
     * This field is mandatory.
     */
    @NotNull
    @Valid
    private PhysicalExamination physicalExamination;

    /**
     * A list of the patient's past medical conditions and procedures.
     * This field is mandatory and the list cannot be empty.
     */
    @NotEmpty(message = "medicalHistory cannot be empty")
    @Valid
    private List<MedicalHistoryItem> medicalHistory;

    /**
     * A list of relevant medical conditions in the patient's family.
     * This field is mandatory and the list cannot be empty.
     */
    @NotEmpty(message = "familyHistory cannot be empty")
    @Valid
    private List<FamilyHistoryItem> familyHistory;

    /**
     * A list of known allergies for the patient.
     * This field is mandatory and the list cannot be empty.
     */
    @NotEmpty(message = "allergies cannot be empty")
    private List<@NotBlank(message = "allergy cannot be empty") String> allergies;

    /**
     * A list of immunizations the patient has received.
     * This field is mandatory and the list cannot be empty.
     */
    @NotEmpty(message = "immunizations cannot be empty")
    private List<@NotBlank(message = "immunization cannot be empty") String> immunizations;

    /**
     * A list of medications the patient is currently taking.
     * This field is mandatory and the list cannot be empty.
     */
    @NotEmpty(message = "currentMedications cannot be empty")
    private List<@NotBlank(message = "current medication cannot be empty") String> currentMedications;

    /**
     * A list of diagnoses or conditions identified during the consultation.
     * This field is mandatory and the list cannot be empty.
     */
    @NotEmpty(message = "conditions cannot be empty")
    private List<@NotBlank(message = "condition cannot be empty") String> conditions;

    /**
     * A list of recommended investigations or lab tests.
     * This field is mandatory and the list cannot be empty.
     */
    @NotEmpty(message = "investigationAdvice cannot be empty")
    private List<@NotBlank(message = "investigation advice cannot be empty") String> investigationAdvice;

    /**
     * A list of medications prescribed during the consultation.
     * This field is mandatory and the list cannot be empty.
     */
    @NotEmpty(message = "prescribedMedications cannot be empty")
    private List<@NotBlank(message = "prescribed medication cannot be empty") String> prescribedMedications;

    /**
     * A list of procedures performed or advised during the consultation.
     * This field is mandatory and the list cannot be empty.
     */
    @NotEmpty(message = "currentProcedures cannot be empty")
    @Valid
    private List<ProcedureItem> currentProcedures;

    /**
     * A list of general advice or notes given to the patient.
     * This field is mandatory and the list cannot be empty.
     */
    @NotEmpty(message = "advisoryNotes cannot be empty")
    private List<@NotBlank(message = "advisory note cannot be empty") String> advisoryNotes;

    /**
     * Instructions for patient follow-up.
     * This field is mandatory and the list cannot be empty.
     */
    @NotEmpty(message = "followUp cannot be empty")
    private List<@NotBlank(message = "follow up cannot be empty") String> followUp;

    /**
     * A list of document references (e.g., URLs) associated with this consultation.
     * This field is mandatory.
     */
    @NotNull
    private List<@NotBlank(message = "opConsultDocument cannot be empty") String> opConsultDocument;

}