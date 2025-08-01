package carestack.documentLinking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import carestack.base.enums.DocLinkingEnums;
import carestack.base.errors.EhrApiError;
import carestack.base.errors.ErrorType;
import carestack.base.errors.ValidationError;
import carestack.documentLinking.dto.*;
import carestack.documentLinking.mappers.Mapper;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DocumentLinking Service Tests")
class DocumentLinkingTest {

    @Spy
    private ObjectMapper objectMapper;

    @Mock
    private WebClient webClient;

    @Mock
    private Mapper mapper;

    @InjectMocks
    private DocumentLinking documentLinking;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private HealthDocumentLinkingDTO validHealthDocDto;
    private CreateCareContextDTO validCreateCareContextDto;
    private UpdateVisitRecordsDTO validUpdateVisitRecordsDto;
    private LinkCareContextDTO validLinkCareContextDto;

    @BeforeEach
    void setUp() {
        // Common setup for DTOs to be used across tests
        validHealthDocDto = new HealthDocumentLinkingDTO();
        validHealthDocDto.setPatientReference("f8f8f8f8-f8f8-f8f8-f8f8-f8f8f8f8f8f8");
        validHealthDocDto.setPractitionerReference("a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1");
        validHealthDocDto.setAppointmentReference(UUID.randomUUID().toString());
        validHealthDocDto.setAppointmentStartDate("2023-10-27T10:00:00Z");
        validHealthDocDto.setAppointmentEndDate("2023-10-27T11:00:00Z");
        validHealthDocDto.setOrganizationId("org-123");
        validHealthDocDto.setMobileNumber("1234567890");
        validHealthDocDto.setHealthRecords(List.of(new HealthInformationDTO()));
        validHealthDocDto.setPatientAddress("123 Main St");
        validHealthDocDto.setPatientName("John Doe");
        validHealthDocDto.setHiType(DocLinkingEnums.HealthInformationTypes.OPConsultation);

        validCreateCareContextDto = new CreateCareContextDTO();
        validCreateCareContextDto.setPatientReference("f8f8f8f8-f8f8-f8f8-f8f8-f8f8f8f8f8f8");
        validCreateCareContextDto.setPractitionerReference("a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1");
        validCreateCareContextDto.setAppointmentReference(UUID.randomUUID().toString());
        validCreateCareContextDto.setAppointmentDate("10:00 AM - 11:00 AM");
        validCreateCareContextDto.setResendOtp(false);
        validCreateCareContextDto.setHiType(DocLinkingEnums.HealthInformationTypes.OPConsultation);

        validUpdateVisitRecordsDto = new UpdateVisitRecordsDTO();
        validUpdateVisitRecordsDto.setCareContextReference("cc-ref-123");
        validUpdateVisitRecordsDto.setPatientReference(UUID.randomUUID().toString());
        validUpdateVisitRecordsDto.setPractitionerReference(UUID.randomUUID().toString());
        validUpdateVisitRecordsDto.setAppointmentReference(UUID.randomUUID().toString());
        validUpdateVisitRecordsDto.setHealthRecords(List.of(new HealthInformationDTO()));

        validLinkCareContextDto = new LinkCareContextDTO();
        validLinkCareContextDto.setRequestId("req-123");
        validLinkCareContextDto.setAppointmentReference(UUID.randomUUID().toString());
        validLinkCareContextDto.setPatientAddress("abha-address");
        validLinkCareContextDto.setPatientReference(UUID.randomUUID().toString());
        validLinkCareContextDto.setCareContextReference("cc-ref-123");
        validLinkCareContextDto.setAuthMode(DocLinkingEnums.AuthMode.DEMOGRAPHICS);
        validLinkCareContextDto.setPatientName("Jane Doe");
    }

