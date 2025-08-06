package carestack.patient.abha;

import carestack.practitioner.hpr.dto.IdSuggestionRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import carestack.patient.abha.DTO.AbhaFlowResponse;
import carestack.patient.abha.DTO.CreateAbhaAddressRequestDto;
import carestack.patient.abha.DTO.UpdateMobile;
import carestack.patient.abha.DTO.VerifyAadhaarOtpDto;
import carestack.patient.abha.enums.AbhaSteps;
import carestack.ai.EncryptionUtilities;
import carestack.base.Base;
import carestack.base.errors.ValidationError;
import carestack.base.utils.LogUtil;
import carestack.practitioner.hpr.dto.GenerateAadhaarOtpRequest;
import carestack.practitioner.hpr.dto.VerifyMobileOtpRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static carestack.base.utils.Constants.*;

/**
 * <h3>ABHA Creation Workflow Service</h3>
 *
 * This service orchestrates the multistep Ayushman Bharat Health Account (ABHA) creation workflow.
 * It functions as a state machine, guiding the user through a series of steps from Aadhaar OTP generation
 * to the final ABHA address creation.
 *
 * <h3>Core Responsibilities:</h3>
 * <ul>
 *   <li><b>State Management:</b> Manages the user's progress through the registration flow using the {@link AbhaSteps} enum.</li>
 *   <li><b>Data Validation:</b> Ensures that the data provided at each step is valid using Jakarta Bean Validation.</li>
 *   <li><b>Data Encryption:</b> Encrypts sensitive information like Aadhaar numbers and OTPs before sending them to the ABHA APIs, as per security requirements.</li>
 *   <li><b>API Interaction:</b> Communicates with the backend ABHA services for each step of the process.</li>
 * </ul>
 *
 * The primary entry point is the {@link #createAbha(AbhaSteps, Object)} method, which dispatches requests to the appropriate handler based on the current step.
 */
@Service
public class AbhaService extends Base {

    @Autowired
    private EncryptionUtilities encryptionUtilities;

    private final Validator validator;


    /**
     * Constructs the AbhaService with necessary dependencies.
     *
     * @param objectMapper Jackson's ObjectMapper for JSON serialization/deserialization.
     * @param webClient    Spring's reactive WebClient for making HTTP requests.
     * @param validator    Jakarta's Validator for validating DTOs.
     */
    public AbhaService(ObjectMapper objectMapper, WebClient webClient,
                       Validator validator) {
        super(objectMapper, webClient);
        this.validator = validator;
    }

