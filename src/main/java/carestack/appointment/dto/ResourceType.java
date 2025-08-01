package carestack.appointment.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the resource details associated with an appointment request.
 * <p>
 * This class holds the various references and identifiers (like patient, practitioner, slot)
 * that define the context of an appointment. It is typically embedded within an
 * {@link AppointmentResponse}.
 * </p>
 * <p>
 * Note: The class name {@code ResourceType} might be confused with the {@code carestack.base.enums.ResourceType} enum.
 * This class specifically models the resource-related fields of an appointment.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResourceType {

    /**
     * The unique reference identifier for the created appointment.
     */
    @JsonAlias("reference")
    private String appointmentReference;

    /**
     * The reference identifier for the practitioner associated with the appointment.
     */
    @JsonAlias("practitionerReference")
    private String practitionerReference;

    /**
     * The reference identifier for the patient associated with the appointment.
     */
    @JsonAlias("patientReference")
    private String patientReference;

    /**
     * The identifier for the time slot booked for the appointment.
     */
    @JsonAlias("slot")
    private String slot;

    /**
     * The priority level of the appointment (e.g., "EMERGENCY", "FOLLOWUP").
     */
    @JsonAlias("priority")
    private String priority;

    /**
     * The start date and time of the appointment in ISO-8601 format.
     */
    @JsonAlias("start")
    private String appointmentStartTime;

    /**
     * The end date and time of the appointment in ISO-8601 format.
     */
    @JsonAlias("end")
    private String appointmentEndTime;

    /**
     * The identifier for the organization where the appointment is scheduled.
     */
    @JsonAlias("organizationId")
    private String organizationId;
}