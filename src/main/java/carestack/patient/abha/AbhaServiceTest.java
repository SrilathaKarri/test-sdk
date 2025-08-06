package carestack.patient.abha;

import carestack.practitioner.hpr.dto.IdSuggestionRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import carestack.patient.abha.DTO.AbhaFlowResponse;
import carestack.patient.abha.DTO.CreateAbhaAddressRequestDto;
import carestack.patient.abha.DTO.UpdateMobile;
import carestack.patient.abha.DTO.VerifyAadhaarOtpDto;
import carestack.patient.abha.enums.AbhaSteps;
import carestack.ai.EncryptionUtilities;
import carestack.base.errors.ValidationError;
import carestack.practitioner.hpr.dto.GenerateAadhaarOtpRequest;
import carestack.practitioner.hpr.dto.VerifyMobileOtpRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static carestack.base.utils.Constants.*;

@MockitoSettings(strictness = Strictness.LENIENT)
 class AbhaServiceTest {

    @Mock private WebClient webClient;
    @Mock private Validator validator;
    @Mock private ObjectMapper objectMapper;
    @Mock private EncryptionUtilities encryptionUtilities;
    @Spy @InjectMocks private AbhaService abhaService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateAbha_InvalidStep() {
        StepVerifier.create(abhaService.createAbhaByStepName("invalid_step", Map.of()))
                .expectNextMatches(res -> res.get("error").equals("Invalid step: invalid_step"))
                .verifyComplete();
    }

    @Test
    void testRegisterWithAadhaar_Success() {
        // Instead of passing the DTO directly, pass a map (which is expected by convertToMap)
        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("aadhaar", "123456789012");

        Map<String, Object> mockApiResponse = new HashMap<>();
        mockApiResponse.put("txnId", "sampleTxnId");

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("response", mockApiResponse);
        responseMap.put("success", true);

        // Simulate empty validation errors
        given(validator.validate(any())).willReturn(Set.of());

        // Mock objectMapper.convertValue
        given(objectMapper.convertValue(any(), eq(GenerateAadhaarOtpRequest.class)))
                .willReturn(new GenerateAadhaarOtpRequest("123456789012"));

        // Mock encryptor behavior
        given(encryptionUtilities.encryptDataForAbha("123456789012")).willReturn("encryptedAadhaar");

        // Mock post-call
        willReturn(Mono.just(responseMap)).given(abhaService)
                .post(eq(REGISTER_WITH_AADHAAR), any(), any(ParameterizedTypeReference.class));

        StepVerifier.create(abhaService.createAbha(AbhaSteps.register_with_aadhaar, requestPayload))
                .expectNextMatches(res -> {
                    return "OTP sent successfully to your registered mobile number.".equals(res.get("message")) &&
                            AbhaSteps.verify_aadhaar_otp.equals(res.get("next_step"));
                })
                .verifyComplete();
    }


    @Test
    void shouldReturnGetAbhaAddressSuggestionsWhenMobileMatches() {
        // Given
        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("mobile", "9876543210");
        requestPayload.put("otp", "123455");
        requestPayload.put("txnId", "353463656546");

        Map<String, Object> abhaProfile = new HashMap<>();
        abhaProfile.put("mobile", "9876543210");

        Map<String, Object> apiResponse = new HashMap<>();
        apiResponse.put("ABHAProfile", abhaProfile);

        Map<String, Object> successMap = new HashMap<>();
        successMap.put("response", apiResponse);
        successMap.put("success", true);

        doReturn(Mono.just(successMap))
                .when(abhaService)
                .validateAndProcess(
                        eq(requestPayload),
                        eq(VerifyAadhaarOtpDto.class),
                        eq(ENROLL_AADHAAR),
                        eq("POST"),
                        eq(AbhaSteps.verify_aadhaar_otp)
                );

        // When
        Mono<AbhaFlowResponse> result = abhaService.handleVerifyAadhaarOtp(requestPayload);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getMessage()).isEqualTo("Aadhaar OTP verified successfully.");
                    assertThat(response.getData()).isEqualTo(successMap);
                    assertThat(response.getNextStep()).isEqualTo(AbhaSteps.get_abha_address_suggestions);
                    assertThat(response.getNextStepPayloadHint()).isEqualTo("Mobile number is already verified. Proceed to select ABHA address.");
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnUpdateMobileStepWhenMobileMismatch() {
        // Given
        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("mobile", "9876543210");

        Map<String, Object> abhaProfile = new HashMap<>();
        abhaProfile.put("mobile", "9999999999"); // different

        Map<String, Object> apiResponse = new HashMap<>();
        apiResponse.put("ABHAProfile", abhaProfile);

        Map<String, Object> successMap = new HashMap<>();
        successMap.put("response", apiResponse);
        successMap.put("success", true);

        given(abhaService.validateAndProcess(
                eq(requestPayload),
                eq(VerifyAadhaarOtpDto.class),
                eq(ENROLL_AADHAAR),
                eq("POST"),
                eq(AbhaSteps.verify_aadhaar_otp)))
                .willReturn(Mono.just(successMap));

        // When
        Mono<AbhaFlowResponse> result = abhaService.handleVerifyAadhaarOtp(requestPayload);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getMessage()).isEqualTo("Aadhaar OTP verified successfully.");
                    assertThat(response.getNextStep()).isEqualTo(AbhaSteps.update_mobile);
                    assertThat(response.getNextStepPayloadHint()).isEqualTo("Mobile number needs to be updated. Please proceed with mobile update.");
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnErrorWhenOtpVerificationFails() {
        // Given
        Map<String, Object> requestPayload = new HashMap<>();

        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("success", false);
        errorMap.put("error", "Invalid OTP");

        given(abhaService.validateAndProcess(
                eq(requestPayload),
                eq(VerifyAadhaarOtpDto.class),
                eq(ENROLL_AADHAAR),
                eq("POST"),
                eq(AbhaSteps.verify_aadhaar_otp)))
                .willReturn(Mono.just(errorMap));

        // When
        Mono<AbhaFlowResponse> result = abhaService.handleVerifyAadhaarOtp(requestPayload);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getMessage()).isEqualTo("Failed to verify OTP: Invalid OTP");
                    assertThat(response.getData()).isNull();
                    assertThat(response.getNextStep()).isNull();
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnSuccessResponseWhenUpdateMobileOtpSent() {
        // Given
        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("mobile", "9876543211");

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("success", true);

        given(abhaService.validateAndProcess(
                eq(requestPayload),
                eq(UpdateMobile.class),
                eq(UPDATE_MOBILE),
                eq("POST"),
                eq(AbhaSteps.update_mobile)
        )).willReturn(Mono.just(responseMap));

        // When
        Mono<AbhaFlowResponse> result = abhaService.handleUpdateMobile(requestPayload);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getMessage()).isEqualTo("OTP sent to your new mobile number.");
                    assertThat(response.getData()).isEqualTo(responseMap);
                    assertThat(response.getNextStep()).isEqualTo(AbhaSteps.verify_update_mobile_otp);
                    assertThat(response.getNextStepPayloadHint()).isEqualTo("Please enter the OTP sent to your new mobile number.");
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnErrorResponseWhenUpdateMobileOtpFails() {
        // Given
        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("mobile", "1234567890");

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("success", false);
        responseMap.put("error", "Mobile number is invalid");

        given(abhaService.validateAndProcess(
                eq(requestPayload),
                eq(UpdateMobile.class),
                eq(UPDATE_MOBILE),
                eq("POST"),
                eq(AbhaSteps.update_mobile)
        )).willReturn(Mono.just(responseMap));

        // When
        Mono<AbhaFlowResponse> result = abhaService.handleUpdateMobile(requestPayload);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getMessage()).isEqualTo("Failed to send OTP: Mobile number is invalid");
                    assertThat(response.getData()).isNull();
                    assertThat(response.getNextStep()).isNull();
                    assertThat(response.getNextStepPayloadHint()).isNull();
                })
                .verifyComplete();
    }


    @Test
    void shouldReturnSuccessWhenOtpVerificationIsSuccessful() {
        // Given
        Map<String, Object> payload = new HashMap<>();
        payload.put("otp", "123456");
        payload.put("txnId", "txn-123");

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("success", true);

        given(abhaService.validateAndProcess(
                eq(payload),
                eq(VerifyMobileOtpRequest.class),
                eq(VERIFY_UPDATE_MOBILE),
                eq("POST"),
                eq(AbhaSteps.verify_update_mobile_otp)
        )).willReturn(Mono.just(responseMap));

        // When
        Mono<AbhaFlowResponse> result = abhaService.handleVerifyUpdateMobileOtp(payload);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getMessage()).isEqualTo("Mobile number updated successfully.");
                    assertThat(response.getData()).isEqualTo(responseMap);
                    assertThat(response.getNextStep()).isEqualTo(AbhaSteps.get_abha_address_suggestions);
                    assertThat(response.getNextStepPayloadHint()).isEqualTo("Now, select an address from the suggestions.");
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnErrorWhenMobileOtpVerificationFails() {
        // Given
        Map<String, Object> payload = new HashMap<>();
        payload.put("otp", "999999");
        payload.put("txnId", "txn-999");

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("success", false);
        responseMap.put("error", "Invalid OTP");

        given(abhaService.validateAndProcess(
                eq(payload),
                eq(VerifyMobileOtpRequest.class),
                eq(VERIFY_UPDATE_MOBILE),
                eq("POST"),
                eq(AbhaSteps.verify_update_mobile_otp)
        )).willReturn(Mono.just(responseMap));

        // When
        Mono<AbhaFlowResponse> result = abhaService.handleVerifyUpdateMobileOtp(payload);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getMessage()).isEqualTo("Failed to verify OTP: Invalid OTP");
                    assertThat(response.getData()).isNull();
                    assertThat(response.getNextStep()).isNull();
                    assertThat(response.getNextStepPayloadHint()).isNull();
                })
                .verifyComplete();
    }

    @Test
    void shouldPropagateErrorWhenValidateAndProcessFails() {
        // Given
        Map<String, Object> payload = new HashMap<>();
        payload.put("otp", "123456");

        RuntimeException exception = new RuntimeException("Service error");

        given(abhaService.validateAndProcess(
                any(),
                eq(VerifyMobileOtpRequest.class),
                anyString(),
                anyString(),
                any()
        )).willReturn(Mono.error(exception));

        // When
        Mono<AbhaFlowResponse> result = abhaService.handleVerifyUpdateMobileOtp(payload);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().equals("Service error"))
                .verify();
    }


    @Test
    void shouldReturnSuccessWhenSuggestionsAreAvailable() {
        // Given
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "test-user");

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("success", true);
        responseMap.put("suggestions", List.of("test1@abdm", "test2@abdm"));

        // Mock validateAndProcess
        given(abhaService.validateAndProcess(
                eq(payload),
                eq(IdSuggestionRequest.class),
                eq(GET_ABHA_ADDRESS_SUGGESTIONS),
                eq("GET"),
                eq(AbhaSteps.get_abha_address_suggestions)
        )).willReturn(Mono.just(responseMap));

        // When
        Mono<AbhaFlowResponse> result = abhaService.handleGetAbhaAddressSuggestions(payload);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getMessage()).isEqualTo("Here are your ABHA address suggestions.");
                    assertThat(response.getData()).isEqualTo(responseMap);
                    assertThat(response.getNextStep()).isEqualTo(AbhaSteps.final_register);
                    assertThat(response.getNextStepPayloadHint()).isEqualTo("Select an address and proceed to final registration.");
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnErrorWhenSuggestionsFetchFails() {
        // Given
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "fail-case");

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("success", false);
        responseMap.put("error", "No suggestions available");

        given(abhaService.validateAndProcess(
                eq(payload),
                eq(IdSuggestionRequest.class),
                eq(GET_ABHA_ADDRESS_SUGGESTIONS),
                eq("GET"),
                eq(AbhaSteps.get_abha_address_suggestions)
        )).willReturn(Mono.just(responseMap));

        // When
        Mono<AbhaFlowResponse> result = abhaService.handleGetAbhaAddressSuggestions(payload);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getMessage()).isEqualTo("Failed to fetch address suggestions: No suggestions available");
                    assertThat(response.getData()).isNull();
                    assertThat(response.getNextStep()).isNull();
                    assertThat(response.getNextStepPayloadHint()).isNull();
                })
                .verifyComplete();
    }

    @Test
    void shouldPropagateErrorIfValidateAndProcessFails() {
        // Given
        Map<String, Object> payload = Map.of("name", "throw-exception");

        RuntimeException error = new RuntimeException("Unexpected error");

        given(abhaService.validateAndProcess(
                any(),
                eq(IdSuggestionRequest.class),
                eq(GET_ABHA_ADDRESS_SUGGESTIONS),
                eq("GET"),
                eq(AbhaSteps.get_abha_address_suggestions)
        )).willReturn(Mono.error(error));

        // When
        Mono<AbhaFlowResponse> result = abhaService.handleGetAbhaAddressSuggestions(payload);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().equals("Unexpected error"))
                .verify();
    }

    @Test
    @DisplayName("should complete ABHA registration successfully when final_register is called")
    void testFinalRegisterSuccess() {
        // Given
        Map<String, Object> payload = new HashMap<>();
        payload.put("address", "sai@ndhm");
        payload.put("txnId","353453");

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("someKey", "someValue");

        Map<String, Object> fakeResponse = new HashMap<>();
        fakeResponse.put("success", true);
        fakeResponse.put("response", responseBody);

        doReturn(Mono.just(fakeResponse))
                .when(abhaService)
                .validateAndProcess(
                        any(), eq(CreateAbhaAddressRequestDto.class),
                        anyString(), eq("POST"), eq(AbhaSteps.final_register));

        // When
        Mono<Map<String, Object>> result = abhaService.createAbha(AbhaSteps.final_register, payload);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(map -> map.get("message").equals("ABHA registered successfully.") &&
                        map.get("data") instanceof Map)
                .verifyComplete();
    }

    @Test
    @DisplayName("should return error when ABHA registration fails")
    void testFinalRegisterFailure() {
        // Given
        Map<String, Object> fakePayload = new HashMap<>();
        fakePayload.put("address", "john@ndhm");

        Map<String, Object> fakeResponse = new HashMap<>();
        fakeResponse.put("success", false);
        fakeResponse.put("error", "Some backend error");

        doReturn(Mono.just(fakeResponse))
                .when(abhaService)
                .validateAndProcess(
                        any(), eq(CreateAbhaAddressRequestDto.class),
                        anyString(), eq("POST"), eq(AbhaSteps.final_register));

        // When
        Mono<Map<String, Object>> result = abhaService.createAbha(AbhaSteps.final_register, fakePayload);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(map ->
                        "Failed to complete ABHA registration: Some backend error".equals(map.get("message")) &&
                                map.get("data") == null &&
                                map.get("next_step") == null &&
                                map.get("next_step_payload_hint") == null
                )
                .verifyComplete();
    }


    @Test
    void testValidationErrorOnInvalidDto() {
        GenerateAadhaarOtpRequest request = new GenerateAadhaarOtpRequest("invalid");

        // Force validation to throw the actual error
        doThrow(new ValidationError("Invalid Aadhaar"))
                .when(abhaService).validateAbhaData(any());

        // Mock object mapper
        given(objectMapper.convertValue(any(), eq(GenerateAadhaarOtpRequest.class))).willReturn(request);

        StepVerifier.create(abhaService.createAbha(AbhaSteps.register_with_aadhaar, request))
                .expectNextMatches(res -> {
                    String message = (String) res.get("message");
                    return message != null && message.contains("Validation failed: Invalid Aadhaar");
                })
                .verifyComplete();
    }

}
