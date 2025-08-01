package carestack.organization;

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
import carestack.base.errors.ValidationError;
import carestack.organization.dto.*;
import carestack.organization.enums.Region;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;

@MockitoSettings(strictness = Strictness.LENIENT)
 class OrganizationTest {

    @Mock private WebClient webClient;
    @Mock private ObjectMapper objectMapper;
    @Mock private Demographic demographic;
    @Spy @InjectMocks private Organization organization;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testFindAll_Success() {
        Map<String, Object> mockResponse = Map.of("data", "organization list");
        doReturn(Mono.just(mockResponse)).when(organization).get(anyString(), anyMap(), any());

        StepVerifier.create(organization.findAll(10, "1"))
                .expectNext(mockResponse)
                .verifyComplete();
    }

    @Test
    public void testFindAll_InvalidPageSize() {
        StepVerifier.create(organization.findAll(101))
                .expectErrorMatches(t -> t instanceof ValidationError &&
                        t.getMessage().contains("pageSize must be between 1 and 100"))
                .verify();
    }

    @Test
    public void testFindById_Success() {
        Map<String, Object> mockResponse = Map.of("id", "ORG001");
        doReturn(Mono.just(mockResponse)).when(organization).get(anyString(), any());

        StepVerifier.create(organization.findById("accountid", "ORG001"))
                .expectNext(mockResponse)
                .verifyComplete();
    }

    @Test
    public void testFindById_InvalidInput() {
        StepVerifier.create(organization.findById("", ""))
                .expectErrorMatches(t -> t instanceof ValidationError &&
                        t.getMessage().contains("Organization ID and ID Type cannot be null or empty."))
                .verify();
    }

    @Test
    public void testFindById_InvalidIdType() {
        StepVerifier.create(organization.findById("invalid_type", "ORG001"))
                .expectErrorMatches(t -> t instanceof ValidationError &&
                        t.getMessage().contains("Invalid Organization ID Type provided."))
                .verify();
    }

    @Test
    public void testExists_Success() {
        doReturn(Mono.just(Map.of("message", "Facility Found !!!"))).when(organization).findById(anyString(), anyString());

        StepVerifier.create(organization.exists("abdm", "ORG001"))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    public void testExists_Failure() {
        doReturn(Mono.error(new RuntimeException("error"))).when(organization).findById(anyString(), anyString());

        StepVerifier.create(organization.exists("abdm", "ORG001"))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    public void testRegister_Success() {
        OrganizationDTO validOrg = getValidOrganizationDTO();
        doReturn(Mono.just(validOrg)).when(organization).validateOrganization(validOrg);
        doReturn(Mono.just(Map.of("status", "registered"))).when(organization).post(anyString(), eq(validOrg), any());

        StepVerifier.create(organization.register(validOrg))
                .expectNextMatches(response -> ((Map<?, ?>) response).get("status").equals("registered"))
                .verifyComplete();
    }

    @Test
    public void testRegister_ValidationError() {
        StepVerifier.create(organization.register(null))
                .expectErrorMatches(t -> t instanceof ValidationError &&
                        t.getMessage().contains("Organization data cannot be null."))
                .verify();
    }

    @Test
    public void testUpdateSpoc_Success() {
        UpdateSpocForOrganization updateData = new UpdateSpocForOrganization();
        updateData.setId("ORG001");
        doReturn(Mono.just(Map.of("status", "updated"))).when(organization).put(anyString(), eq(updateData), any());

        StepVerifier.create(organization.updateSpoc(updateData))
                .expectNextMatches(response -> ((Map<?, ?>) response).get("status").equals("updated"))
                .verifyComplete();
    }

    @Test
    public void testUpdateSpoc_NullInput() {
        StepVerifier.create(organization.updateSpoc(null))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    public void testUpdateSpoc_ValidationError() {
        UpdateSpocForOrganization updateData = new UpdateSpocForOrganization();

        StepVerifier.create(organization.updateSpoc(updateData))
                .expectErrorMatches(t -> t instanceof ValidationError &&
                        t.getMessage().contains("Organization ID is required for updating SPOC."))
                .verify();
    }

    @Test
    public void testDelete_Success() {
        doReturn(Mono.empty()).when(organization).delete(anyString(), any());

        StepVerifier.create(organization.delete("ORG001"))
                .verifyComplete();
    }

    @Test
    public void testDelete_NullId() {
        StepVerifier.create(organization.delete(null))
                .expectErrorMatches(t -> t instanceof ValidationError &&
                        t.getMessage().contains("Organization ID cannot be null or empty."))
                .verify();
    }

    @Test
    public void testGetLgdStates_Success() {
        List<ResponseDTOs.StateDTO> states = Collections.singletonList(new ResponseDTOs.StateDTO());
        doReturn(Mono.just(states)).when(organization).get(anyString(), any());

        StepVerifier.create(organization.getLgdStates())
                .expectNext(states)
                .verifyComplete();
    }

    private OrganizationDTO getValidOrganizationDTO() {
        OrganizationDTO dto = new OrganizationDTO();
        BasicInformation basic = new BasicInformation();
        basic.setRegion(Region.URBAN.getCode());
        dto.setBasicInformation(basic);
        dto.setContactInformation(new ContactInformation());
        dto.setOrganizationDetails(new OrganizationDetails());
        dto.setAccountId("acc123");
        return dto;
    }
}
