package carestack.patient.abha.DTO;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for verifying an Aadhaar OTP in the ABHA registration flow.
 * This class contains the necessary information required to verify the OTP sent to the user's mobile
 * during the Aadhaar verification process.
 * It ensures that the required fields are not null or empty.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class VerifyAadhaarOtpDto {

    /**
     * The OTP (One-Time Password) received on the user's mobile for Aadhaar verification.
     * This OTP needs to be submitted to verify the user's Aadhaar.
     * <p>
     * This field is mandatory and cannot be null or empty.
     */
    @NotNull
    @NotEmpty
    private String otp;

    /**
     * The transaction ID (txnId) associated with the Aadhaar OTP verification request.
     * This ID links the OTP verification request to a specific transaction, ensuring consistency
     * between the OTP and the request.
     * <p>
     * This field is mandatory and cannot be null or empty.
     */
    @NotNull
    @NotEmpty
    private String txnId;

    /**
     * The mobile number associated with the Aadhaar account for which the OTP is being verified.
     * This mobile number should be the same as the one linked to the Aadhaar.
     * <p>
     * This field is mandatory and cannot be null or empty.
     */
    @NotNull
    @NotEmpty
    private String mobile;
}
