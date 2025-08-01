package carestack.encounter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import carestack.ai.EncryptionUtilities;
import carestack.base.enums.CaseType;
import carestack.encounter.dtos.EncounterRequestDTO;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.*;

 class EncounterTest {

    @Mock
    private Validator validator;

    @Mock
    private EncryptionUtilities encryptionUtilities;

    @InjectMocks
    private Encounter encounter;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        encounter = new Encounter(objectMapper, null, validator);
        encounter.encryptionUtilities = encryptionUtilities;
    }

    @Test
    void createEncounter_withPayloadMap_shouldCallGenerateFhirFromPayload() {
        // Given
        Map<String, Object> dtoMap = new HashMap<>();
        dtoMap.put("payload", Map.of("someField", "someValue"));

        EncounterRequestDTO request = new EncounterRequestDTO();
        request.setCaseType(CaseType.OP_CONSULTATION);
        request.setDto(dtoMap);

        given(validator.validate(any())).willReturn(Collections.emptySet());
        given(encryptionUtilities.encryption(any())).willReturn("encrypted-data");

        Encounter spyEncounter = spy(encounter);

        given(spyEncounter.generateFhirFromPayload(any(), any(), any())).willReturn(Mono.just(Map.of("fhir", "bundle")));

        // When
        Mono<?> resultMono = spyEncounter.createEncounter(request);

        // Then
        then(spyEncounter).should().generateFhirFromPayload(eq(CaseType.OP_CONSULTATION), eq(dtoMap), eq(Optional.empty()));
        assertNotNull(resultMono);
    }

    @Test
    void createEncounter_withCaseSheetsList_shouldCallGenerateFhirFromFiles() {
        // Given
        Map<String, Object> dtoMap = new HashMap<>();
        dtoMap.put("caseSheets", List.of("file1", "file2"));

        EncounterRequestDTO request = new EncounterRequestDTO();
        request.setCaseType(CaseType.DISCHARGE_SUMMARY);
        request.setDto(dtoMap);

        given(validator.validate(any())).willReturn(Collections.emptySet());
        given(encryptionUtilities.encryption(any())).willReturn("encrypted-data");

        Encounter spyEncounter = spy(encounter);

        List<String> caseSheets = List.of("file1", "file2");

        given(spyEncounter.generateFhirFromFiles(any(), any(), any())).willReturn(Mono.just(Map.of("fhir", "bundle")));

        // When
        Mono<?> resultMono = spyEncounter.createEncounter(request);

        // Then
        then(spyEncounter).should().generateFhirFromFiles(eq(CaseType.DISCHARGE_SUMMARY), eq(caseSheets), eq(Optional.empty()));
        assertNotNull(resultMono);
    }

    @Test
    void createEncounter_withNoPayloadAndNoFiles_shouldReturnErrorMono() {
        // Given
        EncounterRequestDTO request = new EncounterRequestDTO();
        request.setCaseType(CaseType.OP_CONSULTATION);
        request.setDto(Map.of());  // empty map, no payload or caseSheets

        given(validator.validate(any())).willReturn(Collections.emptySet());

        // When
        Mono<?> resultMono = encounter.createEncounter(request);

        // Then
        assertThatThrownBy(resultMono::block)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Provide either 'payload' or 'files'");
    }

    @Test
    void createEncounter_withInvalidPayloadType_shouldReturnErrorMono() {
        // Given
        EncounterRequestDTO request = new EncounterRequestDTO();
        request.setCaseType(CaseType.OP_CONSULTATION);
        request.setDto("invalidPayloadType");

        given(validator.validate(any())).willReturn(Collections.emptySet());

        // When
        Mono<?> resultMono = encounter.createEncounter(request);

        // Then
        assertThatThrownBy(resultMono::block)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid request");
    }


    @Test
    void validateInput_shouldThrowExceptionOnViolation() {
        // Given
        EncounterRequestDTO request = new EncounterRequestDTO();
        Set<ConstraintViolation<EncounterRequestDTO>> violations = Set.of(mock(ConstraintViolation.class));
        given(validator.validate(request)).willReturn(violations);

        // When & Then
        assertThatThrownBy(() -> encounter.createEncounter(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Validation failed");
    }
}