    private void mockWebClientPostChain() {
        lenient().when(webClient.post()).thenReturn(requestBodyUriSpec);
        lenient().when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodySpec);
        lenient().when(requestBodySpec.bodyValue(any(Map.class))).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        lenient().when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
    }


    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("validateData with null should throw ValidationError")
        void validateData_withNullData_shouldThrowValidationError() {
            // When & Then
            assertThrows(ValidationError.class, () -> documentLinking.validateData(null), "Input data cannot be null");
        }

        @Test
        @DisplayName("validateData with unsupported DTO should throw ValidationError")
        void validateData_withUnsupportedDto_shouldThrowValidationError() {
            // Given
            Object unsupportedDto = new Object();

            // When & Then
            assertThrows(ValidationError.class, () -> documentLinking.validateData(unsupportedDto), "Unsupported DTO type should throw error");
        }

        @Test
        @DisplayName("validateData with valid HealthDocumentLinkingDTO should not throw")
        void validateData_withValidHealthDocDto_shouldNotThrow() {
            assertDoesNotThrow(() -> documentLinking.validateData(validHealthDocDto));
        }

        @Test
        @DisplayName("validateData with invalid HealthDocumentLinkingDTO should throw ValidationError")
        void validateData_withInvalidHealthDocDto_shouldThrowValidationError() {
            validHealthDocDto.setPatientReference(null);
            assertThrows(ValidationError.class, () -> documentLinking.validateData(validHealthDocDto));
        }

        @Test
        @DisplayName("validateData with valid CreateCareContextDTO should not throw")
        void validateData_withValidCreateCareContextDto_shouldNotThrow() {
            assertDoesNotThrow(() -> documentLinking.validateData(validCreateCareContextDto));
        }

        @Test
        @DisplayName("validateData with invalid CreateCareContextDTO should throw ValidationError")
        void validateData_withInvalidCreateCareContextDto_shouldThrowValidationError() {
            validCreateCareContextDto.setAppointmentReference(null);
            assertThrows(ValidationError.class, () -> documentLinking.validateData(validCreateCareContextDto));
        }

        @Test
        @DisplayName("validateData with valid UpdateVisitRecordsDTO should not throw")
        void validateData_withValidUpdateVisitRecordsDto_shouldNotThrow() {
            assertDoesNotThrow(() -> documentLinking.validateData(validUpdateVisitRecordsDto));
        }

        @Test
        @DisplayName("validateData with invalid UpdateVisitRecordsDTO should throw ValidationError")
        void validateData_withInvalidUpdateVisitRecordsDto_shouldThrowValidationError() {
            validUpdateVisitRecordsDto.setCareContextReference(""); // empty string
            assertThrows(ValidationError.class, () -> documentLinking.validateData(validUpdateVisitRecordsDto));
        }

        @Test
        @DisplayName("validateData with valid LinkCareContextDTO should not throw")
        void validateData_withValidLinkCareContextDto_shouldNotThrow() {
            assertDoesNotThrow(() -> documentLinking.validateData(validLinkCareContextDto));
        }

        @Test
        @DisplayName("validateData with invalid LinkCareContextDTO should throw ValidationError")
        void validateData_withInvalidLinkCareContextDto_shouldThrowValidationError() {
            validLinkCareContextDto.setRequestId(null);
            assertThrows(ValidationError.class, () -> documentLinking.validateData(validLinkCareContextDto));
        }
    }

    @Nested
    @DisplayName("linkHealthDocument Workflow Tests")
    class LinkHealthDocumentWorkflowTests {

        @Test
        void linkHealthDocument_shouldSucceed_whenAllStepsAreSuccessful() {
            // Given
            CreateCareContextResponse careContextResponse = new CreateCareContextResponse("cc-ref-123", "req-123", null);

            // Mapper mocks
            lenient().when(mapper.mapToCareContextDTO(any(), any(), any(), any())).thenReturn(validCreateCareContextDto);
            lenient().when(mapper.mapToConsultationDTO(any(), any(), any(), any())).thenReturn(validUpdateVisitRecordsDto);
            lenient().when(mapper.mapToLinkCareContextDTO(any(), any(), any(), any())).thenReturn(validLinkCareContextDto);

            // Mock WebClient for first API call (create care context)
            WebClient.RequestBodyUriSpec requestBodyUriSpec1 = mock(WebClient.RequestBodyUriSpec.class);
            WebClient.RequestBodySpec requestBodySpec1 = mock(WebClient.RequestBodySpec.class);
            WebClient.RequestHeadersSpec requestHeadersSpec1 = mock(WebClient.RequestHeadersSpec.class);
            WebClient.ResponseSpec responseSpec1 = mock(WebClient.ResponseSpec.class);

            // Mock WebClient for second API call (update visit records)
            WebClient.RequestBodyUriSpec requestBodyUriSpec2 = mock(WebClient.RequestBodyUriSpec.class);
            WebClient.RequestBodySpec requestBodySpec2 = mock(WebClient.RequestBodySpec.class);
            WebClient.RequestHeadersSpec requestHeadersSpec2 = mock(WebClient.RequestHeadersSpec.class);
            WebClient.ResponseSpec responseSpec2 = mock(WebClient.ResponseSpec.class);

            // Mock WebClient for third API call (link care context)
            WebClient.RequestBodyUriSpec requestBodyUriSpec3 = mock(WebClient.RequestBodyUriSpec.class);
            WebClient.RequestBodySpec requestBodySpec3 = mock(WebClient.RequestBodySpec.class);
            WebClient.RequestHeadersSpec requestHeadersSpec3 = mock(WebClient.RequestHeadersSpec.class);
            WebClient.ResponseSpec responseSpec3 = mock(WebClient.ResponseSpec.class);

            // Mock the WebClient.post() calls to return the three different RequestBodyUriSpec mocks in sequence
            lenient().when(webClient.post()).thenReturn(requestBodyUriSpec1, requestBodyUriSpec2, requestBodyUriSpec3);

            // First call a chain (create care context)
            lenient().when(requestBodyUriSpec1.uri(any(String.class))).thenReturn(requestBodySpec1);
            lenient().when(requestBodySpec1.bodyValue(any())).thenReturn(requestHeadersSpec1);
            lenient().when(requestHeadersSpec1.retrieve()).thenReturn(responseSpec1);
            lenient().when(responseSpec1.onStatus(any(), any())).thenReturn(responseSpec1);
            lenient().when(responseSpec1.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(careContextResponse));

            // Second call chain (update visit records)
            lenient().when(requestBodyUriSpec2.uri(any(String.class))).thenReturn(requestBodySpec2);
            lenient().when(requestBodySpec2.bodyValue(any())).thenReturn(requestHeadersSpec2);
            lenient().when(requestHeadersSpec2.retrieve()).thenReturn(responseSpec2);
            lenient().when(responseSpec2.onStatus(any(), any())).thenReturn(responseSpec2);
            lenient().when(responseSpec2.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(true));

            // Third call chain (link care context)
            lenient().when(requestBodyUriSpec3.uri(any(String.class))).thenReturn(requestBodySpec3);
            lenient().when(requestBodySpec3.bodyValue(any())).thenReturn(requestHeadersSpec3);
            lenient().when(requestHeadersSpec3.retrieve()).thenReturn(responseSpec3);
            lenient().when(responseSpec3.onStatus(any(), any())).thenReturn(responseSpec3);
            lenient().when(responseSpec3.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(true));

            // When
            Mono<Boolean> result = documentLinking.linkHealthDocument(validHealthDocDto);

            // Then
            StepVerifier.create(result)
                    .expectNext(true)
                    .verifyComplete();

            verify(mapper).mapToCareContextDTO(eq(validHealthDocDto), any(), any(), any());
            verify(mapper).mapToConsultationDTO(eq(validHealthDocDto), any(), eq("cc-ref-123"), eq("req-123"));
            verify(mapper).mapToLinkCareContextDTO(eq(validHealthDocDto), eq("cc-ref-123"), any(), eq("req-123"));
            verify(webClient, times(3)).post();
        }

        @Test
        @DisplayName("should skip update and return false when no health records are provided")
        void linkHealthDocument_shouldSkipUpdate_whenNoHealthRecords() {
            // Given
            validHealthDocDto.setHealthRecords(Collections.emptyList());
            CreateCareContextResponse careContextResponse = new CreateCareContextResponse("cc-ref-123", "req-123", null);

            given(mapper.mapToCareContextDTO(any(), any(), any(), any())).willReturn(validCreateCareContextDto);

            mockWebClientPostChain();
            given(responseSpec.bodyToMono(eq(new ParameterizedTypeReference<CreateCareContextResponse>() {})))
                    .willReturn(Mono.just(careContextResponse));

            // When
            Mono<Boolean> result = documentLinking.linkHealthDocument(validHealthDocDto);

            // Then
            StepVerifier.create(result)
                    .expectNext(false)
                    .verifyComplete();

            // Verify only the care context creation happened
            verify(mapper).mapToCareContextDTO(eq(validHealthDocDto), any(), any(), any());
            verify(mapper, never()).mapToConsultationDTO(any(), any(), any(), any());
            verify(webClient, times(1)).post();
        }

        @Test
        @DisplayName("should fail when initial validation fails")
        void linkHealthDocument_shouldFail_whenInitialValidationFails() {
            // Given
            validHealthDocDto.setPatientReference(null);

            // When
            Mono<Boolean> result = documentLinking.linkHealthDocument(validHealthDocDto);

            // Then
            StepVerifier.create(result)
                    .expectError(ValidationError.class)
                    .verify();

            verifyNoInteractions(webClient);
        }

        @Test
        @DisplayName("should propagate EhrApiError when error status from API")
        void linkHealthDocument_shouldPropagateEhrApiError() {
            // Given
            lenient().when(mapper.mapToCareContextDTO(any(), any(), any(), any())).thenReturn(validCreateCareContextDto);

            // Mock WebClient chain
            lenient().when(webClient.post()).thenReturn(requestBodyUriSpec);
            lenient().when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodySpec);
            lenient().when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
            lenient().when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
            lenient().when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
            lenient().when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                    .thenReturn(Mono.error(new EhrApiError("API Error", ErrorType.INTERNAL_SERVER_ERROR)));

            // When
            Mono<Boolean> result = documentLinking.linkHealthDocument(validHealthDocDto);

            // Then
            StepVerifier.create(result)
                    .expectError(EhrApiError.class)
                    .verify();
        }
    }
}