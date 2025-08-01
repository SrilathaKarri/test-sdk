package carestack.hpr.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a request to get a list of suggested HPR/Abha IDs (usernames).
 * <p>
 * This DTO is used in the HPR registration workflow and Abha registration workflow after the user's demographic
 * data has been verified, to fetch available HPR/abha IDs based on their name.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdSuggestionRequest {

    /**
     * The unique transaction identifier (txnId) from the registration workflow. This field is mandatory.
     */
    @NotBlank(message = "Transaction ID cannot be empty")
    private String txnId;
}