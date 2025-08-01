package carestack.documentLinking.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import carestack.base.enums.DocLinkingEnums;

/**
 * Data Transfer Object for creating a new care context.
 * <p>
 * This DTO encapsulates all the necessary information to establish a care context,
 * which links a patient, a practitioner, and a specific appointment or health event.
 * It is a key part in the document linking workflow.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateCareContextDTO {

    /**
     * The unique reference identifier for the patient. This field is mandatory.
     */
    @NotNull
    @JsonAlias("patientReference")
    private String patientReference;

    /**
     * The patient's ABHA (Ayushman Bharat Health Account) address.
     */
    @JsonAlias("patientAbhaAddress")
    private String patientAbhaAddress;

    /**
     * The unique reference identifier for the practitioner. This field is mandatory.
     */
    @NotNull
    @JsonAlias("practitionerReference")
    private String practitionerReference;

    /**
     * The unique reference identifier for the appointment. This field is mandatory.
     */
    @NotNull
    @JsonAlias("appointmentReference")
    private String appointmentReference;

    /**
     * The type of health information associated with this care context (e.g., Prescription, Diagnostic Report).
     * This field is mandatory.
     */
    @NotNull
    @JsonAlias("hiType")
    private DocLinkingEnums.HealthInformationTypes hiType;

    /**
     * The date of the appointment in a string format. This field is mandatory.
     */
    @NotNull
    @JsonAlias("appointmentDate")
    private String appointmentDate;

    /**
     * A flag indicating whether an OTP (One-Time Password) should be resent for verification.
     * This field is mandatory.
     */
    @NotNull
    @JsonAlias("resendOtp")
    private Boolean resendOtp;
}