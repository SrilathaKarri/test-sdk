package carestack.hpr.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the response after successfully generating an Aadhaar OTP.
 * <p>
 * This DTO contains the transaction ID required for the next step (verification)
 * and a masked version of the mobile number to which the OTP was sent.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateAadhaarOtpResponse {

    /**
     * The unique transaction identifier (txnId) for this registration session.
     * This ID must be used in later steps of the workflow.
     */
    @JsonProperty("txnId")
    private String txnId;

    /**
     * The user's mobile number, masked for privacy (e.g., "xxxxxx1234").
     */
    @JsonProperty("mobileNumber")
    private String mobileNumber;
}