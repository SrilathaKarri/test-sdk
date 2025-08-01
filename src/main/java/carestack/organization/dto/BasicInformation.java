package carestack.organization.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for representing basic organization information.
 * Contains fields such as the organization's name, region, address, and geographic coordinates (lat-long).
 */
@Data
@NoArgsConstructor
public class BasicInformation {

    /**
     * The name of the organization/facility.
     * This field is required and must be between 5 and 100 characters long.
     * The name is mapped to the JSON property "facilityName".
     */
    @NotNull
    @NotBlank(message = "Organization name is required")
    @Size(min = 5, max = 100, message = "Organization name must be at least 5 characters long")
    @JsonProperty("facilityName")
    private String organizationName;

    /**
     * The region where the organization is located.
     * This field is required and cannot be null.
     */
    @NotNull(message = "Region is required")
    private String region;

    /**
     * The first line of the organization's address.
     * This field is required and must be at least 5 characters long.
     * It should follow a specific address pattern (e.g., alphanumeric characters, spaces, commas, and hyphens).
     */
    @NotNull
    @NotBlank(message = "Address is required")
    @Size(min = 5, message = "Address must be at least 5 characters long")
    @Pattern(regexp = "^[A-Za-z0-9\\s,-]+$",
            message = "Invalid address format")
    private String addressLine1;

    /**
     * The second line of the organization's address.
     * This field is required and must be at least 5 characters long.
     * It should follow a specific address pattern (e.g., alphanumeric characters, spaces, commas, and hyphens).
     */
    @NotNull
    @NotBlank(message = "Address is required")
    @Size(min = 5, message = "Address must be at least 5 characters long")
    @Pattern(regexp = "^[A-Za-z0-9\\s,-]+$",
            message = "Invalid address format")
    private String addressLine2;

    /**
     * The state where the organization is located.
     * This field is required and cannot be null.
     */
    @NotNull(message = "State is required")
    private String state;

    /**
     * The district where the organization is located.
     * This field is required and cannot be null.
     */
    @NotNull
    @NotBlank(message = "District is required")
    private String district;

    /**
     * The subdistrict where the organization is located.
     * This field is required and cannot be null.
     */
    @NotNull
    @NotBlank(message = "Sub district is required")
    private String subDistrict;

    /**
     * The city where the organization is located.
     * This field is required and cannot be null.
     */
    @NotNull
    @NotBlank(message = "City is required")
    private String city;

    /**
     * The country where the organization is located.
     * This field is required and cannot be null.
     */
    @NotNull
    @NotBlank(message = "Country is required")
    private String country;

    /**
     * The pin code of the organization's location.
     * This field is required, must be 6 digits, and cannot be null.
     */
    @NotNull
    @NotBlank(message = "Pincode is required")
    @Pattern(regexp = "\\d{6}", message = "Pincode must be 6 digits")
    private String pincode;

    /**
     * The list of geographic coordinates (latitude and longitude) for the organization.
     * The list must contain exactly two elements (lat, long) and cannot be empty.
     */
    @NotEmpty(message = "LatLongs list cannot be empty")
    @Size(min = 2, message = "LatLongs list must contain two elements")
    private List<String> latLongs;
}
