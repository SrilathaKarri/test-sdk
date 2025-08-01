package carestack.organization.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO for representing an Address Proof.
 * This class is used to transfer the information related to an address proof, including its type and the associated attachment.
 */
@Data
@JsonTypeName("AddAddressProof")
public class AddressProof {

    /**
     * The type of address proof (e.g., utility bill, voter ID, etc.).
     * This field is required and cannot be null or blank.
     */
    @NotNull
    @NotBlank(message = "Address Proof type is required")
    private String addressProofType;

    /**
     * The address proof attachment (document), such as an image or PDF file.
     * This field is required and cannot be null.
     */
    @NotNull
    private Document addressProofAttachment;

}
