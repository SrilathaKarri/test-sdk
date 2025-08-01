package carestack.appointment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import carestack.base.enums.DocLinkingEnums;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for updating an existing appointment.
 * <p>
 * This DTO contains the fields that can be modified for an appointment,
 * such as its start and end times, priority, and associated time slot.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateAppointmentDTO {

    /**
     * The new start date and time for the appointment.
     */
    @JsonProperty("start")
    private LocalDateTime appointmentStartTime;

    /**
     * The new end date and time for the appointment.
     */
    @JsonProperty("end")
    private LocalDateTime appointmentEndTime;

    /**
     * The new priority level for the appointment.
     */
    private DocLinkingEnums.AppointmentPriority priority;

    /**
     * The new time slot identifier for the appointment.
     */
    @JsonProperty("slot")
    private String slot;

}