package carestack.appointment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import carestack.appointment.dto.AppointmentDTO;
import carestack.appointment.dto.AppointmentResponse;
import carestack.appointment.dto.UpdateAppointmentDTO;
import carestack.base.Base;
import carestack.base.ResourceService;
import carestack.base.enums.ResourceType;
import carestack.base.errors.EhrApiError;
import carestack.base.errors.ValidationError;
import carestack.base.utils.LogUtil;
import carestack.base.utils.StringUtils;

import java.util.Map;


/**
 * Service for handling appointment-related operations.
 * <p>
 * This class provides a comprehensive set of methods for managing appointments within the EHR system.
 * It implements the {@link ResourceService} interface to offer standardized CRUD (Create, Read, Update, Delete)
 * and search functionalities. All operations are reactive, returning {@link Mono} objects to ensure
 * non-blocking execution.
 * </p>
 */
@Service
public class Appointment extends Base implements ResourceService<AppointmentDTO, UpdateAppointmentDTO> {

    private static final ResourceType RESOURCE_TYPE = ResourceType.Appointment;

    public Appointment(ObjectMapper objectMapper, WebClient webClient) {
        super(objectMapper, webClient);
    }

    /**
     * <h3>Retrieve a Paginated List of Appointments</h3>
     * Fetches a list of appointment records from the EHR system, with optional pagination.
     *
     * <h3>Input and Parameters:</h3>
     * This method supports flexible, optional parameters for pagination:
     * <ul>
     *     <li><b>pageSize (Integer):</b> The number of records per page. Defaults to the API's setting if not provided.</li>
     *     <li><b>nextPage (String):</b> A token from a previous response to fetch the next page.</li>
     * </ul>
     *
     * <h3>Output:</h3>
     * @return A {@link Mono<Object>} that emits the API response, typically a JSON object containing a list of
     *         appointment records and a {@code nextPage} token for pagination.
     *
     * <h3>Error Handling:</h3>
     * Returns a {@link Mono#error(Throwable)} with a custom {@link EhrApiError} if the API request fails.
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * // Fetch the first page with 15 appointments per page
     * appointmentService.findAll(15)
     *     .subscribe(
     *         response -> System.out.println("Appointments list: " + response),
     *         error -> System.err.println("Error fetching appointments: " + error.getMessage())
     *     );
     *
     * // Expected Output (example):
     * // Appointments list: { "data": [ { "appointmentReference": "appt_1", ... }, { "appointmentReference": "appt_2", ... } ], "nextPage": "tokenForPage2" }
     * }</pre>
     *
     * @param params Optional: an {@link Integer} for pageSize and/or a {@link String} for nextPage.
     */
    @Override
    public Mono<Object> findAll(Object... params) {
        return handleFindAll(RESOURCE_TYPE, params);
    }

    /**
     * <h3>Retrieve an Appointment by ID</h3>
     * Fetches the complete details of a single appointment using its unique identifier.
     *
     * <h3>Input and Validation:</h3>
     * Requires a non-null, non-empty appointment ID string.
     *
     * <h3>Output:</h3>
     * @return A {@link Mono<Object>} that emits the appointment's full details as a structured object (e.g., a Map or DTO).
     *
     * <h3>Error Handling:</h3>
     * <ul>
     *     <li>Returns a {@link Mono#error(Throwable)} with a {@link ValidationError} if the ID is null or empty.</li>
     *     <li>Propagates an {@link EhrApiError} if the API call fails (e.g., 404 Not Found).</li>
     * </ul>
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * String appointmentId = "apt_12345";
     * appointmentService.findById(appointmentId)
     *     .subscribe(
     *         appointment -> System.out.println("Appointment Details: " + appointment),
     *         error -> System.err.println("Error finding appointment " + appointmentId + ": " + error.getMessage())
     *     );
     *
     * // Expected Output (example):
     * // Appointment Details: { "appointmentReference": "apt_12345", "patientReference": "pat_abc", "status": "booked", ... }
     * }</pre>
     *
     * @param id The unique ID of the appointment to retrieve.
     */
    @Override
    public Mono<Object> findById(String id) {
        return handleFindById(RESOURCE_TYPE, id);
    }

