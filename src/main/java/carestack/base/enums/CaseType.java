package carestack.base.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * Enum representing various types of clinical cases that can be used
 * for FHIR bundle generation or other EHR-related operations.
 * <p>
 * Each case type has a user-friendly label associated with it,
 * allowing flexibility in accepting either enum name or label
 * during input parsing.
 */
@Getter
public enum CaseType {

    /**
     * Represents a case type for hospital discharge summaries.
     */
    DISCHARGE_SUMMARY("DischargeSummary"),

    /**
     * Represents a case type for radiology/imaging reports.
     */
    RADIOLOGY_REPORT("Radiology Report"),

    /**
     * Represents a case type for outpatient consultations.
     */
    OP_CONSULTATION("OPConsultation"),

    /**
     * Represents a case type for prescriptions issued by a provider.
     */
    PRESCRIPTION("Prescription"),

    /**
     * Represents a case type for diagnostic lab reports.
     */
    DIAGNOSTIC_REPORT("DiagnosticReport"),

    /**
     * Represents a case type for immunization history or vaccination records.
     */
    IMMUNIZATION_RECORD("ImmunizationRecord"),

    /**
     * Represents a case type for miscellaneous health documents.
     */
    HEALTH_DOCUMENT_RECORD("HealthDocumentRecord"),

    /**
     * Represents a case type for wellness-related records (e.g., fitness, nutrition).
     */
    WELLNESS_RECORD("WellnessRecord");

    /**
     * The user-friendly label of the case type.
     */
    private final String label;

    /**
     * Constructs a new CaseType enum with the specified display label.
     *
     * @param label The readable label associated with this case type.
     */
    CaseType(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    /**
     * Factory method for deserializing a CaseType from a string input.
     * <p>
     * Accepts either the enum name (e.g., "DISCHARGE_SUMMARY") or
     * the user-friendly label (e.g., "Discharge Summary").
     *
     * @param input The string value to match against the enum name or label.
     * @return The matching CaseType.
     * @throws IllegalArgumentException If no match is found.
     */
    @JsonCreator
    public static CaseType fromInput(String input) {
        if (input == null) return null;

        for (CaseType type : values()) {
            if (type.name().equalsIgnoreCase(input) || type.label.equalsIgnoreCase(input)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Invalid caseType: " + input);
    }
}
