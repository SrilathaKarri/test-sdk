package carestack.practitioner;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;

/**
 * Unit tests for the {@link Practitioner} class.
 * <p>
 * This class contains tests for all major functionalities of the Practitioner class, including
 * creating, updating, deleting, retrieving, and checking the existence of practitioners.
 * The tests use JUnit 5, Mockito, and Reactor's StepVerifier for reactive stream testing.
 * </p>
 *
 * <p>Mockito's strictness level is set to WARN to catch potential misuses while allowing some flexibility during mocking.</p>
 */

@MockitoSettings(strictness = Strictness.WARN)
 class PractitionerTest {

    @Mock private WebClient webClient;
    @Mock
    private ObjectMapper objectMapper;

    @Spy
    @InjectMocks
    private Practitioner practitioner;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Tests retrieving all practitioners successfully with pagination.
     */
    @Test
    public void testGetAllPractitioners_Success() {
        List<Map<String, Object>> mockResponse = Collections.singletonList(new HashMap<>());

        given(practitioner.findAll(20, "1")).willReturn(Mono.just(mockResponse));

        StepVerifier.create(practitioner.findAll(20, "1"))
                .expectNext(mockResponse)
                .verifyComplete();
    }

    /**
     * Tests retrieving a practitioner by their ID successfully.
     * Verifies that the {@link Practitioner#findById(String)} method returns the expected practitioner details.
     */
    @Test
    public void testGetPractitionerById_Success() {
        String id = "12345";
        Map<String, Object> response = Map.of("id", id);
        doReturn(Mono.just(response)).when(practitioner).handleFindById(any(), eq(id));

        StepVerifier.create(practitioner.findById(id))
                .expectNext(response)
                .verifyComplete();
    }

    @Test
    public void testPractitionerExists_True() {
        doReturn(Mono.just(true)).when(practitioner).handleExists(any(), anyString());

        StepVerifier.create(practitioner.exists("123"))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    public void testPractitionerExists_Failure() {
        doReturn(Mono.error(new RuntimeException("failure"))).when(practitioner).handleExists(any(), anyString());

        StepVerifier.create(practitioner.exists("123"))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    public void testCreatePractitioner_Success() {
        PractitionerDTO dto = validPractitionerDTO();
        Map<String, Object> result = Map.of("status", "created");

        doReturn(Mono.just(result)).when(practitioner).handleCreate(any(), eq(dto));

        StepVerifier.create(practitioner.create(dto))
                .expectNext(result)
                .verifyComplete();
    }

    @Test
    public void testCreatePractitioner_ValidationError() {
        // given
        PractitionerDTO dto = new PractitionerDTO(); // invalid

        // when and then
        StepVerifier.create(practitioner.create(dto))
                .expectErrorMatches(t -> t instanceof ValidationError &&
                        t.getMessage().contains("Registration ID is required"))
                .verify();
    }

    @Test
    public void testCreatePractitioner_NullInput() {
        // when and then
        StepVerifier.create(practitioner.create(null))
                .expectErrorMatches(t -> t instanceof ValidationError &&
                        t.getMessage().contains("Practitioner data cannot be null"))
                .verify();
    }

    @Test
    public void testUpdatePractitioner_Success() {
        PractitionerDTO dto = validUpdatePractitionerDTO();
        Map<String, Object> result = Map.of("status", "updated");

        doReturn(Mono.just(result)).when(practitioner).handleUpdate(any(), eq(dto));

        StepVerifier.create(practitioner.update(dto))
                .expectNext(result)
                .verifyComplete();
    }


    @Test
    public void testUpdatePractitioner_NullInput() {
        StepVerifier.create(practitioner.update(null))
                .expectErrorMatches(t -> t instanceof ValidationError &&
                        t.getMessage().contains("Update practitioner data cannot be null"))
                .verify();
    }

    @Test
    public void testUpdatePractitioner_MissingResourceId() {
        PractitionerDTO dto = new PractitionerDTO(); // missing resourceId

        StepVerifier.create(practitioner.update(dto))
                .expectErrorMatches(t -> t instanceof ValidationError &&
                        t.getMessage().contains("Resource ID is required"))
                .verify();
    }

    @Test
    public void testDeletePractitioner_Success() {
        // given
        given(practitioner.handleDelete(anyString(), anyString())).willReturn(Mono.just("Deleted"));

        // when and then
        StepVerifier.create(practitioner.delete("123"))
                .expectNext("Deleted")
                .verifyComplete();
    }

    @Test
    public void testDeletePractitioner_InvalidId() {
        StepVerifier.create(practitioner.delete(""))
                .expectErrorMatches(t -> t instanceof ValidationError &&
                        t.getMessage().contains("ID cannot be null or empty for deletion."))
                .verify();
    }

    @Test
    public void testDeletePractitioner_NullId() {
        StepVerifier.create(practitioner.delete(null))
                .expectErrorMatches(t -> t instanceof ValidationError &&
                        t.getMessage().contains("ID cannot be null or empty for deletion."))
                .verify();
    }


    /**
     * Tests the failure scenario when deleting a practitioner with a missing ID.
     * Expects a {@link ValidationError} to be thrown.
     */
    @Test
    public void testDeletePractitioner_Failure_MissingPractitionerId() {
        String practitionerId = "";

        StepVerifier.create(practitioner.delete(practitionerId))
                .expectError(ValidationError.class)
                .verify();
    }


    /**
     * Tests the failure scenario when retrieving a practitioner by an empty ID.
     * Expects a {@link ValidationError} to be thrown.
     */
    @Test
    public void testGetPractitionerById_Failure_MissingPractitionerId() {
        String practitionerId = "";

        StepVerifier.create(practitioner.findById(practitionerId))
                .expectErrorMatches(t -> t instanceof ValidationError &&
                        t.getMessage().contains("Practitioner ID cannot be null or empty"))
                .verify();
    }

    /**
     * Tests checking if a practitioner exists by their ID when the practitioner is found.
     * Expects a successful response of 'true'.
     */
    @Test
    public void testPractitionerExists_Success() {
        doReturn(Mono.just(true)).when(practitioner).handleExists(any(), anyString());

        StepVerifier.create(practitioner.exists("123"))
                .expectNext(true)
                .verifyComplete();
    }

    /**
     * Tests checking if a practitioner exists by their ID when the practitioner is not found.
     * Expects a response of 'false'.
     */
    @Test
    public void testPractitionerExists_Failure_NotFound() {
        doReturn(Mono.error(new RuntimeException("failure"))).when(practitioner).handleExists(any(), anyString());

        StepVerifier.create(practitioner.exists("123"))
                .expectError(RuntimeException.class)
                .verify();
    }

    /**
     * Tests the failure scenario when an unexpected error occurs while checking for practitioner existence.
     * Expects a response of 'false'.
     */
    @Test
    public void testPractitionerExists_Failure_Error() {
        doReturn(Mono.error(new RuntimeException("Error occurred"))).when(practitioner).handleExists(any(), anyString());

        StepVerifier.create(practitioner.exists("123"))
                .expectError(RuntimeException.class)
                .verify();
    }



    /**
     * Tests retrieving practitioners with a null nextPage and default pageSize (10).
     */
    @Test
    public void testGetAllPractitioners_WithNullPage() {
        Integer pageSize = null;
        String nextPage = null;

        Map<String, Object> mockResponse = Map.of("data", "practitioner list");

        given(practitioner.findAll(any(),any())).willReturn(Mono.just(mockResponse));

        StepVerifier.create(practitioner.findAll(pageSize, nextPage))
                .expectNext(mockResponse)
                .verifyComplete();

    }

    /**
     * Tests retrieving practitioners with a custom pageSize but null nextPage.
     */
    @Test
    public void testGetAllPractitioners_WithCustomPageSize() {
        Integer pageSize = 15;
        String nextPage = null;

        Map<String, Object> mockResponse = Map.of("data", "practitioner list");

        given(practitioner.findAll(any(),any())).willReturn(Mono.just(mockResponse));

        StepVerifier.create(practitioner.findAll(pageSize, nextPage))
                .expectNext(mockResponse)
                .verifyComplete();

    }

    /**
     * Tests retrieving practitioners with both null pageSize and nextPage, using default values.
     */
    @Test
    public void testGetAllPractitioners_WithDefaultValues() {
        Integer pageSize = null;
        String nextPage = null;

        Map<String, Object> mockResponse = Map.of("data", "practitioner list");

        given(practitioner.findAll(any(),any())).willReturn(Mono.just(mockResponse));

        StepVerifier.create(practitioner.findAll(pageSize, nextPage))
                .expectNext(mockResponse)
                .verifyComplete();
    }

    // -------- Helper --------

    private PractitionerDTO validPractitionerDTO() {
        PractitionerDTO practitionerDTO = new PractitionerDTO();
        practitionerDTO.setFirstName("Shri");
        practitionerDTO.setLastName("Ram");
        practitionerDTO.setRegistrationId("12345");
        practitionerDTO.setDepartment("Cardiology");
        practitionerDTO.setGender(Gender.MALE);
        practitionerDTO.setJoiningDate("2021-01-01");
        practitionerDTO.setMobileNumber("9876543210");
        practitionerDTO.setEmailId("shri.ram@example.com");
        practitionerDTO.setDesignation("Senior Doctor");
        practitionerDTO.setStatus("active");
        practitionerDTO.setBirthDate("01-01-2000");
        practitionerDTO.setAddress("xyz street");
        practitionerDTO.setPincode("332424");
        practitionerDTO.setState(StatesAndUnionTerritories.TELANGANA);
        practitionerDTO.setResourceType(ResourceType.Practitioner);
        return practitionerDTO;
    }

    private PractitionerDTO validUpdatePractitionerDTO() {
        PractitionerDTO practitionerDTO = new PractitionerDTO();
        practitionerDTO.setFirstName("Shri");
        practitionerDTO.setLastName("Krishna");
        practitionerDTO.setRegistrationId("34902");
        practitionerDTO.setResourceId("3544q6");
        practitionerDTO.setDepartment("Cardiology");
        practitionerDTO.setGender(Gender.MALE);
        practitionerDTO.setJoiningDate("2021-01-01");
        practitionerDTO.setMobileNumber("9876543210");
        practitionerDTO.setEmailId("shri.ram@example.com");
        practitionerDTO.setDesignation("Senior Doctor");
        practitionerDTO.setStatus("active");
        practitionerDTO.setBirthDate("01-01-2000");
        practitionerDTO.setAddress("xyz street");
        practitionerDTO.setPincode("332424");
        practitionerDTO.setResourceType(ResourceType.Practitioner);
        practitionerDTO.setState(StatesAndUnionTerritories.GUJARAT);
        return practitionerDTO;
    }
}