    /**
     * <h3>Check if an Appointment Exists</h3>
     * Determines if an appointment with the specified ID exists in the EHR system.
     *
     * <h3>Input and Validation:</h3>
     * Requires a non-null, non-empty appointment ID string.
     *
     * <h3>Output:</h3>
     * @return A {@code Mono<Boolean>} that emits {@code true} if the appointment exists, and {@code false} otherwise.
     *
     * <h3>Error Handling:</h3>
     * <ul>
     *     <li>Returns a {@link Mono#error(Throwable)} with a {@link ValidationError} if the ID is null or empty.</li>
     *     <li>Propagates API or network errors, allowing the caller to distinguish "not found" from "request failed".</li>
     * </ul>
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * String appointmentId = "appt_12345";
     * appointmentService.exists(appointmentId)
     *     .subscribe(
     *         exists -> System.out.println("Appointment " + appointmentId + " exists: " + exists),
     *         error -> System.err.println("Error checking for existence: " + error.getMessage())
     *     );
     *
     * // Possible Outputs:
     * // Appointment appt_12345 exists: true
     * // or
     * // Appointment appt_12345 exists: false
     * }</pre>
     *
     * @param id The unique ID of the appointment to check.
     */
    @Override
    public Mono<Boolean> exists(String id) {
        if (StringUtils.isNullOrEmpty(id)) {
            return Mono.error(new ValidationError("Appointment ID cannot be null or empty."));
        }
        return handleExists(RESOURCE_TYPE, id);
    }


    /**
     *<h3>Create an Appointment from Various Inputs</h3>
     * A flexible method that accepts appointment data as a {@link Map}, JSON {@link String}, or {@link AppointmentDTO},
     * converts it to a standard DTO, and initiates the creation process.
     *
     * <h3>Input and Validation:</h3>
     * The method validates that the input is one of the supported types. The data within the input must conform
     * to the structure of {@link AppointmentDTO}.
     *
     * <h3>Output:</h3>
     * @return A {@link Mono} emitting a strongly-typed {@link AppointmentResponse} for easier consumption.
     *         This response object standardizes the result from the API.
     *
     * <h3>Error Handling:</h3>
     * <ul>
     *     <li>Returns {@link Mono#error(Throwable)} with an {@link IllegalArgumentException} if the input type is unsupported or if a JSON string is malformed.</li>
     *     <li>Propagates errors from the underlying {@code create} method.</li>
     * </ul>
     *
     * <h3>Example Usage (with a Map):</h3>
     * <pre>{@code
     * Map<String, Object> appointmentData = new HashMap<>();
     * appointmentData.put("patientReference", "pat_john_doe");
     * appointmentData.put("practitionerReference", "prac_dr_smith");
     * appointmentData.put("appointmentStarTime", "2025-02-28T09:00:00Z");
     * appointmentData.put("appointmentEndTime", "2025-02-28T09:30:00Z");
     *
     * appointmentService.createFromInput(appointmentData)
     *     .subscribe(
     *         response -> System.out.println("Creation Response: " + response.getMessage()),
     *         error -> System.err.println("Error creating from map: " + error.getMessage())
     *     );
     *
     * // Expected Output (example):
     * // Creation Response: Appointment created successfully.
     * }</pre>
     *
     * @param input The appointment data, which can be a {@link Map}, a JSON {@link String}, or an {@link AppointmentDTO}.
     */
    public Mono<AppointmentResponse> createFromInput(Object input) {
        AppointmentDTO appointmentDTO = convertToAppointmentDTO(input);
        if (appointmentDTO == null) {
            return Mono.error(new IllegalArgumentException("Invalid input type or data is missing. Supported types: Map, JSON String, AppointmentDTO."));
        }
        return create(appointmentDTO).map(response -> objectMapper.convertValue(response, AppointmentResponse.class));
    }

