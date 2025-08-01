package carestack.hpr.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a request to perform demographic authentication using a mobile number.
 * <p>
 * This DTO is used in the HPR registration workflow to verify that the mobile number
 * provided by the user matches the one associated with their Aadhaar details for the
 * given transaction.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DemographicAuthViaMobileRequest {

    /**
     * The unique transaction identifier (txnId) from the registration workflow. This field is mandatory.
     */
    @NotBlank(message = "Transaction ID cannot be empty")
    private String txnId;

    /**
     * The user's mobile number to be verified against the Aadhaar-linked mobile number.
     * This field is mandatory.
     */
    @NotBlank(message = "Mobile number cannot be empty")
    private String mobileNumber;
}