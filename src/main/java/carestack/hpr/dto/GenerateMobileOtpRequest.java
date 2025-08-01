package carestack.hpr.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a request to generate a One-Time Password (OTP) and send it to a user's mobile number.
 * <p>
 * This DTO is used in the HPR workflow to verify a mobile number that may be different
 * from the one linked with Aadhaar.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateMobileOtpRequest {

    /**
     * The mobile number to which the OTP should be sent. This field is mandatory.
     */
    @NotBlank(message = "Mobile number cannot be empty")
    private String mobile;

    /**
     * The unique transaction identifier (txnId) from the registration workflow. This field is mandatory.
     */
    @NotBlank(message = "Transaction ID cannot be empty")
    private String txnId;
}