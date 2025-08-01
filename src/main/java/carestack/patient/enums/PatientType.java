package carestack.patient.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum representing the types of patients.
 * This helps categorize patients into two types:
 * - OLD: Existing patients.
 * - NEW: Newly registered patients.
 */
public enum PatientType {
    OLD,NEW;

    @JsonValue
    public String getValue() {
        return name();
    }

    @JsonCreator
    public static PatientType fromString(String value) {
        for (PatientType type : PatientType.values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid PatientType: " + value);
    }
}
