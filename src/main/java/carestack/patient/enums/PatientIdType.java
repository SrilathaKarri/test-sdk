package carestack.patient.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum representing different types of patient IDs.
 * This includes various ID types that can be used to identify a patient:
 * - ABHA: Ayushman Bharat Health Account.
 * - AADHAAR: Aadhaar Card number.
 * - PAN: Permanent Account Number (for taxation purposes).
 * - DRIVING_LICENSE: Driver's license number.
 */
public enum PatientIdType {
    ABHA, AADHAAR, PAN, DRIVING_LICENSE;

    @JsonValue
    public String getValue() {
        return name();
    }

    @JsonCreator
    public static PatientIdType fromString(String value) {
        for (PatientIdType type : PatientIdType.values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid PatientIdType: " + value);
    }
}
