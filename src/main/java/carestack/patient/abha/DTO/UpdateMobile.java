package carestack.patient.abha.DTO;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for updating a mobile number in the ABHA registration flow.
 * This class contains the information necessary to request an update to the user's mobile number.
 * It includes validation annotations to ensure the required fields are not null or empty.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateMobile {

        /**
         * The value to which the mobile number should be updated.
         * This field contains the new mobile number or other data to be updated.
         * <p>
         * This field is mandatory and cannot be null or empty.
         */
        @NotNull
        @NotEmpty
        private String updateValue;

        /**
         * The transaction ID (txnId) associated with the update request.
         * This ID ensures that the mobile update request is linked to a specific transaction.
         * <p>
         * This field is mandatory and cannot be null or empty.
         */
        @NotNull
        @NotEmpty
        private String txnId;

}
