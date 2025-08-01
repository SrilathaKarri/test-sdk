package carestack.hpr.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a request to verify a One-Time Password (OTP) for Aadhaar authentication.
 * <p>
 * This DTO is used in the second step of the HPR registration workflow, containing
 * the transaction ID from the previous step, and the OTP entered by the user.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyAadhaarOtpRequest {

        /**
         * The domain name associated with the HPR ID being created (e.g., "@hpr.abdm").
         * This field is mandatory.
         */
        @NotBlank(message = "Domain name cannot be empty")
        private String domainName = "@hpr.abdm";

        /**
         * The type of identifier being used (e.g., "hprid"). This field is mandatory.
         */
        @NotBlank(message = "ID type cannot be empty")
        private String idType = "hpr_id";

        /**
         * The One-Time Password (OTP) sent to the user's Aadhaar-linked mobile number.
         * This field is mandatory.
         */
        @NotBlank(message = "OTP cannot be empty")
        private String otp;

        /**
         * Any restrictions or special conditions for the verification. This field is optional.
         */
        private String restrictions;

        /**
         * The unique transaction identifier (txnId) from the OTP generation step. This field is mandatory.
         */
        @NotBlank(message = "Transaction ID cannot be empty")
        private String txnId;
}
