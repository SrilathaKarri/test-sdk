package carestack.ai;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

/**
 * Enum representing the different case types in the healthcare system.
 * Each case type corresponds to a type of medical report or consultation.
 */
@Getter
public enum CaseType {

    /**
     * Case type for discharge summary reports.
     */
    DISCHARGE_SUMMARY("Discharge Summary"),

    /**
     * Case type for radiology report.
     */
    RADIOLOGY_REPORT("Radiology Report"),

    /**
     * Case type for outpatient consultation reports.
     */
    OP_CONSULTATION("Op Consultation");

    /**
     * The label or description associated with the case type.
     */
    private final String label;

    /**
     * Constructor to initialize the label for each case type.
     *
     * @param label the label describing the case type (e.g., "Discharge Summary").
     */
    CaseType(String label) {
        this.label = label;
    }

    /**
     * A static method used for deserializing a string input into the corresponding {@link CaseType} enum value.
     * The method matches the input string against both the enum name and the label (case-insensitive).
     *
     * @param input the string input to convert into a {@link CaseType}.
     * @return the corresponding {@link CaseType} enum value.
     * @throws IllegalArgumentException if no match is found for the input.
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
