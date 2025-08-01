package carestack.organization.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * Data Transfer Object (DTO) for representing the system of medicine in an organization.
 * <p>This class is used to capture the specialties, organization type, and service type associated with the organization.</p>
 */
@Data
public class SystemOfMedicine {

    /**
     * A list of specialities offered by the organization under the given system of medicine.
     * <p>This field represents the various specialties (e.g., Cardiology, Neurology) available in the organization for a given system of medicine.</p>
     * <p>This field is required and should contain at least one speciality.</p>
     */
    @NotNull
    private List<Speciality> specialities;

    /**
     * The type of the organization.
     * <p>This field is used to specify the type of the organization (e.g., hospital, clinic, diagnostic center).</p>
     * <p>This field is required.</p>
     */
    @NotNull
    @JsonProperty("facilityType")
    private String organizationType;

    /**
     * The subtype of the organization.
     * <p>This field is used to specify the specific subtype of the organization (e.g., private, government, etc.).</p>
     * <p>This field is required.</p>
     */
    @NotNull
    @JsonProperty("facilitySubType")
    private String organizationSubType;

    /**
     * The type of services offered by the organization.
     * <p>This field defines the service type provided by the organization, such as outpatient, inpatient, emergency care, etc.</p>
     * <p>This field is required.</p>
     */
    @NotNull
    private String serviceType;

}
