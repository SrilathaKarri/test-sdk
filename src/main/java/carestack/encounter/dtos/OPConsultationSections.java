package carestack.encounter.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.NoArgsConstructor;

/**
 * Defines the structured sections of an Out-Patient (OP) Consultation note.
 * <p>
 * This DTO extends {@link CommonHealthInformationDTO} to represent a complete
 * OP consultation record. It is used as the {@code payload} within an
 * {@link OPConsultationDTO} when providing pre-structured data.
 * </p>
 */
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OPConsultationSections extends CommonHealthInformationDTO {
    // This class currently inherits all its fields from CommonHealthInformationDTO.
    // It can be extended with fields specific to OP Consultations in the future if needed.
}