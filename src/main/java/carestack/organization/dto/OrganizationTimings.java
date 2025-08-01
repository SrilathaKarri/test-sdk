package carestack.organization.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object representing the operational timings of an organization.
 * <p>This class contains the organization’s working hours, including general human-readable format as well as detailed shifts.</p>
 *
 * <p>It validates that the provided timings follow a specific format and ensures that the shift details (start and end time) are correctly provided.</p>
 */
@Data
public class OrganizationTimings {

    /**
     * General description of the organization's operating hours in a human-readable format.
     * Example: "Monday to Friday, 9 AM to 5 PM".
     * <p>This field must be alphanumeric and may include spaces, commas, colons, hyphens, and parentheses.</p>
     *
     * <p>The field is validated using a regular expression to ensure that only allowed characters are present.</p>
     */
    @NotNull
    @Pattern(regexp = "^[A-Za-z0-9\\s,:\\-()]+$",
            message = "Organization timings must contain only alphanumeric characters, spaces, commas, colons, hyphens, and parentheses")
    private String timings;

    /**
     * List of shifts within the organization.
     * <p>Each shift has a start and end time.</p>
     * <p>The list must not be null and must contain valid shift details.</p>
     */
    @NotNull
    private List<Shift> shifts;

    /**
     * Represents a single shift in the organization.
     * <p>This class contains the start and end time for a specific shift within the organization.</p>
     */
    @Data
    public static class Shift {

        /**
         * The start time of the shift.
         * <p>This is a required field and must be a valid `LocalDateTime`.</p>
         */
        private LocalDateTime start;

        /**
         * The end time of the shift.
         * <p>This is a required field and must be a valid `LocalDateTime`.</p>
         */
        private LocalDateTime end;
    }
}