    /**
     * <h3>Main Entry Point for the ABHA Creation Workflow</h3>
     * This method is the central dispatcher for the ABHA creation process. It routes incoming requests
     * to the appropriate handler method based on the provided {@link AbhaSteps} enum.
     *
     * <h3>Input and Validation:</h3>
     * <ul>
     *     <li><b>Step (AbhaSteps):</b> The current step in the ABHA creation process (e.g., {@code register_with_aadhaar}). This parameter is required and must be a valid enum constant.</li>
     *     <li><b>payload (Object):</b> The data required for the current step. This is typically a {@code Map<String, Object>} or a DTO that can be converted to one. The structure of the payload depends entirely on the {@code step}. For example, for the first step, it would contain an Aadhaar number.</li>
     * </ul>
     *
     * <h3>Workflow:</h3>
     * <ol>
     *     <li>Receives the current step and its corresponding payload.</li>
     *     <li>Uses a switch statement to call the specific handler method for that step (e.g., {@code handleRegisterWithAadhaar}).</li>
     *     <li>The handler method validates the payload, performs necessary actions (like encryption and API calls), and returns an {@link AbhaFlowResponse}.</li>
     *     <li>The response is then formatted into a standardized {@code Map<String, Object>} for the client.</li>
     * </ol>
     *
     * <h3>Output:</h3>
     * @return A {@link Mono} emitting a {@code Map<String, Object>} that contains the result of the step. The map includes:
     * <ul>
     *     <li><b>message (String):</b> A user-friendly message indicating the result of the operation (e.g., "OTP sent successfully").</li>
     *     <li><b>data (Object):</b> The response data from the underlying API call.</li>
     *     <li><b>next_step (AbhaSteps):</b> The next step the user should perform in the flow. This will be {@code null} if the flow is complete.</li>
     *     <li><b>next_step_payload_hint (String):</b> A hint for the developer about what data is expected in the payload for the next step.</li>
     *     <li><b>next_step_payload_dto (String):</b> The name of the DTO class that represents the payload for the next step.</li>
     * </ul>
     *
     * <h3>Error Handling:</h3>
     * If an invalid step is provided or an internal error occurs, the method returns a Mono containing a map with an "error" key.
     *
     * <h3>Example Usage (Step 1: Register with Aadhaar):</h3>
     * <pre>{@code
     * Map<String, Object> payload = new HashMap<>();
     * payload.put("aadhaar", "123456789012"); // User's Aadhaar number
     *
     * abhaService.createAbha(AbhaSteps.register_with_aadhaar, payload)
     *     .subscribe(response -> {
     *         System.out.println("Response: " + response);
     *     });
     * }</pre>
     *
     * <h3>Expected Output (Example):</h3>
     * <pre>{@code
     * // Response: {
     * //   "next_step_payload_dto": "VerifyAadhaarOtpDto",
     * //   "data": { "response": { "txnId": "some-transaction-id" }, "success": true },
     * //   "message": "OTP sent successfully to your registered mobile number.",
     * //   "next_step": "verify_aadhaar_otp",
     * //   "next_step_payload_hint": "Please enter the OTP sent to your registered mobile number and the txnId and the mobile."
     * // }
     * }</pre>
     *
     * @param step    The current step in the ABHA creation process.
     * @param payload The data required for the current step.
     */
    public Mono<Map<String, Object>> createAbha(AbhaSteps step, Object payload) {
        try {
            switch (step) {
                case register_with_aadhaar:
                    return handleRegisterWithAadhaar(payload)
                            .map(this::buildResponseMap);
                case verify_aadhaar_otp:
                    return handleVerifyAadhaarOtp(payload)
                            .map(this::buildResponseMap);
                case update_mobile:
                    return handleUpdateMobile(payload)
                            .map(this::buildResponseMap);
                case verify_update_mobile_otp:
                    return handleVerifyUpdateMobileOtp(payload)
                            .map(this::buildResponseMap);
                case get_abha_address_suggestions:
                    return handleGetAbhaAddressSuggestions(payload)
                            .map(this::buildResponseMap);
                case final_register:
                    return handleFinalRegister(payload)
                            .map(this::buildResponseMap);
                default:
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", "Invalid step in ABHA creation flow");
                    return Mono.just(errorResponse);
            }
        } catch (Exception e) {
            LogUtil.logger.error("Error in ABHA flow for step: {}", step, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal error: " + e.getMessage());
            return Mono.just(errorResponse);
        }
    }

    /**
     * Helper method to build the standardized response map for the client.
     *
     * @param response The internal flow response object.
     * @return A map containing the message, data, next step, and payload hint.
     */
    private Map<String, Object> buildResponseMap(AbhaFlowResponse response) {
        Map<String, Object> result = new HashMap<>();
        result.put("message", response.getMessage());
        result.put("data", response.getData());
        result.put("next_step", response.getNextStep());
        result.put("next_step_payload_hint", response.getNextStepPayloadHint());
        result.put("next_step_payload_dto", response.getNextStepPayloadDTO());
        return result;
    }



    /**
     * <h3>Initiates the ABHA Creation Flow Using a String-Based Step Identifier</h3>
     * This is a convenience method that accepts the step as a string (e.g., "1", "2", or "register_with_aadhaar")
     * and converts it to the corresponding {@link AbhaSteps} enum before processing.
     *
     * <h3>Input and Validation:</h3>
     * <ul>
     *     <li><b>step (String):</b> The string representation of the step. This can be the enum's numeric value or its name (case-insensitive).</li>
     *     <li><b>payload (Object):</b> The data for the step, same as in {@link #createAbha(AbhaSteps, Object)}.
     * </ul>
     *
     * <h3>Error Handling:</h3>
     * Returns a Mono with an error map if the step string is invalid or if any other processing error occurs.
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * Map<String, Object> payload = new HashMap<>();
     * payload.put("aadhaar", "123456789012");
     *
     * // Using the step's numeric value
     * abhaService.createAbhaByStepName("1", payload).subscribe(System.out::println);
     *
     * // Using the step's name
     * abhaService.createAbhaByStepName("register_with_aadhaar", payload).subscribe(System.out::println);
     * }</pre>
     *
     * @param step    The string representation of the step.
     * @param payload The data required for the current step.
     * @return A {@link Mono} with the result of the step execution, identical in format to the output of {@link #createAbha(AbhaSteps, Object)}.
     */
    public Mono<Map<String, Object>> createAbhaByStepName(String step, Object payload) {
        try {
            AbhaSteps abhaStep = AbhaSteps.fromValue(step);
            if (payload instanceof String) {
                payload = objectMapper.readValue((String) payload, Map.class);
            }

            return createAbha(abhaStep, payload);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid step: " + step);
            return Mono.just(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error processing the request: " + e.getMessage());
            return Mono.just(errorResponse);
        }
    }


    /**
     * <h3>Handles Step 1: Register with Aadhaar</h3>
     * This method initiates the ABHA registration by sending an OTP to the user's Aadhaar-linked mobile number.
     *
     * <h3>Workflow:</h3>
     * <ol>
     *     <li>Converts the payload to a {@link GenerateAadhaarOtpRequest} DTO and validates it.</li>
     *     <li>Encrypts the Aadhaar number.</li>
     *     <li>Makes a POST request to the {@code /register_with_aadhaar} endpoint.</li>
     *     <li>On success, returns a response indicating the next step is {@code verify_aadhaar_otp}.</li>
     * </ol>
     *
     * @param payload The input payload, expected to contain an "aadhaar" key.
     * @return A {@link Mono} emitting an {@link AbhaFlowResponse} for the next stage.
     */
    private Mono<AbhaFlowResponse> handleRegisterWithAadhaar(Object payload) {
        return validateAndProcess(payload, GenerateAadhaarOtpRequest.class,
                REGISTER_WITH_AADHAAR, "POST",AbhaSteps.register_with_aadhaar)
                .map(response -> {
                    if (Boolean.TRUE.equals(response.get("success"))) {
                        return AbhaFlowResponse.success(
                                "OTP sent successfully to your registered mobile number.",
                                response,
                                AbhaSteps.verify_aadhaar_otp,
                                "Please enter the OTP sent to your registered mobile number and the txnId and the mobile.",
                                "VerifyAadhaarOtpDto"
                        );
                    } else {
                        return AbhaFlowResponse.error("Failed to send OTP: " + response.get("error"));
                    }
                });
    }


    /**
     * <h3>Handles Step 2: Verify Aadhaar OTP</h3>
     * This method verifies the Aadhaar OTP provided by the user. It also checks if the mobile number
     * provided by the user matches the one registered with Aadhaar.
     *
     * <h3>Workflow:</h3>
     * <ol>
     *     <li>Converts the payload to a {@link VerifyAadhaarOtpDto} and validates it.</li>
     *     <li>Encrypts the OTP.</li>
     *     <li>Makes a POST request to the {@code /enroll_aadhaar} endpoint.</li>
     *     <li>Compares the mobile number from the request payload with the one in the API response.</li>
     *     <li>If they match, the next step is {@code get_abha_address_suggestions}.</li>
     *     <li>If they do not match, the next step is {@code update_mobile}.</li>
     * </ol>
     *
     * @param payload The input payload, expected to contain "otp", "txnId", and "mobile".
     * @return A {@link Mono} emitting an {@link AbhaFlowResponse} for the next stage.
     */
    public Mono<AbhaFlowResponse> handleVerifyAadhaarOtp(Object payload) {
        return validateAndProcess(payload, VerifyAadhaarOtpDto.class,
                ENROLL_AADHAAR, "POST",AbhaSteps.verify_aadhaar_otp)
                .map(response -> {
                    if (Boolean.TRUE.equals(response.get("success"))) {
                        // Extract the mobile number from the request payload
                        String requestMobile = extractMobileFromPayload(payload);

                        // Extract the mobile number from the response
                        String responseMobile = extractMobileFromResponse(response);

                        // Determine the next step based on mobile number comparison
                        AbhaSteps nextStep;
                        String nextStepHint;
                        String nextStepDTO;

                        if (requestMobile != null && requestMobile.equals(responseMobile)) {
                            // Mobile numbers match, skip mobile update
                            nextStep = AbhaSteps.get_abha_address_suggestions;
                            nextStepHint = "Mobile number is already verified. Proceed to select ABHA address.";
                            nextStepDTO = "IdSuggestionRequest";
                        } else {
                            // Mobile numbers don't match or response mobile is null
                            nextStep = AbhaSteps.update_mobile;
                            nextStepHint = "Mobile number needs to be updated. Please proceed with mobile update.";
                            nextStepDTO = "UpdateMobile";
                        }

                        return AbhaFlowResponse.success(
                                "Aadhaar OTP verified successfully.",
                                response,
                                nextStep,
                                nextStepHint,
                                nextStepDTO
                        );
                    } else {
                        return AbhaFlowResponse.error("Failed to verify OTP: " + response.get("error"));
                    }
                });
    }


    /**
     * <h3>Handles Step 3: Update Mobile Number</h3>
     * This method is triggered if the user's provided mobile number differs from the one linked to Aadhaar.
     * It sends an OTP to the new mobile number for verification.
     *
     * <h3>Workflow:</h3>
     * <ol>
     *     <li>Converts the payload to an {@link UpdateMobile} DTO and validates it.</li>
     *     <li>Encrypts the new mobile number (the "updateValue").</li>
     *     <li>Makes a POST request to the {@code /update_mobile} endpoint.</li>
     *     <li>On success, the next step is {@code verify_update_mobile_otp}.</li>
     * </ol>
     *
     * @param payload The input payload, expected to contain "txnId" and "updateValue" (the new mobile number).
     * @return A {@link Mono} emitting an {@link AbhaFlowResponse} for the next stage.
     */
    public Mono<AbhaFlowResponse> handleUpdateMobile(Object payload) {
        return validateAndProcess(payload, UpdateMobile.class,
                UPDATE_MOBILE, "POST",AbhaSteps.update_mobile)
                .map(response -> {
                    if (Boolean.TRUE.equals(response.get("success"))) {
                        return AbhaFlowResponse.success(
                                "OTP sent to your new mobile number.",
                                response,
                                AbhaSteps.verify_update_mobile_otp,
                                "Please enter the OTP sent to your new mobile number.",
                                "VerifyMobileOtpRequest"
                        );
                    } else {
                        return AbhaFlowResponse.error("Failed to send OTP: " + response.get("error"));
                    }
                });
    }


    /**
     * <h3>Handles Step 4: Verify Updated Mobile OTP</h3>
     * This method verifies the OTP sent to the new mobile number.
     *
     * <h3>Workflow:</h3>
     * <ol>
     *     <li>Converts the payload to a {@link VerifyMobileOtpRequest} DTO and validates it.</li>
     *     <li>Encrypts the OTP.</li>
     *     <li>Makes a POST request to the {@code /verify_update_mobile} endpoint.</li>
     *     <li>On success, the next step is {@code get_abha_address_suggestions}.</li>
     * </ol>
     *
     * @param payload The input payload, expected to contain "txnId" and "otp".
     * @return A {@link Mono} emitting an {@link AbhaFlowResponse} for the next stage.
     */
    public Mono<AbhaFlowResponse> handleVerifyUpdateMobileOtp(Object payload) {
        return validateAndProcess(payload, VerifyMobileOtpRequest.class,
                VERIFY_UPDATE_MOBILE, "POST",AbhaSteps.verify_update_mobile_otp)
                .map(response -> {
                    if (Boolean.TRUE.equals(response.get("success"))) {
                        return AbhaFlowResponse.success(
                                "Mobile number updated successfully.",
                                response,
                                AbhaSteps.get_abha_address_suggestions,
                                "Now, select an address from the suggestions.",
                                "IdSuggestionRequest"
                        );
                    } else {
                        return AbhaFlowResponse.error("Failed to verify OTP: " + response.get("error"));
                    }
                });
    }

    /**
     * <h3>Handles Step 5: Get ABHA Address Suggestions</h3>
     * This method fetches a list of suggested ABHA addresses (also known as Health IDs) based on the user's details.
     *
     * <h3>Workflow:</h3>
     * <ol>
     *     <li>Converts the payload to an {@link IdSuggestionRequest} DTO and validates it.</li>
     *     <li>Makes a GET request to the {@code /get_abha_address_suggestions} endpoint with the txnId as a query parameter.</li>
     *     <li>On success, returns the list of suggestions and sets the next step to {@code final_register}.</li>
     * </ol>
     *
     * @param payload The input payload, expected to contain "txnId".
     * @return A {@link Mono} emitting an {@link AbhaFlowResponse} for the next stage.
     */
    public Mono<AbhaFlowResponse> handleGetAbhaAddressSuggestions(Object payload) {
        return validateAndProcess(payload, IdSuggestionRequest.class,
                GET_ABHA_ADDRESS_SUGGESTIONS, "GET",AbhaSteps.get_abha_address_suggestions)
                .map(response -> {
                    if (Boolean.TRUE.equals(response.get("success"))) {
                        return AbhaFlowResponse.success(
                                "Here are your ABHA address suggestions.",
                                response,
                                AbhaSteps.final_register,
                                "Select an address and proceed to final registration.",
                                "CreateAbhaAddressRequestDto"
                        );
                    } else {
                        return AbhaFlowResponse.error("Failed to fetch address suggestions: " + response.get("error"));
                    }
                });
    }
    /**
     * <h3>Handles Step 6: Final ABHA Registration</h3>
     * This is the final step where the user's chosen ABHA address is created and linked to their profile.
     *
     * <h3>Workflow:</h3>
     * <ol>
     *     <li>Converts the payload to a {@link CreateAbhaAddressRequestDto} DTO and validates it.</li>
     *     <li>Makes a POST request to the {@code /final_abha_registration} endpoint.</li>
     *     <li>On success, returns the final ABHA details. The flow is now complete, so the next step is null.</li>
     * </ol>
     *
     * @param payload The input payload, expected to contain "txnId" and the chosen "abhaAddress".
     * @return A {@link Mono} emitting the final {@link AbhaFlowResponse}.
     */
    private Mono<AbhaFlowResponse> handleFinalRegister(Object payload) {
        return validateAndProcess(payload, CreateAbhaAddressRequestDto.class,
                FINAL_ABHA_REGISTRATION, "POST",AbhaSteps.final_register)
                .map(response -> {
                    if (Boolean.TRUE.equals(response.get("success"))) {
                        return AbhaFlowResponse.success(
                                "ABHA registered successfully.",
                                response,
                                null,
                                null,
                                null
                        );
                    } else {
                        return AbhaFlowResponse.error("Failed to complete ABHA registration: " + response.get("error"));
                    }
                });
    }


    /**
     * Generic helper to centralize DTO validation and API call processing for a given step.
     *
     * @param payload     The raw input payload for the step.
     * @param dtoClass    The target DTO class for validation.
     * @param endpoint    The API endpoint to call.
     * @param method      The HTTP method (e.g., "POST", "GET").
     * @param currentStep The current {@link AbhaSteps} being processed.
     * @param <T>         The type of the DTO.
     * @return A {@link Mono} emitting a map with the API response and a success flag.
     */
    <T> Mono<Map<String, Object>> validateAndProcess(Object payload,
                                                     Class<T> dtoClass,
                                                     String endpoint,
                                                     String method,
                                                     AbhaSteps currentStep) {
        try {
            if (payload instanceof String) {
                payload = objectMapper.readValue((String) payload, Map.class);
            }

            T dto = objectMapper.convertValue(payload, dtoClass);

            // Validate DTO
            validateAbhaData(dto);

            // Process the step
            return processStep(endpoint, payload, method,currentStep);

        } catch (ValidationError e) {
            LogUtil.logger.error("Validation error for {}: {}", dtoClass.getSimpleName(), e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Validation failed: " + e.getMessage());
            return Mono.just(errorResponse);
        } catch (Exception e) {
            LogUtil.logger.error("Error processing {} request", dtoClass.getSimpleName(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Processing error: " + e.getMessage());
            return Mono.just(errorResponse);
        }
    }


    /**
     * Validates a DTO object using the configured JSR-380 validator.
     *
     * @param data The DTO to validate.
     * @param <T>  The type of the DTO.
     * @throws ValidationError if validation fails.
     */
    <T> void validateAbhaData(T data) {
        Set<ConstraintViolation<T>> violations = validator.validate(data);
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            throw new ValidationError("Validation failed: " + errorMessage);
        }
    }

    /**
     * Processes an individual step by encrypting the necessary fields and calling the appropriate API endpoint.
     *
     * @param endpoint    The API endpoint for the step.
     * @param payload     The request payload.
     * @param method      The HTTP method.
     * @param currentStep The current step, used to determine which fields to encrypt.
     * @return A {@link Mono} with the API response.
     */
    private Mono<Map<String, Object>> processStep(String endpoint, Object payload, String method, AbhaSteps currentStep) {
        if (endpoint == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Step processed locally without API call");
            return Mono.just(response);
        }

        try {
            Map<String, Object> requestBody = convertToMap(payload);

            if (currentStep == AbhaSteps.update_mobile) {
                if (requestBody.containsKey("updateValue")) {
                    String mobile = String.valueOf(requestBody.get("updateValue"));
                    String encryptedMobile = encryptionUtilities.encryptDataForAbha(mobile);
                    requestBody.put("updateValue", encryptedMobile);
                }
            }

            // Encrypt fields like aadhaar or otp
            if (requestBody.containsKey("aadhaar")) {
                String aadhaar = String.valueOf(requestBody.get("aadhaar"));
                String encryptedAadhaar = encryptionUtilities.encryptDataForAbha(aadhaar);
                requestBody.put("aadhaar", encryptedAadhaar);
            }
            if (requestBody.containsKey("otp")) {
                String otp = String.valueOf(requestBody.get("otp"));
                String encryptedOtp = encryptionUtilities.encryptDataForAbha(otp);
                requestBody.put("otp", encryptedOtp);
            }

            ParameterizedTypeReference<Map<String, Object>> responseType =
                    new ParameterizedTypeReference<>() {
                    };

            Mono<Map<String, Object>> apiResponse;

            if ("POST".equals(method)) {
                apiResponse = post(endpoint, requestBody, responseType);
            } else if ("GET".equals(method)) {
                Map<String, String> queryParams = requestBody.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> String.valueOf(entry.getValue())
                        ));
                apiResponse = get(endpoint, queryParams, responseType);
            } else {
                throw new IllegalArgumentException("Unsupported HTTP method: " + method);
            }

            return apiResponse
                    .map(response -> {
                        Map<String, Object> result = new HashMap<>();
                        result.put("response", response);
                        result.put("success", true);
                        return result;
                    })
                    .onErrorResume(error -> {
                        LogUtil.logger.error("API call failed for endpoint: {}", endpoint, error);
                        Map<String, Object> errorResponse = new HashMap<>();
                        errorResponse.put("error", "API call failed: " + error.getMessage());
                        errorResponse.put("success", false);
                        return Mono.just(errorResponse);
                    });

        } catch (Exception e) {
            LogUtil.logger.error("Error processing step for endpoint: {}", endpoint, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Step processing failed: " + e.getMessage());
            errorResponse.put("success", false);
            return Mono.just(errorResponse);
        }
    }


    /**
     * <h3>Converts a Generic Payload to a Map</h3>
     * A utility method to convert a generic payload object into a {@code Map<String, Object>}.
     * It is designed to handle various input types gracefully, making the service's internal data handling more robust.
     *
     * <h3>What it does:</h3>
     * This method inspects the type of the input {@code payload} and converts it to a Map. It supports:
     * <ul>
     *     <li>Objects that are already an instance of {@link Map}.</li>
     *     <li>JSON strings that can be parsed into a Map.</li>
     *     <li>Any other Plain Old Java Object (POJO) or DTO, which will be converted to a Map by Jackson's {@code convertValue}.</li>
     * </ul>
     *
     * <h3>Parameters:</h3>
     * <ul>
     *     <li><b>payload (Object):</b> The input object to be converted.</li>
     * </ul>
     *
     * <h3>Validation Rules:</h3>
     * <ul>
     *     <li>If the payload is a {@link String}, it must be a valid JSON object representation. If parsing fails, it throws a {@link ValidationError}.</li>
     * </ul>
     *
     * <h3>Input:</h3>
     * An {@link Object} that can be a {@code Map}, a JSON {@code String}, or a DTO.
     *
     * <h3>Output:</h3>
     * A {@code Map<String, Object>} representation of the input payload.
     *
     * <h3>Example Usage (Internal):</h3>
     * <pre>{@code
     * // Case 1: Payload is already a Map
     * Map<String, Object> mapPayload = new HashMap<>();
     * mapPayload.put("key", "value");
     * Map<String, Object> result1 = convertToMap(mapPayload);
     *
     * // Case 2: Payload is a JSON String
     * String jsonPayload = "{\"key\":\"value\"}";
     * Map<String, Object> result2 = convertToMap(jsonPayload);
     *
     * // Case 3: Payload is a DTO
     * VerifyAadhaarOtpDto dtoPayload = new VerifyAadhaarOtpDto("123456", "txn-123", "9876543210");
     * Map<String, Object> result3 = convertToMap(dtoPayload);
     * }</pre>
     *
     * <h3>Expected Output (Internal):</h3>
     * <pre>{@code
     * // For all three cases above, the output would be a Map equivalent to:
     * // { "key": "value" } for the first two, and
     * // { "otp": "123456", "txnId": "txn-123", "mobile": "9876543210" } for the third.
     * }</pre>
     *
     * @param payload The object to convert (can be a Map, a JSON string, or a DTO).
     * @return The resulting Map.
     * @throws ValidationError if the payload is an unsupported type or an invalid JSON string.
     */
    private Map<String, Object> convertToMap(Object payload) {
        if (payload instanceof Map) {
            return (Map<String, Object>) payload;
        } else if (payload instanceof String) {
            try {
                return objectMapper.readValue((String) payload, Map.class);
            } catch (Exception e) {
                throw new ValidationError("Invalid JSON format: " + e.getMessage());
            }
        } else {
            return objectMapper.convertValue(payload, new TypeReference<Map<String, Object>>() {});
        }
    }


    /**
     * <h3>Safely Extracts Mobile Number from Request Payload</h3>
     * A resilient helper method designed to extract the "mobile" field from the request payload.
     *
     * <h3>What it does:</h3>
     * This method attempts to convert the given payload to a Map and retrieve the value associated with the "mobile" key.
     * It is designed to fail gracefully by returning {@code null} instead of throwing an exception if the key is missing
     * or if any other error occurs during extraction. This prevents the main workflow from crashing due to malformed input.
     *
     * <h3>Parameters:</h3>
     * <ul>
     *     <li><b>payload (Object):</b> The request payload, expected to be convertible to a Map containing a "mobile" key.</li>
     * </ul>
     *
     * <h3>Input:</h3>
     * An {@link Object} representing the request payload.
     *
     * <h3>Output:</h3>
     * The mobile number as a {@link String}, or {@code null} if it cannot be found or an error occurs.
     *
     * <h3>Example Usage (Internal):</h3>
     * <pre>{@code
     * // Payload with a mobile number
     * Map<String, Object> validPayload = Map.of("mobile", "9876543210", "txnId", "123");
     * String mobile1 = extractMobileFromPayload(validPayload);
     *
     * // Payload without a mobile number
     * Map<String, Object> invalidPayload = Map.of("txnId", "123");
     * String mobile2 = extractMobileFromPayload(invalidPayload);
     * }</pre>
     *
     * <h3>Expected Output (Internal):</h3>
     * <pre>{@code
     * // mobile1 will be "9876543210"
     * // mobile2 will be null
     * }</pre>
     *
     * @param payload The request payload.
     * @return The mobile number as a string, or null if not found or an error occurs.
     */
    private String extractMobileFromPayload(Object payload) {
        try {
            Map<String, Object> payloadMap = convertToMap(payload);
            return (String) payloadMap.get("mobile");
        } catch (Exception e) {
            LogUtil.logger.warn("Failed to extract mobile from payload", e);
            return null;
        }
    }

    /**
     * <h3>Safely Extracts Mobile Number from Nested API Response</h3>
     * A resilient helper method to extract the mobile number from the complex, nested structure of an API response.
     *
     * <h3>What it does:</h3>
     * This method navigates a specific path within the API response map ({@code response -> ABHAProfile -> mobile})
     * to find the user's mobile number. It is built to handle cases where parts of the path are missing
     * (e.g., no "ABHAProfile" object) without throwing {@code NullPointerException} or {@code ClassCastException}.
     * It logs any issues as warnings and returns {@code null}.
     *
     * <h3>Parameters:</h3>
     * <ul>
     *     <li><b>response (Map&lt;String, Object&gt;):</b> The wrapped API response map, as returned by the {@code processStep} method.</li>
     * </ul>
     *
     * <h3>Input:</h3>
     * A {@code Map<String, Object>} representing the API response. The expected structure is:
     * {@code { "response": { "ABHAProfile": { "mobile": "..." } } } }
     *
     * <h3>Output:</h3>
     * The mobile number as a {@link String}, or {@code null} if it cannot be found at the expected path or an error occurs.
     *
     * <h3>Example Usage (Internal):</h3>
     * <pre>{@code
     * // A typical successful response structure
     * Map<String, Object> successResponse = Map.of(
     *     "response", Map.of(
     *         "ABHAProfile", Map.of(
     *             "mobile", "9876543210",
     *             "name", "John Doe"
     *         )
     *     ),
     *     "success", true
     * );
     * String mobile1 = extractMobileFromResponse(successResponse);
     *
     * // A response where the path is broken or missing
     * Map<String, Object> errorResponse = Map.of("response", Map.of("error", "invalid data"));
     * String mobile2 = extractMobileFromResponse(errorResponse);
     * }</pre>
     *
     * <h3>Expected Output (Internal):</h3>
     * <pre>{@code
     * // mobile1 will be "9876543210"
     * // mobile2 will be null
     * }</pre>
     *
     * @param response The API response map.
     * @return The mobile number as a string, or null if not found or an error occurs.
     */
    private String extractMobileFromResponse(Map<String, Object> response) {
        try {
            Map<String, Object> responseData = (Map<String, Object>) response.get("response");
            if (responseData != null) {
                Map<String, Object> abhaProfile = (Map<String, Object>) responseData.get("ABHAProfile");
                if (abhaProfile != null) {
                    return (String) abhaProfile.get("mobile");
                }
            }
            return null;
        } catch (Exception e) {
            LogUtil.logger.warn("Failed to extract mobile from response", e);
            return null;
        }
    }

}

