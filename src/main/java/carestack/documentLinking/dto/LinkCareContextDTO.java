package carestack.documentLinking.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import carestack.base.enums.DocLinkingEnums;

/**
 * Represents a request to link a specific care context to a patient's record.
 * <p>
 * This DTO facilitates the association of appointments, patient information,
 * and authentication details for better care coordination and continuity.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LinkCareContextDTO {

    /**
     * A unique identifier for the request to link the care context.
     * This field is required and cannot be empty.
     */
    @NotEmpty(message = "Request ID is required and cannot be empty")
    private String requestId;

    /**
     * A reference to the appointment associated with the care context.
     * This field is required and cannot be empty.
     */
    @NotEmpty(message = "Appointment reference is required and cannot be empty")
    private String appointmentReference;

    /**
     * The patient's ABHA address, which uniquely identifies the patient
     * within the healthcare system.
     * This field is required and cannot be empty.
     */
    @NotEmpty(message = "Patient abha address is required and cannot be empty")
    private String patientAddress;

    /**
     * The full name of the patient.
     * This field is required and cannot be empty.
     */
    @NotEmpty(message = "Patient name is required and cannot be empty")
    private String patientName;

    /**
     * A unique reference identifier for the patient.
     * This field is required and cannot be empty.
     */
    @NotEmpty(message = "Patient reference is required and cannot be empty")
    private String patientReference;

    /**
     * A reference to the care context, representing the specific
     * healthcare event or setting linked to the patient.
     * This field is required and cannot be empty.
     */
    @NotEmpty(message = "Care context reference is required and cannot be empty")
    private String careContextReference;

    /**
     * The mode of authentication used for verifying the patient's identity
     * when linking the care context.
     * This field is required.
     */
    @NotNull(message = "Authentication mode is required")
    private DocLinkingEnums.AuthMode authMode;
}
