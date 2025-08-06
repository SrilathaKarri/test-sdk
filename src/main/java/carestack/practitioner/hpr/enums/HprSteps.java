package carestack.practitioner.hpr.enums;

import carestack.practitioner.hpr.dto.*;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Defines the sequential steps for the HPR (Healthcare Professional Registry) registration workflow.
 * <p>
 * This enum acts as a state machine, guiding the flow of operations within the HPR service layer.
 * Each constant represents a distinct API call or user action required to create an HPR ID.
 * The {@link #fromValue(String)} method is used to map external string inputs to these defined steps.
 *
 * @see carestack.practitioner.hpr.Hpr The service class that orchestrates this workflow.
 */
@Getter
@RequiredArgsConstructor
public enum HprSteps {

    /**
     * Step 1: Initiates the registration by generating an OTP for the provided Aadhaar number.
     * Corresponds to value "1".
     * <p>
     * <b>Expected Payload DTO:</b> {@link GenerateAadhaarOtpRequest}
     */
    generate_aadhaar_otp("1"),

    /**
     * Step 2: Verifies the Aadhaar OTP to authenticate the user and retrieve their demographic data.
     * Corresponds to value "2".
     * <p>
     * <b>Expected Payload DTO:</b> {@link VerifyAadhaarOtpRequest}
     */
    verify_aadhaar_otp("2"),

    /**
     * Step 3: Checks if an HPR account already exists for the authenticated user.
     * Corresponds to value "3".
     * <p>
     * <b>Expected Payload DTO:</b> {@link CheckAccountRequest}
     */
    check_account_exists("3"),

    /**
     * Step 4: Performs demographic authentication using the user's mobile number.
     * Corresponds to value "4".
     * <p>
     * <b>Expected Payload DTO:</b> {@link DemographicAuthViaMobileRequest}
     */
    demographic_auth_via_mobile("4"),

    /**
     * Step 5: Generates an OTP to a user-provided mobile number to verify it.
     * Corresponds to value "5".
     * <p>
     * <b>Expected Payload DTO:</b> {@link GenerateMobileOtpRequest}
     */
    generate_mobile_otp("5"),

    /**
     * Step 6: Verifies the OTP sent to the new mobile number, confirming its ownership.
     * Corresponds to value "6".
     * <p>
     * <b>Expected Payload DTO:</b> {@link VerifyMobileOtpRequest}
     */
    verify_mobile_otp("6"),

    /**
     * Step 7: Fetches a list of available HPR ID (username) suggestions.
     * Corresponds to value "7".
     * <p>
     * <b>Expected Payload DTO:</b> {@link IdSuggestionRequest}
     */
    get_hpr_id_suggestions("7"),

    /**
     * Step 8: Completes the registration by submitting all details and creating the final HPR ID.
     * Corresponds to value "8".
     * <p>
     * <b>Expected Payload DTO:</b> {@link CreateHprIdWithPreVerifiedRequest}
     */
    create_hpr("8");

    /**
     * The unique string identifier for the workflow step. This value is used for routing requests
     * and is also the value that will be serialized in JSON responses.
     */
    @JsonValue
    private final String value;

    /**
     * A static map for efficient, constant-time lookups of enum constants by their string value.
     */
    private static final Map<String, HprSteps> valueMap =
            Arrays.stream(values()).collect(Collectors.toMap(HprSteps::getValue, Function.identity()));

    /**
     * Converts a string identifier to the corresponding {@link HprSteps} enum constant.
     * <p>
     * This method provides a safe and efficient way to look up a step from an incoming request parameter.
     *
     * @param value The string identifier for the step (e.g., "1", "2").
     * @return The corresponding {@link HprSteps} enum constant.
     * @throws IllegalArgumentException if the provided value does not map to any known step.
     */
    public static HprSteps fromValue(String value) {
        HprSteps step = valueMap.get(value);
        if (step == null) {
            throw new IllegalArgumentException("Invalid Hpr step value: " + value + ". Valid values are: " + valueMap.keySet());
        }
        return step;
    }
}