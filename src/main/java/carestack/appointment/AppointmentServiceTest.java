package carestack.appointment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import carestack.appointment.dto.AppointmentDTO;
import carestack.appointment.dto.AppointmentResponse;
import carestack.base.errors.ValidationError;

import java.util.HashMap;
import java.util.Map;

class AppointmentServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private Appointment appointmentService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        appointmentService = new Appointment(new ObjectMapper(), webClient) {
            public ObjectMapper getObjectMapper() {
                return new ObjectMapper();
            }
        };
    }

    @Nested
    @DisplayName("createFromInput() tests")
    class CreateFromInputTests {

        @Test
        @DisplayName("Should create appointment from DTO successfully")
        void createFromDto_Positive() {
            AppointmentDTO dto = new AppointmentDTO();
            AppointmentResponse mockResponse = new AppointmentResponse();

            Appointment serviceSpy = new Appointment(new ObjectMapper(), webClient) {
                @Override
                public Mono<Object> create(AppointmentDTO appointmentDTO) {
                    return Mono.just(mockResponse);
                }

                public ObjectMapper getObjectMapper() {
                    return new ObjectMapper();
                }
            };

            Mono<AppointmentResponse> result = serviceSpy.createFromInput(dto);

            StepVerifier.create(result)
                    .expectNextMatches(response -> response instanceof AppointmentResponse)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should create appointment from JSON string")
        void createFromJson_Positive() throws Exception {
            String json = "{\"patientReference\":\"123\"}";

            Appointment serviceSpy = new Appointment(new ObjectMapper(), webClient) {
                @Override
                public Mono<Object> create(AppointmentDTO appointmentDTO) {
                    return Mono.just(new AppointmentResponse());
                }

                public ObjectMapper getObjectMapper() {
                    return new ObjectMapper();
                }
            };

            StepVerifier.create(serviceSpy.createFromInput(json))
                    .expectNextMatches(response -> response instanceof AppointmentResponse)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should fail when input is unsupported type")
        void createFromUnsupportedType_Negative() {
            StepVerifier.create(appointmentService.createFromInput(123))
                    .expectError(IllegalArgumentException.class)
                    .verify();
        }

        @Test
        @DisplayName("Should handle unsupported input type")
        void createFromUnsupportedInput_Negative() {
            Object unsupported = 12345;

            StepVerifier.create(appointmentService.createFromInput(unsupported))
                    .expectError(IllegalArgumentException.class)
                    .verify();
        }

        @Test
        @DisplayName("Should create appointment from Map input")
        void createFromMap_Positive() {
            Map<String, Object> inputMap = new HashMap<>();
            inputMap.put("patientReference", "patient123");
            inputMap.put("practitionerReference", "pract123");

            Appointment serviceSpy = new Appointment(new ObjectMapper(), webClient) {
                @Override
                public Mono<Object> create(AppointmentDTO appointmentDTO) {
                    return Mono.just(new AppointmentResponse());
                }

                public ObjectMapper getObjectMapper() {
                    return new ObjectMapper();
                }
            };

            StepVerifier.create(serviceSpy.createFromInput(inputMap))
                    .expectNextMatches(response -> response instanceof AppointmentResponse)
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("create() method tests")
    class CreateTests {

        @Test
        @DisplayName("Should return error if AppointmentDTO is null")
        void create_NullDTO_Error() {
            StepVerifier.create(appointmentService.create(null))
                    .expectError(ValidationError.class)
                    .verify();
        }
    }

    @Nested
    @DisplayName("update() method tests")
    class UpdateTests {

        @Test
        @DisplayName("Should return error if UpdateAppointmentDTO is null")
        void update_NullDTO_Error() {
            StepVerifier.create(appointmentService.update(null))
                    .expectError(ValidationError.class)
                    .verify();
        }
    }

    @Nested
    @DisplayName("exists() method tests")
    class ExistsTests {

        @Test
        @DisplayName("Should return error if ID is null or empty")
        void exists_NullOrEmptyId_Error() {
            StepVerifier.create(appointmentService.exists(null))
                    .expectError(ValidationError.class)
                    .verify();

            StepVerifier.create(appointmentService.exists(""))
                    .expectError(ValidationError.class)
                    .verify();
        }

        @Test
        @DisplayName("Should pass for valid ID")
        void exists_ValidId_Positive() {
            Appointment serviceSpy = new Appointment(new ObjectMapper(), webClient) {
                @Override
                public Mono<Boolean> exists(String id) {
                    return Mono.just(true);
                }
            };

            StepVerifier.create(serviceSpy.exists("valid-id"))
                    .expectNext(true)
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("delete() method tests")
    class DeleteTests {

        @Test
        @DisplayName("Should delete appointment successfully")
        void delete_ValidId_Success() {
            Appointment serviceSpy = new Appointment(new ObjectMapper(), webClient) {
                @Override
                public Mono<Object> delete(String id) {
                    return Mono.just("deleted");
                }
            };

            StepVerifier.create(serviceSpy.delete("valid-id"))
                    .expectNext("deleted")
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("findAll() method tests")
    class FindAllTests {

        @Test
        @DisplayName("Should find all appointments")
        void findAll_Positive() {
            Appointment serviceSpy = new Appointment(new ObjectMapper(), webClient) {
                @Override
                public Mono<Object> findAll(Object... params) {
                    return Mono.just("all appointments");
                }
            };

            StepVerifier.create(serviceSpy.findAll())
                    .expectNext("all appointments")
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("findById() method tests")
    class FindByIdTests {

        @Test
        @DisplayName("Should find appointment by ID")
        void findById_Positive() {
            Appointment serviceSpy = new Appointment(new ObjectMapper(), webClient) {
                @Override
                public Mono<Object> findById(String id) {
                    return Mono.just("appointment details");
                }
            };

            StepVerifier.create(serviceSpy.findById("appointment-id"))
                    .expectNext("appointment details")
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("findByFilters() method tests")
    class FindByFiltersTests {

        @Test
        @DisplayName("Should return filtered appointments")
        void findByFilters_Positive() {
            Appointment serviceSpy = new Appointment(new ObjectMapper(), webClient) {
                @Override
                public Mono<Object> findByFilters(Object... params) {
                    return Mono.just("filtered appointments");
                }
            };

            StepVerifier.create(serviceSpy.findByFilters("practitioner", "123"))
                    .expectNext("filtered appointments")
                    .verifyComplete();
        }
    }
}