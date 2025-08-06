package carestack.practitioner.hpr.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the response from a demographic authentication request via mobile.
 * <p>
 * This DTO simply indicates whether the provided mobile number was successfully
 * verified against the details associated with the transaction.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemographicAuthViaMobileResponse {
    /**
     * A boolean flag that is {@code true} if the demographic authentication was successful,
     * and {@code false} otherwise.
     */
    private boolean verified;
}