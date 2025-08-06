package carestack.patient.abha.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import carestack.patient.abha.enums.AbhaSteps;

/**
 * A DTO class that represents the response for each step in the ABHA (Aadhaar-based Health Account) registration flow.
 * It contains information about the outcome of each step, the next step in the flow, and any hints for the next step.
 * This class is used to standardize the response format for ABHA-related operations.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AbhaFlowResponse {

    /**
     * The message providing information about the current step or outcome.
     */
    private String message;

    /**
     * The data associated with the response. This could include information such as the transaction ID,
     * ABHA profile data, etc., depending on the step.
     */
    private Object data;

    /**
     * The next step in the ABHA registration flow after the current step has been completed.
     * This is represented by the {@link AbhaSteps} enum.
     */
    private AbhaSteps nextStep;

    /**
     * A hint or description of the payload required for the next step in the ABHA registration flow.
     * It provides guidance to the user or system on what should be provided next.
     */
    private String nextStepPayloadHint;

    /**
     * A hint or description of the payload DTO required for the next step in the ABHA registration flow.
     * It provides guidance to the user or system on what should be provided next.
     */
    private String nextStepPayloadDTO;

    /**
     * A utility method to construct a successful response with a message, data, next step, and hint.
     *
     * @param message The message describing the success or outcome of the current step.
     * @param data The data associated with the successful step (e.g., transaction ID, user data).
     * @param nextStep The next step to take in the ABHA flow.
     * @param nextStepPayloadHint A hint for the next step's payload.
     * @return A successful {@link AbhaFlowResponse} object containing the provided information.
     *
     */
    public static AbhaFlowResponse success(String message, Object data, AbhaSteps nextStep, String nextStepPayloadHint,String nextStepPayloadDTO) {
        return new AbhaFlowResponse(message, data, nextStep, nextStepPayloadHint, nextStepPayloadDTO);
    }

    /**
     * A utility method to construct an error response with a message.
     *
     * @param message The error message explaining what went wrong in the current step.
     * @return An error {@link AbhaFlowResponse} object with no data, next step, or hint.
     */
    public static AbhaFlowResponse error(String message) {
        return new AbhaFlowResponse(message, null, null, null, null);
    }

}
