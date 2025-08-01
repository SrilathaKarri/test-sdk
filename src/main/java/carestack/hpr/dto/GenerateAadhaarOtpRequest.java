package carestack.hpr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a request to generate a One-Time Password (OTP) for Aadhaar verification.
 * <p>
 * This DTO is the first step in the HPR registration workflow and also
 * the ABHA creation workflow containing the user's
 * 12-digit Aadhaar number.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateAadhaarOtpRequest {

    /**
     * The user's 12-digit Aadhaar number. This field is mandatory and must be exactly 12 digits.
     */
    @NotBlank(message = "Aadhaar cannot be empty")
    @Pattern(regexp = "\\d{12}", message = "Aadhaar must be exactly 12 digits")
    private String aadhaar;
}