    /**
     * <h3>Create a New Appointment (Core Method)</h3>
     * Sends a request to the EHR system to create a new appointment record from a validated DTO.
     *
     * <h3>Input and Validation:</h3>
     * The method requires a valid {@link AppointmentDTO} object.
     * <ul>
     *     <li>The DTO itself cannot be null.</li>
     *     <li>The {@code @Valid} annotation triggers bean validation on its fields (e.g., {@code patientReference}, {@code practitionerReference}, {@code appointmentStarTime}, {@code appointmentEndTime} must not be empty/null).</li>
     * </ul>
     *
     * <h3>Output:</h3>
     * @return A {@link Mono<Object>} that emits the raw response from the EHR system, typically confirming creation and including the new appointment's ID.
     *
     * <h3>Error Handling:</h3>
     * <ul>
     *     <li>Returns a {@link Mono#error(Throwable)} with a {@link ValidationError} if the DTO is null.</li>
     *     <li>Fails with a framework-level validation exception if DTO constraints are violated.</li>
     *     <li>Propagates an {@link EhrApiError} on API failure.</li>
     * </ul>
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * AppointmentDTO newAppointment = new AppointmentDTO(
     *     "prac_dr_smith",
     *     "pat_john_doe",
     *     ZonedDateTime.parse("2025-02-28T09:00:00Z"),
     *     ZonedDateTime.parse("2025-02-28T09:30:00Z"),
     *     DocLinkingEnums.AppointmentPriority.ROUTINE,
     *     "org_main_clinic",
     *     "slot_morning_1",
     *     null // appointmentReference is not needed for creation
     * );
     *
     * appointmentService.create(newAppointment)
     *       .subscribe(
     *          response -> System.out.println("API Response: " + response),
     *          error -> System.err.println("Failed to create appointment: " + error.getMessage())
     *       );
     *
     * // Expected Output (example):
     * // API Response: { "type": "SUCCESS", "message": "Appointment created successfully.", "requestResource": { "appointmentReference": "appt_new_67890", ... } }
     * }</pre>
     *
     * @param appointmentDTO The data transfer object containing the new appointment's information.
     */
    @Override
    public Mono<Object> create(@Valid AppointmentDTO appointmentDTO) {
        if (appointmentDTO == null) {
            return Mono.error(new ValidationError("Appointment data cannot be null."));
        }
        return handleCreate(RESOURCE_TYPE, appointmentDTO);
    }

    /**
     * <h3>Update an Existing Appointment</h3>
     * Modifies an existing appointment's record in the EHR system.
     *
     * <h3>Input and Validation:</h3>
     * Requires a valid {@link UpdateAppointmentDTO} object.
     * <ul>
     *     <li>The DTO itself cannot be null.</li>
     *     <li>The {@code @Valid} annotation triggers validation on its fields. Crucially, {@code appointmentReference} must be provided to identify which appointment to update.</li>
     * </ul>
     *
     * <h3>Output:</h3>
     * @return A {@link Mono<Object>} that emits the raw API response confirming the update.
     *
     * <h3>Error Handling:</h3>
     * <ul>
     *     <li>Returns a {@link Mono#error(Throwable)} with a {@link ValidationError} if the DTO is null.</li>
     *     <li>Fails with a framework-level validation exception if DTO constraints are violated.</li>
     *     <li>Propagates an {@link EhrApiError} on API failure.</li>
     * </ul>
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * UpdateAppointmentDTO updateData = new UpdateAppointmentDTO();
     * updateData.setAppointmentReference("apt_12345"); // <-- Important: ID of appointment to update
     * updateData.setAppointmentStartTime(ZonedDateTime.parse("2025-03-01T14:00:00Z"));
     * updateData.setAppointmentEndTime(ZonedDateTime.parse("2025-03-01T14:30:00Z"));
     * updateData.setPriority(DocLinkingEnums.AppointmentPriority.URGENT);
     *
     * appointmentService.update(updateData)
     *      .subscribe(
     *           response -> System.out.println("Update successful: " + response),
     *           error -> System.err.println("Failed to update appointment: " + error.getMessage())
     *       );
     *
     * // Expected Output (example):
     * // Update successful: { "status": "success", "id": "apt_12345", "message": "Appointment updated." }
     * }</pre>
     *
     * @param updateAppointmentData The DTO containing the fields to update.
     */
    @Override
    public Mono<Object> update(@Valid UpdateAppointmentDTO updateAppointmentData) {
        if (updateAppointmentData == null) {
            return Mono.error(new ValidationError("Update appointment data cannot be null."));
        }
        return handleUpdate(RESOURCE_TYPE, updateAppointmentData);
    }

    /**
     * <h3>Search for Appointments with Filters</h3>
     * Fetches a list of appointments based on a flexible set of search criteria.
     *
     * <h3>Input and Parameters:</h3>
     * Accepts a variable number of key-value pairs for filtering, plus optional pagination parameters.
     * <ul>
     *     <li><b>Filter Key (String):</b> The name of the field to filter by (e.g., "patientReference", "status").</li>
     *     <li><b>Filter Value (Object):</b> The value to match for the given key.</li>
     *     <li><b>Pagination:</b> Can also include an {@link Integer} for pageSize and a {@link String} for nextPage.</li>
     * </ul>
     *
     * <h3>Output:</h3>
     * @return A {@link Mono<Object>} that emits the API response containing a list of matching appointments.
     *
     * <h3>Error Handling:</h3>
     * Propagates an {@link EhrApiError} if the API call fails.
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * // Search for all "booked" appointments for a specific patient
     * String patientId = "pat_john_doe";
     * appointmentService.findByFilters("patientReference", patientId, "status", "booked")
     *     .subscribe(
     *         response -> System.out.println("Search results: " + response),
     *         error -> System.err.println("Error during search: " + error.getMessage())
     *     );
     *
     * // Expected Output (example):
     * // Search results: { "data": [ { "appointmentReference": "apt_xyz", "patientReference": "pat_john_doe", "status": "booked", ... } ], "nextPage": null }
     * }</pre>
     *
     * @param params A sequence of key-value filter pairs and optional pagination parameters.
     */
    @Override
    public Mono<Object> findByFilters(Object... params) {
        return handleFind(RESOURCE_TYPE, params);
    }

