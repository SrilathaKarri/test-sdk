package carestack.ai;

import carestack.ai.dto.FhirBundleDTO;
import carestack.ai.dto.ProcessDSDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import carestack.base.enums.CaseType;
import carestack.base.utils.Constants;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AiServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private EncryptionUtilities encryptionUtilities;

    @Mock
    private Validator validator;

    private ObjectMapper objectMapper;

    private AiService aiService;


    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();

        // Manually inject the mock EncryptionUtilities
        aiService = new AiService(objectMapper, webClient, validator);
        aiService.encryptionUtilities = encryptionUtilities;
    }

    @Nested
    @DisplayName("generateDischargeSummary")
    class GenerateDischargeSummaryTests {

        @Test
        @DisplayName("Should generate discharge summary successfully")
        void testGenerateDischargeSummarySuccess() {
            // Given
            ProcessDSDto dto = new ProcessDSDto();
            dto.setFiles(List.of("https://test.com/file.pdf"));
            dto.setPublicKey("dummy-key");

            when(validator.validate(dto)).thenReturn(Set.of());
            when(encryptionUtilities.encryption(any())).thenReturn("encryptedData");

            AiService spyService = Mockito.spy(aiService);
            doReturn(Mono.just("Success")).when(spyService)
                    .post(eq(Constants.GENERATE_CASE_SHEET_SUMMARY_URL), any(), any(ParameterizedTypeReference.class));

            Mono<String> result = spyService.generateDischargeSummary(dto);

            StepVerifier.create(result)
                    .expectNext("Success")
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should fail validation with missing required fields")
        void testGenerateDischargeSummaryValidationError() {
            ProcessDSDto dto = new ProcessDSDto(); // missing required fields

            Mono<String> result = aiService.generateDischargeSummary(dto);

            StepVerifier.create(result)
                    .expectError(RuntimeException.class)
                    .verify();
        }

        @Test
        @DisplayName("Should handle encryption or HTTP failure")
        void testGenerateDischargeSummaryHttpError() {
            ProcessDSDto dto = new ProcessDSDto();
            dto.setFiles(List.of("file1"));
            dto.setPublicKey("key");

            when(encryptionUtilities.encryption(any()))
                    .thenThrow(new RuntimeException("Encryption failed"));

            Mono<String> result = aiService.generateDischargeSummary(dto);

            StepVerifier.create(result)
                    .expectErrorMatches(error ->
                            error instanceof RuntimeException &&
                                    error.getMessage().contains("Error preparing discharge summary"))
                    .verify();
        }
    }

    @Nested
    @DisplayName("generateFhirBundle")
    class GenerateFhirBundleTests {

        @Test
        @DisplayName("Should generate FHIR bundle successfully")
        void testGenerateFhirBundleSuccess() {
            FhirBundleDTO dto = new FhirBundleDTO();
            dto.setExtractedData(Map.of("key", "value"));
            dto.setCaseType(CaseType.OP_CONSULTATION);

            when(encryptionUtilities.encryption(any())).thenReturn("encryptedFhirData");

            AiService spy = spy(aiService);
            doReturn(Mono.just(Map.of("status", "ok")))
                    .when(spy)
                    .post(eq(Constants.GENERATE_FHIR_BUNDLE_URL), any(), any());

            Mono<Map<String, Object>> result = spy.generateFhirBundle(dto);

            StepVerifier.create(result)
                    .expectNextMatches(map -> "ok".equals(map.get("status")))
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should fail validation on FHIR bundle input")
        void testGenerateFhirBundleValidationFail() {
            // Given: an invalid FhirBundleDTO
            FhirBundleDTO dto = new FhirBundleDTO();

            // Mock a validation error
            ConstraintViolation<FhirBundleDTO> mockViolation = mock(ConstraintViolation.class);
            when(mockViolation.getMessage()).thenReturn("Invalid field");

            Set<ConstraintViolation<FhirBundleDTO>> violations = Set.of(mockViolation);
            when(validator.validate(dto)).thenReturn(violations);

            // When
            Mono<Map<String, Object>> result = aiService.generateFhirBundle(dto);

            // Then: Expect IllegalArgumentException
            StepVerifier.create(result)
                    .expectError(IllegalArgumentException.class)
                    .verify();
        }
    }

    @Nested
    @DisplayName("extractEntity")
    class ExtractEntityTests {

        @Test
        @DisplayName("Should extract entity successfully")
        void testExtractEntitySuccess() {
            Map<String, Object> payload = Map.of(
                    "extractedData", Map.of("patient", "test"),
                    "caseType", "DISCHARGE_SUMMARY"
            );

            AiService spy = spy(aiService);
            doReturn(Mono.just(Map.of("entity", "data")))
                    .when(spy)
                    .post(eq(Constants.HEALTH_INFORMATION_EXTRACTION_URL), any(), any());

            StepVerifier.create(spy.extractEntity(payload))
                    .expectNextMatches(result -> ((Map<?, ?>) result).containsKey("entity"))
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should throw error when extractedData missing")
        void testExtractEntityMissingExtractedData() {
            Map<String, Object> payload = Map.of("caseType", "DISCHARGE_SUMMARY");

            StepVerifier.create(aiService.extractEntity(payload))
                    .expectError(RuntimeException.class)
                    .verify();
        }

        @Test
        @DisplayName("Should throw error when caseType missing")
        void testExtractEntityMissingCaseType() {
            Map<String, Object> payload = Map.of("extractedData", Map.of("a", "b"));

            StepVerifier.create(aiService.extractEntity(payload))
                    .expectError(RuntimeException.class)
                    .verify();
        }
    }
}
