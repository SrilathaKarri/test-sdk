package carestack.practitioner.hpr.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import carestack.practitioner.hpr.enums.HprSteps;

/**
 * An internal helper DTO to manage the state and data between steps in the HPR registration workflow.
 * <p>
 * This class is not intended for direct use by the SDK consumer. It standardizes the
 * response structure within the {@link carestack.practitioner.hpr.Hpr} service, carrying the result data,
 * a user-friendly message, the next logical step, and a hint for the next step's payload.
 * </p>
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class HprFlowResponse {
    /**
     * A human-readable message describing the result of the last executed step.
     */
    private String message;

    /**
     * The data returned from the last executed step's API call.
     */
    private Object data;

    /**
     * The next step to be performed in the HPR registration flow. Can be null if the flow is complete.
     */
    private HprSteps nextStep;

    /**
     * A user-friendly hint describing the payload required for the next step.
     */
    private String nextStepPayloadHint;

    /**
     * A user-friendly hint describing the request dto required for the next step.
     */
    private String nextStepRequestDTO;

    /**
     * Factory method to create a successful flow response.
     *
     * @param message             A success message.
     * @param data                The result data.
     * @param nextStep            The next step in the flow.
     * @param nextStepPayloadHint A hint for the next step's payload.
     * @return A new {@link HprFlowResponse} instance.
     */
    public static HprFlowResponse success(String message, Object data, HprSteps nextStep, String nextStepPayloadHint, String nextStepRequestDTO) {
        return new HprFlowResponse(message, data, nextStep, nextStepPayloadHint, nextStepRequestDTO);
    }

    /**
     * Factory method to create an error flow response.
     *
     * @param message The error message.
     * @return A new {@link HprFlowResponse} instance representing an error state.
     */
    public static HprFlowResponse error(String message) {
        return new HprFlowResponse(message, null, null, null,null);
    }
}