package carestack.encounter.dtos;

import com.fasterxml.jackson.annotation.JsonSubTypes;

import java.util.List;

/**
 * Defines a common interface for all health information Data Transfer Objects (DTOs).
 * <p>
 * This interface ensures that different types of health records (e.g., OP Consultation,
 * Discharge Summary) can be handled polymorphically by the {@link carestack.encounter.Encounter} service.
 * It standardizes the access to both unstructured file references ({@code caseSheets}) and
 * structured data ({@code payload}).
 * </p>
 * <p>
 * The {@code @JsonSubTypes} annotation allows Jackson to correctly deserialize JSON
 * into the specific DTO implementation based on a type identifier.
 * </p>
 */
@JsonSubTypes({
        @JsonSubTypes.Type(value = OPConsultationDTO.class, name = "OPConsultation"),
        @JsonSubTypes.Type(value = DischargeSummaryDTO.class, name = "DischargeSummary"),
        @JsonSubTypes.Type(value = PrescriptionRecordDTO.class, name = "Prescription"),
        @JsonSubTypes.Type(value = WellnessRecordDTO.class, name = "WellnessRecord"),
        @JsonSubTypes.Type(value = ImmunizationRecordDTO.class, name = "ImmunizationRecord"),
        @JsonSubTypes.Type(value = DiagnosticReportDTO.class, name = "DiagnosticReport"),
        @JsonSubTypes.Type(value = HealthDocumentRecordDTO.class, name = "HealthDocumentRecord")
})
public interface HealthInformationDTO {
    /**
     * Gets the list of file URLs associated with the health record.
     *
     * @return A list of strings representing file URLs.
     */
    List<String> getCaseSheets();

    /**
     * Sets the list of file URLs for the health record.
     *
     * @param caseSheets A list of strings representing file URLs.
     */
    void setCaseSheets(List<String> caseSheets);

    /**
     * Gets the structured payload of the health record.
     *
     * @return The structured data as an Object.
     */
    Object getPayload();

    /**
     * Sets the structured payload of the health record.
     *
     * @param payload The structured data as an Object.
     */
    void setPayload(Object payload);
}