package carestack.hpr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a request to check for the existence of a Healthcare Professional Registry (HPR) account.
 * <p>
 * This DTO is used in the HPR registration workflow to determine if an account
 * already exists for a user associated with a specific transaction.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckAccountRequest {

    /**
     * The unique transaction identifier (txnId) obtained from a previous step,
     * such as Aadhaar OTP verification. This field is mandatory.
     */
    @NotBlank(message = "Transaction ID cannot be empty")
    private String txnId;

    /**
     * A flag indicating that the check is being performed with pre-verified data.
     * This field is mandatory.
     */
    @NotNull(message = "Preverified check cannot be null")
    private Boolean preverifiedCheck;
}