package carestack.patient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import carestack.base.enums.Gender;
import carestack.base.enums.ResourceType;
import carestack.base.enums.StatesAndUnionTerritories;
import carestack.base.errors.ValidationError;
import carestack.patient.DTO.PatientDTO;
import carestack.patient.DTO.UpdatePatientDTO;
import carestack.patient.enums.PatientIdType;
import carestack.patient.enums.PatientType;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.*;

@MockitoSettings(strictness = Strictness.WARN)
 class PatientTest {

    @Mock private WebClient webClient;

    @Spy
    @InjectMocks
    private Patient patient;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAllPatients_Success() {
        List<Map<String, Object>> mockResponse = Collections.singletonList(new HashMap<>());

        given(patient.findAll(20, "1")).willReturn(Mono.just(mockResponse));

        StepVerifier.create(patient.findAll(20, "1"))
                .expectNext(mockResponse)
                .verifyComplete();
    }

    @Test
    public void testGetPatientById_Success() {
        String id = "12345";
        Map<String, Object> response = Map.of("id", id);
        doReturn(Mono.just(response)).when(patient).handleFindById(any(), eq(id));

        StepVerifier.create(patient.findById(id))
                .expectNext(response)
                .verifyComplete();
    }

    @Test
    public void testPatientExists_True() {
        doReturn(Mono.just(true)).when(patient).handleExists(any(), anyString());

        StepVerifier.create(patient.exists("123"))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    public void testPatientExists_Failure() {
        doReturn(Mono.error(new RuntimeException("failure"))).when(patient).handleExists(any(), anyString());

        StepVerifier.create(patient.exists("123"))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    public void testCreatePatient_Success() {
        PatientDTO dto = validPatientDTO();
        Map<String, Object> result = Map.of("status", "created");

        doReturn(Mono.just(result)).when(patient).handleCreate(any(), eq(dto));

        StepVerifier.create(patient.create(dto))
                .expectNext(result)
                .verifyComplete();
    }

    @Test
    public void testCreatePatient_ValidationError() {
        // given
        PatientDTO dto = new PatientDTO(); // invalid

        // when and then
        StepVerifier.create(patient.create(dto))
                .expectErrorMatches(t -> t instanceof ValidationError &&
                        t.getMessage().contains("ID Number is required"))
                .verify();
    }

    @Test
    public void testCreatePatient_NullInput() {
        // when and then
        StepVerifier.create(patient.create(null))
                .expectErrorMatches(t -> t instanceof ValidationError &&
                        t.getMessage().contains("Patient data cannot be null"))
                .verify();
    }

    @Test
    public void testUpdatePatient_Success() {
        UpdatePatientDTO dto = new UpdatePatientDTO();
        dto.setResourceId("res-123");
        Map<String, Object> response = Map.of("status", "updated");

        doReturn(Mono.just(response)).when(patient).handleUpdate(any(), eq(dto));

        StepVerifier.create(patient.update(dto))
                .expectNext(response)
                .verifyComplete();
    }


    @Test
    public void testUpdatePatient_NullInput() {
        StepVerifier.create(patient.update(null))
                .expectErrorMatches(t -> t instanceof ValidationError &&
                        t.getMessage().contains("Update patient data cannot be null"))
                .verify();
    }

    @Test
    public void testUpdatePatient_MissingResourceId() {
        UpdatePatientDTO dto = new UpdatePatientDTO(); // missing resourceId

        StepVerifier.create(patient.update(dto))
                .expectErrorMatches(t -> t instanceof ValidationError &&
                        t.getMessage().contains("Resource ID is required"))
                .verify();
    }

    @Test
    public void testDeletePatient_Success() {
        // given
        given(patient.handleDelete(anyString(), anyString())).willReturn(Mono.just("Deleted"));

        // when and then
        StepVerifier.create(patient.delete("123"))
                .expectNext("Deleted")
                .verifyComplete();
    }

    @Test
    public void testDeletePatient_InvalidId() {
        StepVerifier.create(patient.delete(""))
                .expectErrorMatches(t -> t instanceof ValidationError &&
                        t.getMessage().contains("ID cannot be null or empty for deletion."))
                .verify();
    }

    @Test
    public void testDeletePatient_NullId() {
        StepVerifier.create(patient.delete(null))
                .expectErrorMatches(t -> t instanceof ValidationError &&
                        t.getMessage().contains("ID cannot be null or empty for deletion."))
                .verify();
    }

    // -------- Helper --------

    private PatientDTO validPatientDTO() {
        PatientDTO dto = new PatientDTO();
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setMobileNumber("9876543210");
        dto.setIdType(PatientIdType.AADHAAR);
        dto.setIdNumber("123456789012");
        dto.setBirthDate("2000-01-01");
        dto.setGender(Gender.MALE);
        dto.setAddress("Test Address");
        dto.setPincode("110001");
        dto.setState(StatesAndUnionTerritories.DELHI);
        dto.setPatientType(PatientType.NEW);
        dto.setResourceType(ResourceType.Patient);
        return dto;
    }
}
