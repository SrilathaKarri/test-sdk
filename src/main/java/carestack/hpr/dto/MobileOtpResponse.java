package carestack.hpr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a response from mobile OTP (One-Time Password) operations.
 * <p>
 * This DTO is used for both generating and verifying mobile OTPs. It contains the
 * transaction ID and, upon successful verification, the masked mobile number.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileOtpResponse {

    /**
     * The unique transaction identifier (txnId) for the session.
     */
    private String txnId;

    /**
     * The user's mobile number, masked for privacy (e.g., "xxxxxx1234").
     * This is typically populated after successful verification.
     */
    private String mobileNumber;
}