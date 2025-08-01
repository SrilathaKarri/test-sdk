package carestack.hpr.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a request to verify a One-Time Password (OTP) sent to a user's mobile number.
 * <p>
 * This DTO is used in the HPR workflow and ABHA workflow to confirm ownership of a mobile number by
 * submitting the OTP that was sent to it.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyMobileOtpRequest {

    /**
     * The One-Time Password (OTP) that was sent to the user's mobile number.
     * This field is mandatory.
     */
    @NotBlank(message = "OTP cannot be empty")
    private String otp;

    /**
     * The unique transaction identifier (txnId) from the registration workflow. This field is mandatory.
     */
    @NotBlank(message = "Transaction ID cannot be empty")
    private String txnId;
}