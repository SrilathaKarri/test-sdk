package carestack.organization.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Data Transfer Object (DTO) for updating the SPOC (Single Point of Contact) for an organization.
 * <p>This class is used to update the SPOC details within an organization, including the name, ID, and consent manager information.</p>
 */
@Data
public class UpdateSpocForOrganization {

    /**
     * The name of the SPOC (Single Point of Contact) for the organization.
     * <p>This field is required and must be between 3 and 100 characters long.</p>
     */
    @NotNull
    @Size(min = 3, max = 100)
    private String spocName;

    /**
     * The ID of the organization for which the SPOC is being updated.
     * <p>This field is required and must be between 1 and 50 characters long.</p>
     */
    @NotNull
    @Size(min = 1, max = 50)
    private String id;

    /**
     * The SPOC ID (identifier) for the individual who is responsible for being the contact person for the organization.
     * <p>This field is required and must be between 1 and 50 characters long.</p>
     */
    @NotNull
    @Size(min = 1, max = 50)
    private String spocId;

    /**
     * The name of the consent manager associated with the organization, if applicable.
     * <p>This field is optional and can be up to 100 characters long.</p>
     */
    @Size(max = 100)
    private String consentManagerName;

    /**
     * The ID of the consent manager associated with the organization, if applicable.
     * <p>This field is optional and can be up to 50 characters long.</p>
     */
    @Size(max = 50)
    private String consentManagerId;
}
