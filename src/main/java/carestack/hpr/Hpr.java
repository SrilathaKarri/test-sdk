package carestack.hpr;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import carestack.base.Base;
import carestack.base.errors.EhrApiError;
import carestack.base.utils.Constants;
import carestack.base.utils.LogUtil;
import carestack.hpr.dto.*;
import carestack.hpr.enums.HprSteps;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Provides a comprehensive service for the Healthcare Professional Registry (HPR) registration workflow.
 * <p>
 * This class acts as a high-level facade for all HPR-related operations. It offers two main ways of interaction:
 * <ol>
 *   <li><b>State Machine:</b> Using the {@link #createHPR(HprSteps, Object)} method, which guides the developer through the
 *   multi-step registration process, from Aadhaar OTP to final HPR ID creation.</li>
 *   <li><b>Direct Method Calls:</b> Each step of the workflow is also exposed as a public method (e.g.,
 *   {@link #generateAadhaarOtp(GenerateAadhaarOtpRequest)}, {@link #verifyAadhaarOtp(VerifyAadhaarOtpRequest)})
 *   for more granular control.</li>
 * </ol>
 * All methods are built on Project Reactor, returning {@link Mono} objects for asynchronous, non-blocking execution.
 * They include built-in request validation and centralized error handling.
 *
 * @see HprSteps For the definition of each step in the registration flow.
 */
@Service
public class Hpr extends Base {

    private final Validator validator;

    /**
     * Constructor for HPR Service.
     *
     * @param objectMapper Jackson ObjectMapper for JSON processing
     * @param webClient WebClient for HTTP operations
     * @param validator Bean Validator for request validation
     */
    public Hpr(ObjectMapper objectMapper, WebClient webClient, Validator validator) {
        super(objectMapper, webClient);
        this.validator = validator;
    }

    /**
     * <h3>HPR Registration State Machine (Enum-based)</h3>
     * Main entry point for the HPR registration flow. It routes the request to the appropriate handler
     * based on the {@link HprSteps} enum value, acting as a state machine.
     *
     * @param step    The {@link HprSteps} enum constant representing the current step in the workflow.
     * @param payload The input data for the specified step, typically a Map or a DTO.
     * @return A {@link Mono} emitting a {@link Map} that contains the result of the operation. The map includes
     * a message, the response data, the next step to perform, and a hint for the next step's payload.
     */
    public Mono<Map<String, Object>> createHPR(HprSteps step, Object payload) {
        try {
            return switch (step) {
                case generate_aadhaar_otp -> handleGenerateAadhaarOtp(payload).map(this::buildResponseMap);
                case verify_aadhaar_otp -> handleVerifyAadhaarOtp(payload).map(this::buildResponseMap);
                case check_account_exists -> handleCheckAccountExist(payload).map(this::buildResponseMap);
                case demographic_auth_via_mobile -> handleDemographicAuthViaMobile(payload).map(this::buildResponseMap);
                case generate_mobile_otp -> handleGenerateMobileOtp(payload).map(this::buildResponseMap);
                case verify_mobile_otp -> handleVerifyMobileOtp(payload).map(this::buildResponseMap);
                case get_hpr_id_suggestions -> handleGetHprSuggestion(payload).map(this::buildResponseMap);
                case create_hpr -> handleCreateHprId(payload).map(this::buildResponseMap);
                // The default case is technically unreachable if HprSteps is exhaustive, but good for safety.
                default -> Mono.just(Map.of("error", "Invalid step in HPR ID creation flow"));
            };
        } catch (Exception e) {
            LogUtil.logger.error("Error in HPR flow for step: {}", step, e);
            return Mono.just(Map.of("error", "Internal error: " + e.getMessage()));
        }
    }

    /**
     * <h3>HPR Registration State Machine (String-based)</h3>
     * A convenience overload that accepts a string representation of the step (e.g., "1", "2") and
     * delegates to the enum-based {@link #createHPR(HprSteps, Object)} method.
     *
     * <h3>Input and Validation:</h3>
     * <ul>
     *     <li><b>step (String):</b> The numeric string identifier for the step (e.g., "1" for Aadhaar OTP generation).
     *     If the string does not correspond to a valid step, the method returns an error response.</li>
     *     <li><b>payload (Object):</b> The input data for the step. This is typically a {@code Map<String, Object>}
     *     from a JSON request body.</li>
     * </ul>
     *
     * <h3>Output:</h3>
     * @return A {@link Mono} emitting a {@link Map} that contains the result of the operation. On success, this
     * includes a message, data, the next step, and a payload hint. On failure (e.g., invalid step), it
     * contains an "error" key.
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * Map<String, String> payload = new HashMap<>();
     * payload.put("aadhaar", "123456789012");
     *
     * hprService.createHprByStepName("1", payload)
     *     .subscribe(response -> {
     *         System.out.println("Message: " + response.get("message"));
     *         System.out.println("Next Step: " + response.get("next_step"));
     *         // The data object will contain the txnId
     *         System.out.println("Data: " + response.get("data"));
     *     });
     * }</pre>
     *
     * @param step    The step name as a string (e.g., "1", "2").
     * @param payload The payload for the step.
     * @return A response map with a message, data, and next step info, or an error map.
     */
    public Mono<Map<String, Object>> createHprByStepName(String step, Object payload) {
        try {
            HprSteps hprStep = HprSteps.fromValue(step);
            return createHPR(hprStep, payload);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid step: " + step);
            return Mono.just(errorResponse);
        }
    }

    /**
     * Builds the response map from {@link HprFlowResponse}.
     *
     * @param response HprFlowResponse object
     * @return A map suitable for HTTP JSON response
     */
    // Helper method to build the response map with all the necessary fields
    private Map<String, Object> buildResponseMap(HprFlowResponse response) {
        Map<String, Object> result = new HashMap<>();
        result.put("message", response.getMessage());
        result.put("data", response.getData());
        result.put("next_step", response.getNextStep());
        result.put("next_step_payload_hint", response.getNextStepPayloadHint());
        result.put("next_step_payload_dto",response.getNextStepRequestDTO());
        return result;
    }

    /**
     * Validates any request DTO using Bean Validation.
     *
     * @param requestData DTO to validate
     * @param <T> DTO type
     * @return Validated DTO
     * @throws EhrApiError if validation fails
     */
    private <T> T validateRequest(T requestData) {
        Set<ConstraintViolation<T>> violations = validator.validate(requestData);
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            LogUtil.logger.error("Validation failed: {}", errorMessage);
            throw new EhrApiError("Validation failed: " + errorMessage, HttpStatusCode.valueOf(400));
        }
        return requestData;
    }

    /**
     * <h3>Step 1: Generate Aadhaar OTP</h3>
     * Initiates the HPR registration workflow by generating a One-Time Password (OTP) and sending it to the
     * mobile number linked with the provided Aadhaar number.
     *
     * <h3>Input and Validation:</h3>
     * The method accepts a {@link GenerateAadhaarOtpRequest} object which contains:
     * <ul>
     *     <li><b>Aadhaar:</b> A 12-digit Aadhaar number as a String. This field is mandatory and is validated
     *     by the {@code @Pattern(regexp = "\\d{12}")} annotation to ensure it contains exactly 12 digits.</li>
     * </ul>
     * If validation fails, the method returns a {@code Mono.error} with an {@link EhrApiError}.
     *
     * <h3>API Request:</h3>
     * This method makes a <b>POST</b> request to the {@code /v1/hpr/registration/aadhaar/generateOtp} endpoint.
     *
     * <h3>Output:</h3>
     * @return A {@link Mono} that emits a {@link GenerateAadhaarOtpResponse} upon success. This response contains:
     * <ul>
     *     <li><b>txnId:</b> A unique transaction ID for the entire registration session. This ID is crucial and must be passed in all subsequent steps.</li>
     *     <li><b>mobileNumber:</b> A masked version of the mobile number to which the OTP was sent (e.g., "xxxxxx1234").</li>
     * </ul>
     *
     * <h3>Error Handling:</h3>
     * Returns a {@code Mono.error} with a 400-level {@link EhrApiError} if request validation fails, or a 500-level error if the remote API call fails.
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * GenerateAadhaarOtpRequest request = new GenerateAadhaarOtpRequest("123456789012");
     *
     * hprService.generateAadhaarOtp(request)
     *     .subscribe(
     *         response -> {
     *             System.out.println("Successfully generated OTP. Transaction ID: " + response.getTxnId());
     *             System.out.println("OTP sent to masked mobile: " + response.getMobileNumber());
     *         },
     *         error -> System.err.println("Error generating OTP: " + error.getMessage())
     *     );
     *
     * // Expected Output (example):
     * // Successfully generated OTP. Transaction ID: txn-abc-123
     * // OTP sent to masked mobile: xxxxxx1234
     * }</pre>
     *
     * @param request DTO containing the 12-digit Aadhaar number.
     * @return A {@link Mono} emitting a {@link GenerateAadhaarOtpResponse} upon success.
     */
    public Mono<GenerateAadhaarOtpResponse> generateAadhaarOtp(GenerateAadhaarOtpRequest request) {
        LogUtil.logger.info("Generating Aadhaar OTP for request");

        return Mono.fromCallable(() -> validateRequest(request))
                .flatMap(validatedRequest -> {
                    try {
                        return post(Constants.GENERATE_AADHAAR_OTP,
                                validatedRequest,
                                new ParameterizedTypeReference<GenerateAadhaarOtpResponse>() {});
                    } catch (Exception e) {
                        LogUtil.logger.error("An unexpected error occurred while generating Aadhaar OTP", e);
                        return Mono.error(new EhrApiError("An unexpected error occurred while generating Aadhaar OTP", HttpStatusCode.valueOf(500)));
                    }
                });
    }


    /**
     * <h3>Step 2: Verify Aadhaar OTP</h3>
     * Verifies the One-Time Password (OTP) sent to the user's Aadhaar-linked mobile number to complete
     * the authentication process and retrieve their demographic data.
     *
     * <h3>Input and Validation:</h3>
     * The method accepts a {@link VerifyAadhaarOtpRequest} object which contains:
     * <ul>
     *     <li><b>txnId:</b> The unique transaction ID received from the {@link #generateAadhaarOtp(GenerateAadhaarOtpRequest)} step. This field is mandatory.</li>
     *     <li><b>Otp:</b> The 6-digit OTP entered by the user. This field is mandatory.</li>
     *     <li><b>domainName:</b> The domain for the HPR ID (e.g., "@hpr.abdm"). Mandatory.</li>
     *     <li><b>idType:</b> The type of ID being processed (e.g., "hprid"). Mandatory.</li>
     * </ul>
     * If validation fails, the method returns a {@code Mono.error} with an {@link EhrApiError}.
     *
     * <h3>API Request:</h3>
     * This method makes a <b>POST</b> request to the {@code /v1/hpr/registration/aadhaar/verifyOtp} endpoint.
     *
     * <h3>Output:</h3>
     * @return A {@link Mono} that emits a {@link VerifyAadhaarOtpResponse} upon success. This response is rich with
     * the user's demographic data retrieved from Aadhaar, such as:
     * <ul>
     *     <li><b>txnId:</b> The same transaction ID, for continuity.</li>
     *     <li><b>name, gender, birthDate:</b> Basic demographic details.</li>
     *     <li><b>address, state, district, pincode:</b> Full address information.</li>
     *     <li><b>photo:</b> A base64-encoded string of the user's Aadhaar photograph.</li>
     * </ul>
     *
     * <h3>Error Handling:</h3>
     * Returns a {@code Mono.error} if the OTP is incorrect, has expired, or if the `txnId` is invalid.
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * VerifyAadhaarOtpRequest request = new VerifyAadhaarOtpRequest();
     * request.setTxnId("txn-abc-123");
     * request.setOtp("654321");
     * request.setDomainName("@hpr.abdm");
     * request.setIdType("hprid");
     *
     * hprService.verifyAadhaarOtp(request)
     *     .subscribe(
     *         response -> {
     *             System.out.println("Verification successful for user: " + response.getName());
     *             System.out.println("Transaction ID confirmed: " + response.getTxnId());
     *         },
     *         error -> System.err.println("Error verifying OTP: " + error.getMessage())
     *     );
     *
     * // Expected Output (example):
     * // Verification successful for user: John Doe
     * // Transaction ID confirmed: txn-abc-123
     * }</pre>
     *
     * @param request DTO containing the transaction ID and the user-entered OTP.
     * @return A {@link Mono} emitting a {@link VerifyAadhaarOtpResponse} with user demographic data.
     */
    public Mono<VerifyAadhaarOtpResponse> verifyAadhaarOtp(VerifyAadhaarOtpRequest request) {
        LogUtil.logger.info("Verifying Aadhaar OTP for transaction ID: {}", request.getTxnId());

        return Mono.fromCallable(() -> validateRequest(request))
                .flatMap(validatedRequest -> {
                    try {
                        return post(Constants.VERIFY_AADHAAR_OTP,
                                validatedRequest,
                                new ParameterizedTypeReference<>() {});
                    } catch (Exception e) {
                        LogUtil.logger.error("An unexpected error occurred while verifying Aadhaar OTP", e);
                        return Mono.error(new EhrApiError("An unexpected error occurred while verifying Aadhaar OTP", HttpStatusCode.valueOf(500)));
                    }
                });
    }

    /**
     * <h3>Step 3: Check for Existing HPR Account</h3>
     * After successful Aadhaar verification, this method checks if a Healthcare Professional Registry (HPR)
     * account already exists for the user. The API can return two different structures based on the result.
     *
     * <h3>Input and Validation:</h3>
     * The method accepts a {@link CheckAccountRequest} object which contains:
     * <ul>
     *     <li><b>txnId:</b> The unique transaction ID from the verified session. This field is mandatory.</li>
     *     <li><b>preverifiedCheck:</b> A boolean flag, typically set to {@code true}, indicating the check
     *     is being performed with pre-verified data. This field is mandatory.</li>
     * </ul>
     *
     * <h3>API Request:</h3>
     * This method makes a <b>POST</b> request to the {@code /v1/hpr/registration/check/account-exist} endpoint.
     *
     * <h3>Output:</h3>
     * @return A {@link Mono} that emits an {@link Object}. The caller must check the actual type of this object:
     * <ul>
     *     <li><b>If an account exists:</b> The object will be an instance of {@link HprAccountResponse}, containing
     *     details like {@code hprId}, {@code hprIdNumber}, and a {@code token}. The {@code isNew} flag will be {@code false}.</li>
     *     <li><b>If no account exists:</b> The object will be an instance of {@link NonHprAccountResponse}, containing
     *     the user's demographic data (name, address, etc.) to pre-fill the registration form. The {@code isNew} flag will be {@code true}.</li>
     * </ul>
     *
     * <h3>Error Handling:</h3>
     * Returns a {@code Mono.error} if the `txnId` is invalid or if the API call fails.
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * CheckAccountRequest request = new CheckAccountRequest("txn-abc-123", true);
     *
     * hprService.checkAccountExist(request)
     *     .subscribe(
     *         response -> {
     *             if (response instanceof HprAccountResponse) {
     *                 HprAccountResponse existingAccount = (HprAccountResponse) response;
     *                 System.out.println("HPR Account already exists with ID: " + existingAccount.getHprId());
     *             } else if (response instanceof NonHprAccountResponse) {
     *                 NonHprAccountResponse newAccountInfo = (NonHprAccountResponse) response;
     *                 System.out.println("No HPR account found. Ready to register user: " + newAccountInfo.getName());
     *             }
     *         },
     *         error -> System.err.println("Error checking account existence: " + error.getMessage())
     *     );
     *
     * // Expected Output (if account exists):
     * // HPR Account already exists with ID: johndoe.hpr
     *
     * // Expected Output (if no account exists):
     * // No HPR account found. Ready to register user: John Doe
     * }</pre>
     *
     * @param request DTO containing the transaction ID.
     * @return A {@link Mono} emitting an {@link Object} which can be cast to {@link HprAccountResponse} or {@link NonHprAccountResponse}.
     */
    public Mono<Object> checkAccountExist(CheckAccountRequest request) {
        LogUtil.logger.info("Checking account existence for transaction ID: {}", request.getTxnId());

        return Mono.fromCallable(() -> validateRequest(request))
                .flatMap(validatedRequest -> {
                    try {
                        return post(Constants.CHECK_HPR_ACCOUNT,
                                validatedRequest,
                                new ParameterizedTypeReference<HprAccountResponse>() {})
                                .cast(Object.class)
                                .onErrorResume(error -> {
                                    // If HPR account response fails, try with NonHprAccountResponse
                                    return post("/check/account-exist",
                                            validatedRequest,
                                            new ParameterizedTypeReference<NonHprAccountResponse>() {})
                                            .cast(Object.class);
                                });
                    } catch (Exception e) {
                        LogUtil.logger.error("An unexpected error occurred while checking account existence", e);
                        return Mono.error(new EhrApiError("An unexpected error occurred while checking account existence", HttpStatusCode.valueOf(500)));
                    }
                });
    }

    /**
     * <h3>Step 4: Perform Demographic Authentication via Mobile</h3>
     * This method is used to verify that the mobile number provided by the user matches the one associated
     * with their Aadhaar details for the given transaction.
     *
     * <h3>Input and Validation:</h3>
     * The method accepts a {@link DemographicAuthViaMobileRequest} object which contains:
     * <ul>
     *     <li><b>txnId:</b> The unique transaction ID from the verified session. This field is mandatory.</li>
     *     <li><b>mobileNumber:</b> The user's mobile number to be verified. This field is mandatory.</li>
     * </ul>
     *
     * <h3>API Request:</h3>
     * This method makes a <b>POST</b> request to the {@code /v1/hpr/registration/demographic-auth/mobile} endpoint.
     *
     * <h3>Output:</h3>
     * @return A {@link Mono} that emits a {@link DemographicAuthViaMobileResponse}. This response contains:
     * <ul>
     *     <li><b>verified:</b> A boolean flag that is {@code true} if the mobile number was successfully verified.</li>
     * </ul>
     *
     * <h3>Error Handling:</h3>
     * Returns a {@code Mono.error} if the mobile number does not match or if the API call fails.
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * DemographicAuthViaMobileRequest request = new DemographicAuthViaMobileRequest("txn-abc-123", "9876543210");
     *
     * hprService.demographicAuthViaMobile(request)
     *     .subscribe(
     *         response -> System.out.println("Demographic authentication successful: " + response.isVerified()),
     *         error -> System.err.println("Error during demographic auth: " + error.getMessage())
     *     );
     *
     * // Expected Output:
     * // Demographic authentication successful: true
     * }</pre>
     *
     * @param request DTO containing the transaction ID and mobile number.
     * @return A {@link Mono} emitting a {@link DemographicAuthViaMobileResponse} with the verification status.
     */
    public Mono<DemographicAuthViaMobileResponse> demographicAuthViaMobile(DemographicAuthViaMobileRequest request) {
        LogUtil.logger.info("Performing demographic auth via mobile for transaction ID: {}", request.getTxnId());

        return Mono.fromCallable(() -> validateRequest(request))
                .flatMap(validatedRequest -> {
                    try {
                        return post(Constants.DEMOGRAPHIC_AUTH_MOBILE,
                                validatedRequest,
                                new ParameterizedTypeReference<DemographicAuthViaMobileResponse>() {});
                    } catch (Exception e) {
                        LogUtil.logger.error("An unexpected error occurred while verifying demographic auth via mobile", e);
                        return Mono.error(new EhrApiError("An unexpected error occurred while verifying demographic auth via mobile", HttpStatusCode.valueOf(500)));
                    }
                });
    }

    /**
     * <h3>Step 5: Generate Mobile OTP</h3>
     * Generates an OTP and sends it to a user-provided mobile number. This step is used to verify a mobile number
     * that may be different from the one linked to Aadhaar.
     *
     * <h3>Input and Validation:</h3>
     * The method accepts a {@link GenerateMobileOtpRequest} object which contains:
     * <ul>
     *     <li><b>txnId:</b> The unique transaction ID from the session. This field is mandatory.</li>
     *     <li><b>Mobile:</b> The mobile number to which the OTP should be sent. This field is mandatory.</li>
     * </ul>
     *
     * <h3>API Request:</h3>
     * This method makes a <b>POST</b> request to the {@code /v1/hpr/registration/generate/mobileOtp} endpoint.
     *
     * <h3>Output:</h3>
     * @return A {@link Mono} that emits a {@link MobileOtpResponse}. This response primarily contains:
     * <ul>
     *     <li><b>txnId:</b> The same transaction ID, passed through for the next step.
     *     <li><b>txnId:</b> The same transaction ID, passed through for the next step.</li>
     * </ul>
     *
     * <h3>Error Handling:</h3>
     * Returns a {@code Mono.error} if the `txnId` is invalid or if the API call fails.
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * GenerateMobileOtpRequest request = new GenerateMobileOtpRequest("txn-abc-123", "9876543210");
     *
     * hprService.generateMobileOtp(request)
     *     .subscribe(
     *         response -> System.out.println("Mobile OTP generation initiated. Transaction ID: " + response.getTxnId()),
     *         error -> System.err.println("Error generating mobile OTP: " + error.getMessage())
     *     );
     *
     * // Expected Output:
     * // Mobile OTP generation initiated. Transaction ID: txn-abc-123
     * }</pre>
     *
     * @param request DTO containing the transaction ID and the mobile number.
     * @return A {@link Mono} emitting a {@link MobileOtpResponse}.
     */
    public Mono<MobileOtpResponse> generateMobileOtp(GenerateMobileOtpRequest request) {
        LogUtil.logger.info("Generating mobile OTP for transaction ID: {}", request.getTxnId());

        return Mono.fromCallable(() -> validateRequest(request))
                .flatMap(validatedRequest -> {
                    try {
                        return post(Constants.GENERATE_MOBILE_OTP,
                                validatedRequest,
                                new ParameterizedTypeReference<MobileOtpResponse>() {});
                    } catch (Exception e) {
                        LogUtil.logger.error("An unexpected error occurred while generating mobile OTP", e);
                        return Mono.error(new EhrApiError("An unexpected error occurred while generating mobile OTP", HttpStatusCode.valueOf(500)));
                    }
                });
    }

    /**
     * <h3>Step 6: Verify Mobile OTP</h3>
     * Verifies the One-Time Password (OTP) sent to the user's mobile number to confirm ownership.
     *
     * <h3>Input and Validation:</h3>
     * The method accepts a {@link VerifyMobileOtpRequest} object which contains:
     * <ul>
     *     <li><b>txnId:</b> The unique transaction ID from the session. This field is mandatory.</li>
     *     <li><b>Otp:</b> The 6-digit OTP entered by the user. This field is mandatory.</li>
     * </ul>
     *
     * <h3>API Request:</h3>
     * This method makes a <b>POST</b> request to the {@code /v1/hpr/registration/verify/mobileOtp} endpoint.
     *
     * <h3>Output:</h3>
     * @return A {@link Mono} that emits a {@link MobileOtpResponse}. This response confirms verification and contains:
     * <ul>
     *     <li><b>txnId:</b> The same transaction ID.</li>
     *     <li><b>mobileNumber:</b> The masked mobile number that was just verified.</li>
     * </ul>
     *
     * <h3>Error Handling:</h3>
     * Returns a {@code Mono.error} if the OTP is incorrect, has expired, or if the API call fails.
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * VerifyMobileOtpRequest request = new VerifyMobileOtpRequest("654321", "txn-abc-123");
     *
     * hprService.verifyMobileOtp(request)
     *     .subscribe(
     *         response -> System.out.println("Mobile number " + response.getMobileNumber() + " verified successfully."),
     *         error -> System.err.println("Error verifying mobile OTP: " + error.getMessage())
     *     );
     *
     * // Expected Output:
     * // Mobile number xxxxxx3210 verified successfully.
     * }</pre>
     *
     * @param request DTO containing the transaction ID and the user-entered OTP.
     * @return A {@link Mono} emitting a {@link MobileOtpResponse} confirming the verification.
     */
    public Mono<MobileOtpResponse> verifyMobileOtp(VerifyMobileOtpRequest request) {
        LogUtil.logger.info("Verifying mobile OTP for transaction ID: {}", request.getTxnId());

        return Mono.fromCallable(() -> validateRequest(request))
                .flatMap(validatedRequest -> {
                    try {
                        return post(Constants.VERIFY_MOBILE_OTP,
                                validatedRequest,
                                new ParameterizedTypeReference<MobileOtpResponse>() {});
                    } catch (Exception e) {
                        LogUtil.logger.error("An unexpected error occurred while verifying mobile OTP", e);
                        return Mono.error(new EhrApiError("An unexpected error occurred while verifying mobile OTP", HttpStatusCode.valueOf(500)));
                    }
                });
    }

    /**
     * <h3>Step 7: Get HPR ID Suggestions</h3>
     * Fetches a list of available HPR ID (username) suggestions based on the user's verified name
     * from the current transaction context.
     *
     * <h3>Input and Validation:</h3>
     * The method accepts a {@link IdSuggestionRequest} object which contains:
     * <ul>
     *     <li><b>txnId:</b> The unique transaction ID from the session. This field is mandatory.</li>
     * </ul>
     *
     * <h3>API Request:</h3>
     * This method makes a <b>POST</b> request to the {@code /v1/hpr/registration/hpId/suggestion} endpoint.
     *
     * <h3>Output:</h3>
     * @return A {@link Mono} that emits a {@link List} of {@link String}s. Each string is a suggested,
     * available HPR ID that the user can choose from.
     *
     * <h3>Error Handling:</h3>
     * Returns a {@code Mono.error} if the `txnId` is invalid or if the API call fails.
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * IdSuggestionRequest request = new IdSuggestionRequest("txn-abc-123");
     *
     * hprService.getHprSuggestion(request)
     *     .subscribe(
     *         suggestions -> {
     *             System.out.println("Available HPR ID suggestions:");
     *             suggestions.forEach(System.out::println);
     *         },
     *         error -> System.err.println("Error fetching HPR ID suggestions: " + error.getMessage())
     *     );
     *
     * // Expected Output (example):
     * // Available HPR ID suggestions:
     * // johndoe1
     * // j.doe
     * // john.d.hpr
     * }</pre>
     *
     * @param request DTO containing the transaction ID.
     * @return A {@link Mono} emitting a list of suggested HPR IDs.
     */
    public Mono<List<String>> getHprSuggestion(IdSuggestionRequest request) {
        LogUtil.logger.info("Getting HPR suggestions for transaction ID: {}", request.getTxnId());

        return Mono.fromCallable(() -> validateRequest(request))
                .flatMap(validatedRequest -> {
                    try {
                        return post(Constants.GET_HPR_SUGGESTIONS,
                                validatedRequest,
                                new ParameterizedTypeReference<List<String>>() {});
                    } catch (Exception e) {
                        LogUtil.logger.error("An unexpected error occurred while getting HPR suggestions", e);
                        return Mono.error(new EhrApiError("An unexpected error occurred while getting HPR suggestions", HttpStatusCode.valueOf(500)));
                    }
                });
    }

    /**
     * <h3>Step 8: Create HPR ID</h3>
     * Finalizes the registration process by submitting all the user's details and creating the
     * Healthcare Professional Registry (HPR) ID.
     *
     * <h3>Input and Validation:</h3>
     * The method accepts a {@link CreateHprIdWithPreVerifiedRequest} object, which is a comprehensive DTO
     * containing all required information, such as:
     * <ul>
     *     <li><b>txnId:</b> The unique transaction ID from the session. Mandatory.</li>
     *     <li><b>hprId:</b> The HPR ID chosen by the user from the suggestions. Mandatory.</li>
     *     <li><b>Password:</b> The password for the new account. Mandatory.</li>
     *     <li><b>firstName, lastName, dayOfBirth, etc.:</b> All demographic and professional details. Mandatory.</li>
     * </ul>
     * The DTO is validated against its defined constraints.
     *
     * <h3>API Request:</h3>
     * This method makes a <b>POST</b> request to the {@code /v1/hpr/registration/hprId/create} endpoint.
     *
     * <h3>Output:</h3>
     * @return A {@link Mono} that emits a {@link CreateHprIdWithPreVerifiedResponse} upon success. This response
     * contains the complete details of the newly created account, including:
     * <ul>
     *     <li><b>hprId:</b> The created HPR ID.</li>
     *     <li><b>hprIdNumber:</b> The system-generated unique HPR number.</li>
     *     <li><b>token:</b> An authentication token for the new session.</li>
     *     <li>And all other profile details.</li>
     * </ul>
     *
     * <h3>Error Handling:</h3>
     * Returns a {@code Mono.error} if any validation fails, if the chosen `hprId` is already taken,
     * or if any other API error occurs.
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * CreateHprIdWithPreVerifiedRequest request = CreateHprIdWithPreVerifiedRequest.builder()
     *         .txnId("txn-abc-123")
     *         .hprId("johndoe1")
     *         .password("StrongP@ssw0rd!")
     *         .firstName("John")
     *         .lastName("Doe")
     *         .dayOfBirth("15").monthOfBirth("10").yearOfBirth("1985")
     *         .email("john.doe@email.com")
     *         .address("123 Health St, Medicity")
     *         .stateCode("MH").districtCode("MUM").pincode("400001")
     *         .hpCategoryCode("HP01").hpSubCategoryCode("SUB01")
     *         .build();
     *
     * hprService.createHprIdWithPreVerified(request)
     *     .subscribe(
     *         response -> {
     *             System.out.println("HPR Account created successfully!");
     *             System.out.println("HPR ID: " + response.getHprId());
     *             System.out.println("HPR Number: " + response.getHprIdNumber());
     *         },
     *         error -> System.err.println("Error creating HPR ID: " + error.getMessage())
     *     );
     *
     * // Expected Output (example):
     * // HPR Account created successfully!
     * // HPR ID: johndoe1
     * // HPR Number: 11-2233-4455-6677
     * }</pre>
     *
     * @param request DTO containing all the final details for HPR ID creation.
     * @return A {@link Mono} emitting a {@link CreateHprIdWithPreVerifiedResponse} with the new account details.
     */
    public Mono<CreateHprIdWithPreVerifiedResponse> createHprIdWithPreVerified(CreateHprIdWithPreVerifiedRequest request) {
        LogUtil.logger.info("Creating HPR ID with pre-verified data for transaction ID: {}", request.getTxnId());

        return Mono.fromCallable(() -> validateRequest(request))
                .flatMap(validatedRequest -> {
                    try {
                        return post(Constants.CREATE_HPR,
                                validatedRequest,
                                new ParameterizedTypeReference<CreateHprIdWithPreVerifiedResponse>() {});
                    } catch (Exception e) {
                        LogUtil.logger.error("An unexpected error occurred while creating HPR ID with pre-verified data", e);
                        return Mono.error(new EhrApiError("An unexpected error occurred while creating HPR ID with pre-verified data", HttpStatusCode.valueOf(500)));
                    }
                });
    }

    /**
     * Handles the Aadhaar OTP generation step in the HPR registration flow.
     * Converts the input payload to {@link GenerateAadhaarOtpRequest}, initiates OTP generation,
     * and builds a successful or error response wrapped in {@link HprFlowResponse}.
     *
     * @param payload input payload containing Aadhaar number
     * @return Mono of {@link HprFlowResponse} indicating next step or error
     */
    private Mono<HprFlowResponse> handleGenerateAadhaarOtp(Object payload) {
        GenerateAadhaarOtpRequest request = objectMapper.convertValue(payload, GenerateAadhaarOtpRequest.class);
        return generateAadhaarOtp(request)
                .map(response -> HprFlowResponse.success(
                        "OTP sent successfully to your registered mobile number.",
                        response,
                        HprSteps.verify_aadhaar_otp,
                        "Please enter the OTP sent to your registered mobile number and the txnId.",
                        "VerifyAadhaarOtpRequest"
                ))
                .onErrorResume(error -> Mono.just(HprFlowResponse.error(error.getMessage())));

    }

    /**
     * Handles the Aadhaar OTP verification step in the HPR registration flow.
     * Converts the payload to {@link VerifyAadhaarOtpRequest} and verifies the OTP.
     *
     * @param payload input payload containing OTP verification details
     * @return Mono of {@link HprFlowResponse} indicating next step or error
     */
    private Mono<HprFlowResponse> handleVerifyAadhaarOtp(Object payload) {
        VerifyAadhaarOtpRequest request = objectMapper.convertValue(payload, VerifyAadhaarOtpRequest.class);
        return verifyAadhaarOtp(request)
                .map(res -> HprFlowResponse.success(
                        "Aadhaar OTP verified successfully.",
                        res,
                        HprSteps.check_account_exists,
                        "Please provide txnId and preverifiedCheck boolean as true to check account existence",
                        "CheckAccountRequest"
                ))
                .onErrorResume(error -> Mono.just(HprFlowResponse.error(error.getMessage())));
    }

    /**
     * Handles the step to check whether an HPR account exists for the given Aadhaar.
     *
     * @param payload input payload containing txnId and preverified flag
     * @return Mono of {@link HprFlowResponse} indicating next step or existing account
     */
    private Mono<HprFlowResponse> handleCheckAccountExist(Object payload) {
        CheckAccountRequest request = objectMapper.convertValue(payload, CheckAccountRequest.class);

        return checkAccountExist(request)
                .map(res -> {
                    Map<String, Object> dataMap = objectMapper.convertValue(res, Map.class);
                    boolean isNew = Boolean.TRUE.equals(dataMap.get("new"));

                    if (isNew) {
                        return HprFlowResponse.success(
                                "No account exists, please follow next steps.",
                                res,
                                HprSteps.demographic_auth_via_mobile,
                                "Provide txnId and mobile for demographic auth",
                                "DemographicAuthViaMobileRequest"
                        );
                    } else {
                        return HprFlowResponse.success(
                                "Account already exists.",
                                res,
                                null,
                                null,
                                null
                        );
                    }
                })
                .onErrorResume(error -> Mono.just(HprFlowResponse.error(error.getMessage())));
    }

    /**
     * Handles demographic authentication via mobile.
     * Converts the input payload to {@link DemographicAuthViaMobileRequest},
     * sends the request, and decides the next step based on verification.
     *
     * @param payload input payload with txnId and mobile number
     * @return Mono of {@link HprFlowResponse} with next step or error
     */
    private Mono<HprFlowResponse> handleDemographicAuthViaMobile(Object payload) {
        DemographicAuthViaMobileRequest request = objectMapper.convertValue(payload, DemographicAuthViaMobileRequest.class);
        return demographicAuthViaMobile(request)
                .map(res -> {
                    Map<String, Object> dataMap = objectMapper.convertValue(res, Map.class);
                    boolean verified = Boolean.TRUE.equals(dataMap.get("true"));
                    if (verified) {
                        return HprFlowResponse.success(
                                "Demographic auth via mobile successful.",
                                res,
                                HprSteps.verify_mobile_otp,
                                "Provide tnxId and Mobile to generate otp.",
                                "GenerateMobileOtpRequest"
                        );
                    } else {
                        return HprFlowResponse.success(
                                "Demographic auth via mobile successful.",
                                res,
                                HprSteps.get_hpr_id_suggestions,
                                "Provide txnId for HPR ID suggestions",
                                "IdSuggestionRequest"
                        );
                    }
                })
                .onErrorResume(error -> Mono.just(HprFlowResponse.error(error.getMessage())));
    }

    /**
     * Handles the mobile OTP generation step.
     * Converts the payload to {@link GenerateMobileOtpRequest}, invokes the API, and returns next step.
     *
     * @param payload input payload with txnId and mobile
     * @return Mono of {@link HprFlowResponse} containing result or error
     */
    private Mono<HprFlowResponse> handleGenerateMobileOtp(Object payload) {
        GenerateMobileOtpRequest request = objectMapper.convertValue(payload, GenerateMobileOtpRequest.class);
        return generateMobileOtp(request)
                .map(res -> HprFlowResponse.success(
                        "Mobile OTP sent successfully.",
                        res,
                        HprSteps.verify_mobile_otp,
                        "Enter txnId and OTP to verify mobile",
                        "VerifyMobileOtpRequest"
                ))
                .onErrorResume(error -> Mono.just(HprFlowResponse.error(error.getMessage())));
    }


    /**
     * Handles the mobile OTP verification step.
     * Converts the payload to {@link VerifyMobileOtpRequest}, sends the verification request,
     * and prepares the next step in the flow.
     *
     * @param payload input payload with txnId and OTP
     * @return Mono of {@link HprFlowResponse} indicating next step or error
     */
    private Mono<HprFlowResponse> handleVerifyMobileOtp(Object payload) {
        VerifyMobileOtpRequest request = objectMapper.convertValue(payload, VerifyMobileOtpRequest.class);
        return verifyMobileOtp(request)
                .map(res -> HprFlowResponse.success(
                        "Mobile OTP verified successfully.",
                        res,
                        HprSteps.get_hpr_id_suggestions,
                        "Provide txnId for HPR ID suggestions",
                        "IdSuggestionRequest"
                ))
                .onErrorResume(error -> Mono.just(HprFlowResponse.error(error.getMessage())));
    }

    /**
     * Handles retrieval of HPR ID suggestions for the given txnId.
     * Converts the payload to {@link IdSuggestionRequest} and invokes suggestion endpoint.
     *
     * @param payload input payload with txnId
     * @return Mono of {@link HprFlowResponse} with a list of suggestions or error
     */
    private Mono<HprFlowResponse> handleGetHprSuggestion(Object payload) {
        IdSuggestionRequest request = objectMapper.convertValue(payload, IdSuggestionRequest.class);
        return getHprSuggestion(request)
                .map(res -> HprFlowResponse.success(
                        "Fetched HPR ID suggestions.",
                        res,
                        HprSteps.create_hpr,
                        "Provide txnId and final details to create HPR ID",
                        "CreateHprIdWithPreVerifiedRequest"
                ))
                .onErrorResume(error -> Mono.just(HprFlowResponse.error(error.getMessage())));
    }

    /**
     * Handles the final step of creating HPR ID using all collected and verified data.
     * Converts the payload to {@link CreateHprIdWithPreVerifiedRequest} and invokes the HPR creation endpoint.
     *
     * @param payload input payload with HPR creation details
     * @return Mono of {@link HprFlowResponse} containing success message or error
     */
    private Mono<HprFlowResponse> handleCreateHprId(Object payload) {
        CreateHprIdWithPreVerifiedRequest request = objectMapper.convertValue(payload, CreateHprIdWithPreVerifiedRequest.class);
        return createHprIdWithPreVerified(request)
                .map(res -> HprFlowResponse.success(
                        "HPR ID created successfully.",
                        res,
                        null,
                        null,
                        null
                ))
                .onErrorResume(error -> Mono.just(HprFlowResponse.error(error.getMessage())));
    }

}