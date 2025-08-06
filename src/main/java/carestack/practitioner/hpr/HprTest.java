package carestack.practitioner.hpr;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
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
import carestack.base.errors.EhrApiError;
import carestack.base.utils.Constants;
import carestack.practitioner.hpr.dto.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static carestack.base.utils.Constants.*;

/**
 * Unit tests for the {@link Hpr} service class.
 * <p>
 * This class provides comprehensive test coverage for the {@link Hpr} service,
 * ensuring all public methods for the HPR registration workflow behave as expected.
 * It uses JUnit 5, Mockito for mocking dependencies, and Project Reactor's
 * {@link StepVerifier} to test reactive streams declaratively.
 * </p>
 */
@MockitoSettings(strictness = Strictness.WARN)
 class HprTest {

    @Mock private WebClient webClient;
    @Mock private Validator validator;
    @Mock private ObjectMapper objectMapper;

    @Spy
    @InjectMocks
    private Hpr hpr;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGenerateAadhaarOtp_Success() {
        GenerateAadhaarOtpRequest request = new GenerateAadhaarOtpRequest("123456789012");
        GenerateAadhaarOtpResponse response = new GenerateAadhaarOtpResponse();
        response.setTxnId("txn-123");
        response.setMobileNumber("xxxxxx1234");

        given(validator.validate(request)).willReturn(Set.of());
        willReturn(Mono.just(response)).given(hpr)
                .post(eq(Constants.GENERATE_AADHAAR_OTP), eq(request), any(ParameterizedTypeReference.class));

        StepVerifier.create(hpr.generateAadhaarOtp(request))
                .expectNext(response)
                .verifyComplete();
    }

    @Test
    void testVerifyAadhaarOtp_Success() {
        VerifyAadhaarOtpRequest request = new VerifyAadhaarOtpRequest();
        request.setTxnId("txn-123");
        request.setDomainName("@hpr.abdm");
        request.setIdType("hprid");
        request.setOtp("123456");

        VerifyAadhaarOtpResponse response = new VerifyAadhaarOtpResponse();

        given(validator.validate(request)).willReturn(Set.of());
        willReturn(Mono.just(response)).given(hpr)
                .post(eq(VERIFY_AADHAAR_OTP), eq(request), any(ParameterizedTypeReference.class));

        StepVerifier.create(hpr.verifyAadhaarOtp(request))
                .expectNext(response)
                .verifyComplete();
    }

    @Test
    void testCheckAccountExist_HprResponse() {
        CheckAccountRequest request = new CheckAccountRequest();
        request.setTxnId("txn-001");
        request.setPreverifiedCheck(true);

        HprAccountResponse response = new HprAccountResponse();
        response.setIsNew(false);

        given(validator.validate(request)).willReturn(Set.of());
        willReturn(Mono.just(response)).given(hpr)
                .post(eq(CHECK_HPR_ACCOUNT), eq(request), any(ParameterizedTypeReference.class));

        StepVerifier.create(hpr.checkAccountExist(request))
                .expectNext(response)
                .verifyComplete();
    }

    @Test
    void testDemographicAuthViaMobile_Success() {
        DemographicAuthViaMobileRequest request = new DemographicAuthViaMobileRequest();
        request.setTxnId("txn-001");
        request.setMobileNumber("9876543210");

        DemographicAuthViaMobileResponse response = new DemographicAuthViaMobileResponse();
        response.setVerified(true);

        given(validator.validate(request)).willReturn(Set.of());
        willReturn(Mono.just(response)).given(hpr)
                .post(eq(DEMOGRAPHIC_AUTH_MOBILE), eq(request), any(ParameterizedTypeReference.class));

        StepVerifier.create(hpr.demographicAuthViaMobile(request))
                .expectNext(response)
                .verifyComplete();
    }

    @Test
    void testGenerateMobileOtp_Success() {
        GenerateMobileOtpRequest request = new GenerateMobileOtpRequest();
        request.setTxnId("txn-001");
        request.setMobile("9876543210");

        MobileOtpResponse response = new MobileOtpResponse();
        response.setTxnId("txn-001");

        given(validator.validate(request)).willReturn(Set.of());
        willReturn(Mono.just(response)).given(hpr)
                .post(eq(GENERATE_MOBILE_OTP), eq(request), any(ParameterizedTypeReference.class));

        StepVerifier.create(hpr.generateMobileOtp(request))
                .expectNext(response)
                .verifyComplete();
    }

    @Test
    void testVerifyMobileOtp_Success() {
        VerifyMobileOtpRequest request = new VerifyMobileOtpRequest();
        request.setTxnId("txn-001");
        request.setOtp("123456");

        MobileOtpResponse response = new MobileOtpResponse();
        response.setTxnId("txn-001");
        response.setMobileNumber("xxxxxx8976");

        given(validator.validate(request)).willReturn(Set.of());
        willReturn(Mono.just(response)).given(hpr)
                .post(eq(VERIFY_MOBILE_OTP), eq(request), any(ParameterizedTypeReference.class));

        StepVerifier.create(hpr.verifyMobileOtp(request))
                .expectNext(response)
                .verifyComplete();
    }

    @Test
    void testGetHprSuggestion_Success() {
        IdSuggestionRequest request = new IdSuggestionRequest();
        request.setTxnId("txn-001");

        List<String> suggestions = List.of("user1098", "test12");

        given(validator.validate(request)).willReturn(Set.of());
        willReturn(Mono.just(suggestions)).given(hpr)
                .post(eq(GET_HPR_SUGGESTIONS), eq(request), any(ParameterizedTypeReference.class));

        StepVerifier.create(hpr.getHprSuggestion(request))
                .expectNext(suggestions)
                .verifyComplete();
    }

    @Test
    void testCreateHprIdWithPreVerified_Success() {
        // Given
        CreateHprIdWithPreVerifiedRequest request = CreateHprIdWithPreVerifiedRequest.builder()
                .address("123 Main St")
                .dayOfBirth("15")
                .districtCode("HYD")
                .email("test@example.com")
                .firstName("John")
                .hpCategoryCode("HP01")
                .hpSubCategoryCode("SUB01")
                .hprId("HPRID12345")
                .lastName("Doe")
                .middleName("A.")
                .monthOfBirth("07")
                .password("SecureP@ss123")
                .pincode("500001")
                .profilePhoto("base64encodedphoto")
                .stateCode("TS")
                .txnId("txn-001")
                .yearOfBirth("1990")
                .build();

        CreateHprIdWithPreVerifiedResponse response = CreateHprIdWithPreVerifiedResponse.builder()
                .hprId("HPR123456")
                .token("some_token_string")
                .hprIdNumber("HPRN123456")
                .name("John A. Doe")
                .gender("M")
                .yearOfBirth("1990")
                .monthOfBirth("07")
                .dayOfBirth("15")
                .firstName("John")
                .lastName("Doe")
                .middleName("A.")
                .stateCode("TS")
                .districtCode("HYD")
                .stateName("Telangana")
                .districtName("Hyderabad")
                .email("test@example.com")
                .kycPhoto("kyc_photo_url")
                .mobile("9876543210")
                .categoryId(1)
                .subCategoryId(101)
                .authMethods(List.of("OTP", "BIOMETRIC"))
                .isNew(true)
                .build();


        given(validator.validate(request)).willReturn(Set.of());
        willReturn(Mono.just(response)).given(hpr)
                .post(eq(CREATE_HPR), eq(request), any(ParameterizedTypeReference.class));

        // When & Then
        StepVerifier.create(hpr.createHprIdWithPreVerified(request))
                .expectNext(response)
                .verifyComplete();
    }

    @Test
    void testCreateHpr_ByStepName_InvalidStep() {
        StepVerifier.create(hpr.createHprByStepName("invalid_step", Map.of()))
                .expectNextMatches(res -> res.get("error").equals("Invalid step: invalid_step"))
                .verifyComplete();
    }

    @Test
    void testGenerateAadhaarOtp_ValidationError() {
        GenerateAadhaarOtpRequest request = new GenerateAadhaarOtpRequest("invalid");
        given(validator.validate(request)).willReturn(Set.of(
                mock(jakarta.validation.ConstraintViolation.class)  // simulate error
        ));

        StepVerifier.create(hpr.generateAadhaarOtp(request))
                .expectError(EhrApiError.class)
                .verify();
    }
}

