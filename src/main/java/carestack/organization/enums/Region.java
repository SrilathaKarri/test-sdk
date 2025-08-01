package carestack.organization.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum representing regions, specifically "Urban" and "Rural".
 * <p>This enum class allows easy mapping of region values, supporting multiple aliases for each region type.</p>
 */
@Getter
@AllArgsConstructor
public enum Region {

    /**
     * Represents the Urban region.
     * The code for this region is "U", and it supports the aliases "urban" and "u".
     */
    URBAN("U", new String[]{"urban", "u"}),

    /**
     * Represents the Rural region.
     * The code for this region is "R", and it supports the aliases "rural" and "r".
     */
    RURAL("R", new String[]{"rural", "r"});

    // A lookup map to hold aliases for faster lookup.
    private static final Map<String, Region> LOOKUP = new HashMap<>();

    // Static block to populate the LOOKUP map with alias values.
    static {
        for (Region region : Region.values()) {
            for (String alias : region.aliases) {
                LOOKUP.put(alias.toLowerCase(), region);
            }
        }
    }

    /**
     * The code for the region (e.g., "U" for Urban, "R" for Rural).
     */
    private final String code;

    /**
     * An array of aliases (e.g., "urban", "u", etc.) for the region.
     */
    private final String[] aliases;

    /**
     * Converts a string value to the corresponding {@link Region} enum constant.
     * <p>This method is used to convert region values passed as strings, including their aliases, into their respective enum type.</p>
     *
     * @param value The region strings to convert (can be case-insensitive).
     * @return The corresponding {@link Region} enum constant.
     * @throws IllegalArgumentException if the value is null or does not match any region.
     */
    @JsonCreator
    public static Region fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Region cannot be null");
        }
        Region region = LOOKUP.get(value.toLowerCase());
        if (region == null) {
            throw new IllegalArgumentException("Invalid region: " + value);
        }
        return region;
    }

    /**
     * Gets the code associated with the region.
     *
     * @return The code (e.g., "U" for Urban, "R" for Rural).
     */
    @JsonValue
    public String getCode() {
        return code;
    }
}
