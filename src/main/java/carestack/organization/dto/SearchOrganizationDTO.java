package carestack.organization.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * Data Transfer Object (DTO) for searching organizations.
 * <p>This class represents the fields required to search for an organization based on various filters such as ownership code, region, pincode, and organization name.</p>
 */
@Data
public class SearchOrganizationDTO {

    /**
     * The ownership code of the organization.
     * <p>This field filters organizations based on their ownership code. It is optional and can be up to 50 characters long.</p>
     */
    @Size(max = 50)
    private String ownershipCode;

    /**
     * The state LGD (Local Government Directory) code.
     * <p>This field is used to filter organizations based on their state LGD code. It is optional and can be up to 50 characters long.</p>
     */
    @Size(max = 50)
    private String stateLGDCode;

    /**
     * The district LGD code.
     * <p>This field filters organizations based on the district LGD code. It is optional and can be up to 50 characters long.</p>
     */
    @Size(max = 50)
    private String districtLGDCode;

    /**
     * The sub-district LGD code.
     * <p>This field filters organizations based on the subdistrict LGD code. It is optional and can be up to 50 characters long.</p>
     */
    @Size(max = 50)
    private String subDistrictLGDCode;

    /**
     * The pincode of the organization's location.
     * <p>This field filters organizations based on their pincode. It is optional but, if provided, must be a 6-digit number.</p>
     */
    @Size(max = 10)
    @Pattern(regexp = "^[0-9]{6}$", message = "Pincode must be a 6-digit number")
    private String pincode;

    /**
     * The name of the organization.
     * <p>This field is used to search for organizations by their name. It must be between 3 and 255 characters long.</p>
     */
    @Size(min = 3, max = 255, message = "Organization name must be between 3 and 255 characters")
    @JsonProperty("facilityName")
    private String organizationName;

    /**
     * The unique identifier for the organization (optional).
     * <p>This field is used to search for an organization by its ID. It can be up to 50 characters long.</p>
     */
    @Size(max = 50)
    @JsonProperty("facilityId")
    private String organizationId;

    /**
     * The page number for pagination.
     * <p>This field indicates which page of results to return. It is a required field and must be at least 1.</p>
     */
    @NotNull(message = "Page number is required")
    @Min(value = 1, message = "Page number must be at least 1")
    private Integer page;

    /**
     * The number of results per page.
     * <p>This field controls how many results are returned on each page. It is a required field and must be between 1 and 100.</p>
     */
    @NotNull(message = "Results per page is required")
    @Min(value = 1, message = "Results per page must be at least 1")
    @Max(value = 100, message = "Results per page cannot exceed 100")
    private Integer resultsPerPage;
}
