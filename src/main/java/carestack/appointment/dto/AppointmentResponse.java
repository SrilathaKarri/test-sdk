package carestack.appointment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents the standardized response from the appointment creation API.
 * <p>
 * This DTO encapsulates the outcome of an appointment creation request, including
 * success or error messages, validation details, and information about the
 * created resource.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentResponse {


    /**
     * The type of the response, typically indicating success or failure (e.g., "SUCCESS").
     */
    private String type;

    /**
     * A human-readable message describing the result of the operation.
     */
    private String message;

    /**
     * A list of validation errors, if any, occurred during the request processing.
     * This field is only included in the response if there are errors.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Object> validationErrors;


    /**
     * Contains details of the resource that was part of the request,
     * such as patient and practitioner references.
     */
    private ResourceType requestResource;

    /**
     * The FHIR profile ID associated with the created appointment.
     * This field is transient and not serialized into the JSON response.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private transient String fhirProfileId;
}
