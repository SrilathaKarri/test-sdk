package carestack.organization.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object representing an organization's complete information.
 * <p>This class encapsulates all details about an organization, including basic information, contact details, documents, address proof, timings, and other essential fields for organization registration and management.</p>
 *
 * <p>Validation constraints ensure that all necessary fields are provided, with specific validation for each attribute.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationDTO {

    /**
     * Basic information about the organization (e.g., name, address, region).
     * <p>This field is required and must not be null.</p>
     */
    @NotNull(message = "Basic information is required.")
    @Valid
    private BasicInformation basicInformation;

    /**
     * Contact information for the organization (e.g., mobile, email, landline).
     * <p>This field is required and must not be null.</p>
     */
    @NotNull(message = "Contact information is required.")
    @Valid
    private ContactInformation contactInformation;

    /**
     * Documents that need to be uploaded for the organization (e.g., business licenses, etc.).
     * <p>This field is required and must not be null.</p>
     */
    @NotNull(message = "Upload documents are required.")
    @Valid
    private UploadDocuments uploadDocuments;

    /**
     * Address proof details for the organization.
     * <p>At least one address proof document must be provided.</p>
     */
    @NotNull(message = "At least one address proof is required.")
    @Valid
    @JsonProperty("addAddressProof")
    private List<AddressProof> addressProof;

    /**
     * Timings for the organization (e.g., working hours, operational hours).
     * <p>This field is required and must not be null.</p>
     */
    @NotNull(message = "Organization timings are required.")
    @Valid
    @JsonProperty("facilityTimings")
    private List<OrganizationTimings> organizationTimings;

    /**
     * Organization's ownership and status details.
     * <p>This field is required and must not be null.</p>
     */
    @NotNull(message = "Organization details are required.")
    @Valid
    @JsonProperty("facilityDetails")
    private OrganizationDetails organizationDetails;

    /**
     * System of medicine used by the organization (e.g., allopathy, homeopathy, etc.).
     * <p>This field is required and must not be null.</p>
     */
    @NotNull(message = "System of medicine is required.")
    @Valid
    private SystemOfMedicine systemOfMedicine;

    /**
     * Inventory details for the organization (e.g., equipment, supplies).
     * <p>This field is required and must not be null.</p>
     */
    @NotNull(message = "Organization inventory details are required.")
    @Valid
    @JsonProperty("facilityInventory")
    private OrganizationInventory organizationInventory;

    /**
     * The unique account ID associated with the organization.
     * <p>This field is required and must not be null.</p>
     */
    @NotNull(message = "Account ID is required.")
    private String accountId;

    /**
     * The unique organization ID (facility ID).
     * <p>This field is optional and may be provided if available.</p>
     */
    @JsonProperty("facilityId")
    private String organizationId;

    /**
     * The internal ID for the organization.
     * <p>This field is optional and may be used for internal tracking.</p>
     */
    private String id;
}
