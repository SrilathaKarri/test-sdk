package carestack.organization.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * Data Transfer Object representing the inventory and facilities of an organization.
 * <p>This class encapsulates the details about an organization's medical and healthcare facilities, such as ventilators, beds, dialysis center, pharmacy, blood bank, etc.</p>
 * <p>It also contains various IDs related to state and central government schemes and health programs.</p>
 *
 * <p>Validation constraints ensure that all necessary fields are provided, with appropriate value checks (e.g., non-negative numbers).</p>
 */
@Data
public class OrganizationInventory {

    /**
     * Total number of ventilators available in the organization.
     * <p>This field cannot be negative, and must be provided.</p>
     */
    @NotNull
    @Min(value = 0, message = "Total number of ventilators cannot be negative")
    private int totalNumberOfVentilators;

    /**
     * Total number of beds available in the organization.
     * <p>This field cannot be negative, and must be provided.</p>
     */
    @NotNull
    @Min(value = 0, message = "Total number of beds cannot be negative")
    private int totalNumberOfBeds;

    /**
     * Indicates whether the organization has a dialysis center.
     * <p>This field cannot be null and must be provided as a string (e.g., "Yes" or "No").</p>
     */
    @NotNull
    private String hasDialysisCenter;

    /**
     * Indicates whether the organization has a pharmacy.
     * <p>This field cannot be null and must be provided as a string (e.g., "Yes" or "No").</p>
     */
    @NotNull
    private String hasPharmacy;

    /**
     * Indicates whether the organization has a blood bank.
     * <p>This field cannot be null and must be provided as a string (e.g., "Yes" or "No").</p>
     */
    @NotNull
    private String hasBloodBank;

    /**
     * Indicates whether the organization has a catheterization lab (Cath Lab).
     * <p>This field cannot be null and must be provided as a string (e.g., "Yes" or "No").</p>
     */
    @NotNull
    private String hasCathLab;

    /**
     * Indicates whether the organization has a diagnostic lab.
     * <p>This field cannot be null and must be provided as a string (e.g., "Yes" or "No").</p>
     */
    @NotNull
    private String hasDiagnosticLab;

    /**
     * A list of services provided by the organization's imaging center.
     * <p>This field cannot be null and must contain details about the imaging services offered.</p>
     */
    @NotNull
    private List<ImagingCenterServiceType> servicesByImagingCenter;

    /**
     * National Health Resource Registry ID.
     * <p>This field cannot be null and must be provided as a string.</p>
     */
    @NotNull
    private String nhrrid;

    /**
     * National Identification Number (NIN) of the organization.
     * <p>This field cannot be null and must be provided as a string.</p>
     */
    @NotNull
    private String nin;

    /**
     * Ayushman Bharat Pradhan Mantri Jan Arogya Yojana (PMJAY) ID.
     * <p>This field cannot be null and must be provided as a string.</p>
     */
    @NotNull
    private String abpmjayid;

    /**
     * Rohini ID of the organization.
     * <p>This field cannot be null and must be provided as a string.</p>
     */
    @NotNull
    private String rohiniId;

    /**
     * Ex-servicemen Contributory Health Scheme (ECHS) ID.
     * <p>This field cannot be null and must be provided as a string.</p>
     */
    @NotNull
    private String echsId;

    /**
     * Central Government Health Scheme (CGHS) ID.
     * <p>This field cannot be null and must be provided as a string.</p>
     */
    @NotNull
    private String cghsId;

    /**
     * CEA (Central Electricity Authority) Registration ID.
     * <p>This field cannot be null and must be provided as a string.</p>
     */
    @NotNull
    private String ceaRegistration;

    /**
     * State Insurance Scheme ID.
     * <p>This field cannot be null and must be provided as a string.</p>
     */
    @NotNull
    private String stateInsuranceSchemeId;

}
