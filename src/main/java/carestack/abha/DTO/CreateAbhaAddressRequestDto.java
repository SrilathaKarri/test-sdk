package carestack.abha.DTO;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for creating an ABHA address request.
 * This class is used to hold the necessary information required to create a new ABHA address.
 * It includes validation annotations to ensure the required fields are not null or empty.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CreateAbhaAddressRequestDto {

    /**
     * The ABHA address that is being requested for registration or update.
     * This is a unique identifier tied to the user's Aadhaar-based health account.
     * This field is mandatory and cannot be null or empty.
     */
    @NotNull
    @NotEmpty
    private String abhaAddress;

    /**
     * The transaction ID (txnId) that was generated during a previous step in the ABHA registration process.
     * This ID is used to link the current request to its corresponding transaction and ensure consistency.
     * This field is mandatory and cannot be null or empty.
     */
    @NotNull
    @NotEmpty
    private String txnId;

}
