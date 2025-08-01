package carestack.documentLinking.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a collection of resource identifiers related to a health event.
 * <p>
 * This DTO is used within the document linking context to hold references
 * to the key entities involved, such as the patient, practitioner, and appointment.
 * </p>
 * <p>
 * Note: This class is distinct from the {@code carestack.appointment.dto.ResourceType} and
 * the {@code carestack.base.enums.ResourceType} enum.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResourceType {

    /**
     * A generic reference identifier, often for the primary resource like an appointment.
     */
    @JsonAlias("reference")
    private String reference;

    /**
     * The unique reference identifier for the practitioner.
     */
    @JsonAlias("practitionerReference")
    private String practitionerReference;

    /**
     * The unique reference identifier for the patient.
     */
    @JsonAlias("patientReference")
    private String patientReference;

    /**
     * The identifier for the specific time slot of the appointment.
     */
    @JsonAlias("slot")
    private String slot;

    /**
     * The priority level of the event (e.g., "ROUTINE", "URGENT").
     */
    @JsonAlias("priority")
    private String priority;

    /**
     * The start date and time of the event in ISO-8601 format.
     */
    @JsonAlias("start")
    private String start;

    /**
     * The end date and time of the event in ISO-8601 format.
     */
    @JsonAlias("end")
    private String end;

    /**
     * The identifier for the organization where the event occurred.
     */
    @JsonAlias("organizationId")
    private String organizationId;
}