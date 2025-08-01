package carestack.organization.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Represents the ownership and status details of an organization.
 * <p>This class holds information regarding the ownership type, subtype, and the current status of the organization.</p>
 *
 * <p>Validation constraints ensure that:</p>
 * <ul>
 *     <li>Ownership type, ownership subtype, and status are non-null and must be provided.</li>
 * </ul>
 */
@Data
public class OrganizationDetails {

    /**
     * The ownership type of the organization.
     * <p>This field cannot be null and represents the general category of ownership (e.g., private, government, etc.).</p>
     */
    @NotNull
    private String ownershipType;

    /**
     * The specific subtype of ownership within the broader category.
     * <p>This field cannot be null and provides more detailed information about the ownership subtype (e.g., family-owned, corporate, etc.).</p>
     */
    @NotNull
    private String ownershipSubType;

    /**
     * The current operational status of the organization.
     * <p>This field cannot be null and represents whether the organization is active, inactive, under renovation, etc.</p>
     */
    @NotNull
    private String status;

}