    /**
     * <h3>Delete an Appointment by ID</h3>
     * Permanently removes an appointment record from the EHR system using its unique identifier.
     *
     * <h3>Input and Validation:</h3>
     * The method requires a non-null, non-empty appointment ID string. This is validated by the base handler.
     *
     * <h3>API Request:</h3>
     * This method delegates to a base handler to make a <b>DELETE</b> request to the appointment deletion endpoint
     * (e.g., {@code /appointments/{id}}).
     *
     * <h3>Output:</h3>
     * @return A {@link Mono<Object>} that, upon success, emits the response from the EHR system, typically confirming the deletion.
     *
     * <h3>Error Handling:</h3>
     * <ul>
     *     <li>Returns a {@link Mono#error(Throwable)} with a {@link ValidationError} if the provided {@code id} is null or empty.</li>
     *     <li>Propagates a {@link Mono#error(Throwable)} if the API call fails (e.g., 404 Not Found if the appointment doesn't exist), wrapped in a custom {@link EhrApiError}.</li>
     * </ul>
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * String appointmentIdToDelete = "apt_12345";
     *
     * appointmentService.delete(appointmentIdToDelete)
     *     .subscribe(
     *         response -> System.out.println("Deletion successful: " + response),
     *         error -> System.err.println("Failed to delete appointment " + appointmentIdToDelete + ": " + error.getMessage())
     *     );
     *
     * // Expected Output (example):
     * // Deletion successful: { "status": "success", "message": "Appointment with ID apt_12345 deleted." }
     * }</pre>
     *
     * @param id The unique identifier of the appointment to be deleted. Must not be null or empty.
     */
    @Override
    public Mono<Object> delete(String id) {
        return handleDelete("appointments", id);
    }


    /**
     * <h3>Convert Generic Input to AppointmentDTO</h3>
     * A private helper method to standardize various input types into a single, consistent {@link AppointmentDTO} object.
     * This enhances the flexibility of the public-facing {@code createFromInput} method by allowing it to accept
     * data as a {@link Map}, a JSON {@link String}, or a pre-existing {@link AppointmentDTO} instance.
     *
     * <h3>Logic Flow:</h3>
     * <ol>
     *     <li>If the input is already an {@code AppointmentDTO}, it is returned directly.</li>
     *     <li>If the input is a {@code Map}, it is converted to an {@code AppointmentDTO} using the configured {@code ObjectMapper}.</li>
     *     <li>If the input is a {@code String}, it is parsed as JSON into an {@code AppointmentDTO}.</li>
     *     <li>If the input is of any other type, an error is logged and {@code null} is returned.</li>
     * </ol>
     *
     * <h3>Error Handling:</h3>
     * <ul>
     *     <li>Throws an {@link IllegalArgumentException} if a provided JSON string is malformed and cannot be deserialized.</li>
     *     <li>Returns {@code null} for unsupported input types, which is then handled by the calling method to return a user-facing error.</li>
     * </ul>
     *
     * @param input The generic object to be converted. Supported types are {@link AppointmentDTO}, {@link Map}, and {@link String}.
     * @return The converted {@link AppointmentDTO}, or {@code null} if the input type is unsupported.
     * @throws IllegalArgumentException if a JSON string fails to deserialize.
     */
    private AppointmentDTO convertToAppointmentDTO(Object input) {
        if (input instanceof AppointmentDTO dto) {
            return dto;
        } else if (input instanceof Map) {
            return objectMapper.convertValue(input, AppointmentDTO.class);
        } else if (input instanceof String jsonString) {
            try {
                return objectMapper.readValue(jsonString, AppointmentDTO.class);
            } catch (JsonProcessingException e) {
                LogUtil.logger.error("Failed to deserialize JSON string into AppointmentDTO: {}", e.getMessage());
                throw new IllegalArgumentException("Failed to deserialize JSON string.", e);
            }
        }
        LogUtil.logger.error("Unsupported input type for appointment creation: {}", input.getClass().getName());
        return null;
    }
}