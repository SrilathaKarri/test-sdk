package carestack.abha.enums;

import carestack.abha.DTO.CreateAbhaAddressRequestDto;
import carestack.abha.DTO.UpdateMobile;
import carestack.abha.DTO.VerifyAadhaarOtpDto;
import carestack.hpr.dto.GenerateAadhaarOtpRequest;
import carestack.hpr.dto.IdSuggestionRequest;
import carestack.hpr.dto.VerifyMobileOtpRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Defines the sequential steps for the ABHA (Ayushman Bharat Health Account) registration and management workflow.
 * <p>
 * This enum acts as a state machine, guiding the flow of operations within the ABHA service layer.
 * Each constant represents a distinct API call or user action required to create or update an ABHA profile.
 * The {@link #fromValue(String)} method is used to map external string inputs to these defined steps.
 *
 * @see carestack.abha.AbhaService The service class that orchestrates this workflow.
 */
@Getter
@RequiredArgsConstructor
public enum AbhaSteps {

    /**
     * Initiates the registration process by generating an OTP for the provided Aadhaar number.
     * Corresponds to step "1".
     * <p>
     * <b>Expected Payload DTO:</b> {@link GenerateAadhaarOtpRequest}
     */
    register_with_aadhaar("1"),

    /**
     * Verifies the Aadhaar OTP to authenticate the user and retrieve their demographic data.
     * Corresponds to step "2".
     * <p>
     * <b>Expected Payload DTO:</b> {@link VerifyAadhaarOtpDto}
     */
    verify_aadhaar_otp("2"),

    /**
     * Allows the user to provide a mobile number for the ABHA account, which may differ from the
     * Aadhaar-linked number. This step generates an OTP to the new mobile number.
     * Corresponds to step "3".
     * <p>
     * <b>Expected Payload DTO:</b> {@link UpdateMobile}
     */
    update_mobile("3"),

    /**
     * Verifies the OTP sent to the new mobile number, confirming its ownership.
     * Corresponds to step "4".
     * <p>
     * <b>Expected Payload DTO:</b> {@link VerifyMobileOtpRequest}
     */
    verify_update_mobile_otp("4"),

    /**
     * Fetches a list of available ABHA address (username) suggestions based on the user's verified name.
     * Corresponds to step "5".
     * <p>
     * <b>Expected Payload DTO:</b> {@link IdSuggestionRequest}
     */
    get_abha_address_suggestions("5"),

    /**
     * Completes the registration by submitting the chosen ABHA address and creating the final account.
     * Corresponds to step "6".
     * <p>
     * <b>Expected Payload DTO:</b> {@link CreateAbhaAddressRequestDto}
     */
    final_register("6");

    /**
     * The unique string identifier for the workflow step, used for routing requests.
     */
    private final String value;

    /**
     * A static map for efficient lookups of enum constants by their string value.
     */
    private static final Map<String, AbhaSteps> valueMap =
            Arrays.stream(values()).collect(Collectors.toMap(AbhaSteps::getValue, Function.identity()));

    /**
     * Converts a string identifier to the corresponding {@link AbhaSteps} enum constant.
     * <p>
     * This method provides a safe and efficient way to look up a step from an incoming request parameter.
     *
     * @param value The string identifier for the step (e.g., "1", "2").
     * @return The corresponding {@link AbhaSteps} enum constant.
     * @throws IllegalArgumentException if the provided value does not map to any known step.
     */
    public static AbhaSteps fromValue(String value) {
        AbhaSteps step = valueMap.get(value);
        if (step != null) {
            return step;
        }
        try {
            return AbhaSteps.valueOf(value.toLowerCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Abha step value: " + value + ". Valid values are: " +
                    Arrays.toString(AbhaSteps.values()) + " or numeric values: " + valueMap.keySet());
        }
    }

}