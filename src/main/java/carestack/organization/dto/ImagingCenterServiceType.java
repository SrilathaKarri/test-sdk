package carestack.organization.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Represents a service type offered by an imaging center.
 * <p>Each service has a name and a count, which represents the number of times the service is offered or available.</p>
 *
 * <p>Validation constraints ensure that:</p>
 * <ul>
 *     <li>The service name is non-null, and its length must be between 2 and 100 characters.</li>
 *     <li>The count is a non-negative number (>= 0).</li>
 * </ul>
 */
@Data
public class ImagingCenterServiceType {

    /**
     * The name of the service offered by the imaging center.
     * <p>The service name must be between 2 and 100 characters long.</p>
     * <p>This field is required and cannot be null.</p>
     */
    @NotNull
    @Size(min = 2, max = 100)
    private String service;

    /**
     * The count of how many times the service is offered or available.
     * <p>The count must be a non-negative number (>= 0).</p>
     * <p>This field cannot be negative, ensuring it represents a valid count.</p>
     */
    @Min(value = 0, message = "Count must be a non-negative number")
    private int count;
}
