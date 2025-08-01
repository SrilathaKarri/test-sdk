package carestack.documentLinking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object for updating visit records with new health information.
 * <p>
 * This DTO is used in the document linking workflow to add structured health records
 * (like an OP Consultation) to an existing care context. It carries all the necessary
 * references to link the new information correctly.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateVisitRecordsDTO {

    /**
     * The unique reference for the care context being updated. This field is mandatory.
     */
    @NotBlank
    @JsonProperty("careContextReference")
    private String careContextReference;

    /**
     * The unique reference for the patient. This field is mandatory.
     */
    @NotBlank
    @JsonProperty("patientReference")
    private String patientReference;

    /**
     * The unique reference for the practitioner. This field is mandatory.
     */
    @NotBlank
    @JsonProperty("practitionerReference")
    private String practitionerReference;

    /**
     * The unique reference for the appointment associated with the visit. This field is mandatory.
     */
    @NotBlank
    @JsonProperty("appointmentReference")
    private String appointmentReference;

    /**
     * The patient's ABHA (Ayushman Bharat Health Account) address. This field is optional.
     */
    @JsonProperty("patientAbhaAddress")
    private String patientAbhaAddress;

    /**
     * A list of health records to be added to the visit. Each item is a DTO
     * representing a specific type of health information. This list cannot be empty.
     */
    @NotEmpty
    @Valid
    @JsonProperty("healthRecords")
    private List<HealthInformationDTO> healthRecords;

    /**
     * The patient's mobile number. This field is optional.
     */
    @JsonProperty("mobileNumber")
    private String mobileNumber;

    /**
     * The unique identifier for the transaction request. This field is optional.
     */
    @JsonProperty("requestId")
    private String requestId;
}