package carestack.documentLinking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import carestack.base.enums.DocLinkingEnums;

import java.util.List;

/**
 * Represents the primary request for the entire health document linking workflow.
 * <p>
 * This DTO aggregates all necessary information to link a health document,
 * including patient and practitioner details, appointment context, and the
 * specific health records (like an OP Consultation or Discharge Summary) to be linked.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HealthDocumentLinkingDTO {

    /**
     * The unique reference identifier for the patient. This field is mandatory.
     */
    @NotBlank
    @JsonProperty("patientReference")
    private String patientReference;

    /**
     * The unique reference identifier for the practitioner. This field is mandatory.
     */
    @NotBlank
    @JsonProperty("practitionerReference")
    private String practitionerReference;

    /**
     * The patient's full postal address. Must be at least 5 characters long.
     */
    @NotBlank
    @Size(min = 5, message = "Patient address must have minimum length of 5")
    @JsonProperty("patientAddress")
    private String patientAddress;

    /**
     * The patient's full name. Must be at least 3 characters long and contain only letters and spaces.
     */
    @NotBlank
    @Size(min = 3, message = "Patient name must have minimum length of 3")
    @jakarta.validation.constraints.Pattern(regexp = "^[a-zA-Z ]+$", message = "Patient name must contain only letters and spaces")
    @JsonProperty("patientName")
    private String patientName;

    /**
     * The start date and time of the appointment in ISO-8601 format. This field is mandatory.
     */
    @NotBlank
    @JsonProperty("appointmentStartDate")
    private String appointmentStartDate;

    /**
     * The end date and time of the appointment in ISO-8601 format. This field is mandatory.
     */
    @NotBlank
    @JsonProperty("appointmentEndDate")
    private String appointmentEndDate;

    /**
     * The priority of the appointment (e.g., ROUTINE, URGENT).
     */
    @JsonProperty("appointmentPriority")
    private DocLinkingEnums.AppointmentPriority appointmentPriority;

    /**
     * The identifier for the organization where the appointment is scheduled. This field is mandatory.
     */
    @NotBlank
    @JsonProperty("organizationId")
    private String organizationId;

    /**
     * The identifier for the specific time slot of the appointment.
     */
    @JsonProperty("appointmentSlot")
    private String appointmentSlot;

    /**
     * The unique reference identifier for the appointment.
     */
    @JsonProperty("reference")
    private String appointmentReference;

    /**
     * The patient's ABHA (Ayushman Bharat Health Account) address.
     */
    @JsonProperty("patientAbhaAddress")
    private String patientAbhaAddress;

    /**
     * The type of health information being linked (e.g., Prescription, Diagnostic Report). This field is mandatory.
     */
    @NotNull
    @JsonProperty("hiType")
    private DocLinkingEnums.HealthInformationTypes hiType;

    /**
     * The patient's mobile number. This field is mandatory.
     */
    @NotBlank
    @JsonProperty("mobileNumber")
    private String mobileNumber;

    /**
     * A list of health records to be linked. Each item in the list is a DTO
     * representing a specific type of health information. This list cannot be empty.
     */
    @NotEmpty
    @Valid
    @JsonProperty("healthRecords")
    private List<HealthInformationDTO> healthRecords;
}