package carestack.base.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum representing the gender of an individual.
 * This enum is used to store gender information with predefined constants.
 * Example usage:
 * <pre>
 *     Gender gender = Gender.male;
 *     System.out.println(gender); // Outputs: male
 * </pre>
 */
@Getter
@RequiredArgsConstructor
public enum Gender {
    MALE("male"),
    FEMALE("female"),
    OTHER("other"),
    UNKNOWN("unknown");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Gender fromValue(String value) {
        for (Gender gender : values()) {
            if (gender.value.equalsIgnoreCase(value)) {
                return gender;
            }
        }
        throw new IllegalArgumentException("Invalid gender value: " + value);
    }
}
