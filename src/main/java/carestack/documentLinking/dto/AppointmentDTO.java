package carestack.documentLinking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import carestack.base.enums.DocLinkingEnums;

import java.time.ZonedDateTime;

/**
 * DTO that represents an appointment, including start and end dates, patient and practitioner references, and other details.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentDTO {

    /**
     * The reference identifier for the practitioner associated with this appointment.
     * It cannot be empty.
     */
    @NotEmpty(message = "Practitioner reference is required and cannot be empty")
    private String practitionerReference;

    /**
     * The reference identifier for the patient associated with this appointment.
     * It cannot be empty.
     */
    @NotEmpty(message = "Patient reference is required and cannot be empty")
    private String patientReference;

    /**
     * The start date and time of the appointment in ISO 8601 format (e.g., "2025-02-28T09:00:00Z").
     * It must not be empty and must follow the correct format.
     */

    @NotNull(message = "Appointment start date is required and cannot be empty")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private ZonedDateTime start;

    /**
     * The end date and time of the appointment in ISO 8601 format (e.g., "2025-02-28T09:00:00Z").
     * It must not be empty and must follow the correct format.
     */
    @NotNull(message = "Appointment end date is required and cannot be empty")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private ZonedDateTime end;

    /**
     * The priority of the appointment, if applicable.
     */
    private DocLinkingEnums.AppointmentPriority priority;

    /**
     * The organization ID associated with this appointment, if applicable.
     */
    private String organizationId;

    /**
     * The slot in which the appointment is scheduled.
     */
    private String slot;

    /**
     * The reference identifier for this appointment.
     */
    private String reference;
}