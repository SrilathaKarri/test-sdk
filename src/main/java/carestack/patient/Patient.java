package carestack.patient;

import carestack.base.utils.ApplicationContextProvider;
import carestack.patient.abha.AbhaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import carestack.base.Base;
import carestack.base.DTO.SearchFiltersDTO;
import carestack.base.ResourceService;
import carestack.base.enums.Gender;
import carestack.base.enums.ResourceType;
import carestack.base.enums.StatesAndUnionTerritories;
import carestack.base.errors.EhrApiError;
import carestack.base.errors.ValidationError;
import carestack.base.utils.StringUtils;
import carestack.patient.DTO.PatientDTO;
import carestack.patient.DTO.UpdatePatientDTO;
import carestack.patient.enums.PatientIdType;
import carestack.patient.enums.PatientType;

/**
 * Service class for handling patient-related operations such as retrieving, creating, updating, and deleting patients.
 * This service interacts with an external EHR system via REST API calls using WebClient.
 * <p>
 * The service provides methods for:
 * <ul>
 *     <li>Retrieving all patients</li>
 *     <li>Fetching a patient by ID</li>
 *     <li>Checking patient existence</li>
 *     <li>Creating a new patient</li>
 *     <li>Updating an existing patient</li>
 *     <li>Searching for patients using filters</li>
 *     <li>Deleting a patient</li>
 * </ul>
 * </p>
 */
@Service
@Validated
public class Patient extends Base implements ResourceService<PatientDTO, UpdatePatientDTO> {

    private static final ResourceType RESOURCE_TYPE = ResourceType.Patient;

    /**
     * ABHA service instance for handling Ayushman Bharat Health Account operations.
     * Access ABHA functionality through: patient.abha.createAbha(step, payload)
     */
    private AbhaService abhaService;

    /**
     * Public getter for ABHA service with lazy initialization
     */
    public AbhaService abha() {
        return getAbhaService();
    }

    /**
     * Public field-style access to ABHA service (lazy-loaded)
     */
    public AbhaService getAbha() {
        return getAbhaService();
    }

    private AbhaService getAbhaService() {
        if (abhaService == null) {
            try {
                ApplicationContext context = ApplicationContextProvider.getApplicationContext();
                if (context != null) {
                    abhaService = context.getBean(AbhaService.class);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to get AbhaService bean", e);
            }
        }
        return abhaService;
    }

    protected Patient(ObjectMapper objectMapper, WebClient webClient) {
        super(objectMapper, webClient);
    }

    /**
     * <h3>Retrieve a Paginated List of Patients</h3>
     * Fetches a list of patient records from the EHR system, with optional pagination parameters.
     *
     * <h3>Input and Parameters:</h3>
     * This method supports flexible, optional parameters for pagination:
     * <ul>
     *     <li><b>pageSize (Integer):</b> Specifies the number of records per page. If not provided, the API's default (e.g., 10) is used.</li>
     *     <li><b>nextPage (String):</b> A token provided in a previous response to fetch the further page of results.</li>
     * </ul>
     *
     * <h3>Flexible Method Usage:</h3>
     * <ul>
     *     <li>{@code findAll()} - Uses default page size.</li>
     *     <li>{@code findAll(20)} - Sets page size to 20.</li>
     *     <li>{@code findAll("someNextPageToken")} - Fetches the next page using a token.</li>
     *     <li>{@code findAll(20, "someNextPageToken")} - Sets page size and fetches the next page.</li>
     * </ul>
     *
     * <h3>Output:</h3>
     * @return A {@link Mono<Object>} that emits the API response. The response is expected to be a structured object
     *         containing a list of patient records and potentially a {@code nextPage} token for further pagination.
     *
     * <h3>Error Handling:</h3>
     * If an error occurs during the API request (e.g., network failure, server error), the method will return a
     * {@link Mono#error(Throwable)} with the error wrapped in a custom {@link EhrApiError}.
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * // Fetch the first page with 15 patients per page
     * patientService.findAll(15)
     *     .subscribe(
     *         response -> System.out.println("Patients list: " + response),
     *         error -> System.err.println("Error fetching patients: " + error.getMessage())
     *     );
     *
     * // Expected Output (example):
     * // Patients list: { "data": [ { "id": "pat_1", ... }, { "id": "pat_2", ... } ], "nextPage": "tokenForPage2" }
     * }</pre>
     *
     * @param params Optional parameters: an {@link Integer} for pageSize and/or a {@link String} for nextPage.
     */
    @Override
    public Mono<Object> findAll(Object... params) {
        return handleFindAll(RESOURCE_TYPE, params);
    }

    /**
     * <h3>Retrieve a Patient's Profile by ID</h3>
     * Fetches the complete details of a single patient from the EHR system using their unique identifier.
     *
     * <h3>Input and Validation:</h3>
     * The method requires a non-null, non-empty patient ID string. This is validated by the base handler.
     *
     * <h3>Caching:</h3>
     * This method is cached using Spring's {@link Cacheable} annotation. Successful responses are stored in the
     * {@code patientCache} with a key format of {@code 'patient-{id}'}. Further calls with the same ID will
     * return the cached result, improving performance and reducing redundant API calls.
     *
     * <h3>Output:</h3>
     * @return A {@link Mono<Object>} that emits the patient's full profile as a structured object (e.g., a Map or a DTO).
     *
     * <h3>Error Handling:</h3>
     * <ul>
     *     <li>Returns a {@link Mono#error(Throwable)} with a {@link ValidationError} if the provided {@code id} is null or empty.</li>
     *     <li>Propagates a {@link Mono#error(Throwable)} if the API call fails (e.g., 404 Not Found, 500 Server Error).</li>
     * </ul>
     *
     * @param id The unique ID(resource id) of the patient to retrieve.
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * String patientId = "pat_12345";
     * patientService.findById(patientId)
     *     .subscribe(
     *         patient -> System.out.println("Patient Details: " + patient),
     *         error -> System.err.println("Error finding patient " + patientId + ": " + error.getMessage())
     *     );
     *
     * // Expected Output (example):
     * // Patient Details: { "id": "pat_12345", "firstName": "John", "lastName": "Doe", ... }
     * }</pre>
     *
     * @return A {@link Mono} emitting the patient's details.
     */
    @Override
    @Cacheable(value = "patientCache", key = "'patient-' + #id")
    public Mono<Object> findById(String id) {
        return handleFindById(RESOURCE_TYPE, id);
    }

    /**
     * <h3>Check if a Patient Exists</h3>
     * Determines whether a patient with a specific ID exists in the EHR system.
     *
     * <h3>Input and Validation:</h3>
     * The method requires a non-null, non-empty patient ID string.
     *
     * <h3>Logic:</h3>
     * This method delegates to a base handler which typically performs a lightweight check, such as a {@code HEAD} request
     * or a {@code GET} request, and evaluates the response status to determine existence.
     *
     * <h3>Output:</h3>
     * @return A {@code Mono<Boolean>} that emits {@code true} if the patient exists, and {@code false} if the patient does not exist (e.g., on a 404 response).
     *
     * <h3>Error Handling:</h3>
     * <ul>
     *     <li>Returns a {@link Mono#error(Throwable)} with a {@link ValidationError} if the provided {@code id} is null or empty.</li>
     *     <li>Propagates API or network errors. This allows the caller to distinguish between "not found" (which emits {@code false}) and "can't determine due to an error" (which emits an error).</li>
     * </ul>
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * String patientId = "pat_12345";
     * patientService.exists(patientId)
     *     .subscribe(
     *         exists -> System.out.println("Patient " + patientId + " exists: " + exists),
     *         error -> System.err.println("Error checking for patient existence: " + error.getMessage())
     *     );
     *
     * // Possible Outputs:
     * // Patient pat_12345 exists: true
     * // or
     * // Patient pat_12345 exists: false
     * // or (on API failure)
     * // Error checking for patient existence: 500 Internal Server Error
     * }</pre>
     *
     * @param id The unique ID(resource id) of the patient to check.
     * @return A {@link Mono} emitting {@code true} if the patient exists, {@code false} otherwise.
     */
    @Override
    public Mono<Boolean> exists(String id) {
        if (StringUtils.isNullOrEmpty(id)) {
            return Mono.error(new ValidationError("Patient ID cannot be null or empty."));
        }
        return handleExists(RESOURCE_TYPE, id);
    }


    /**
     * <h3>Create a New Patient</h3>
     * Sends a request to the EHR system to create a new patient record based on the provided data.
     *
     * <h3>Input and Validation:</h3>
     * The method accepts a {@link PatientDTO} object. The {@code @Valid} annotation on the parameter triggers automatic
     * validation based on the constraints defined within the {@link PatientDTO} class (e.g., {@code @NotBlank}, {@code @NotNull}, {@code @Pattern}).
     * If validation fails, the framework will throw an exception (e.g., {@code WebExchangeBindException}).
     * A manual check also ensures the DTO object itself is not null.
     *
     * <h3>API Request:</h3>
     * This method delegates to a base handler to make a <b>POST</b> request to the patient creation endpoint,
     * sending the serialized {@link PatientDTO} object in the request body.
     *
     * <h3>Output:</h3>
     * @return A {@link Mono<Object>} that, upon success, emits the response from the EHR system. This response typically
     *         confirms the creation and may include the new patient's unique ID.
     *
     * <h3>Error Handling:</h3>
     * <ul>
     *     <li>Returns a {@link Mono#error(Throwable)} with a {@link ValidationError} if the input {@code patient} object is {@code null}.</li>
     *     <li>Propagates a {@link Mono#error(Throwable)} if the underlying API call fails, wrapped in a custom {@link EhrApiError}.</li>
     * </ul>
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * PatientDTO newPatient = new PatientDTO(
     *     "123456789012",                 // ID Number
     *     PatientIdType.AADHAAR,          // ID Type
     *     "ram@abha",                     // ABHA Address
     *     PatientType.NEW,                // Patient Type
     *     "Ram",                          // First Name
     *     "S",                            // Middle Name
     *     "Kumar",                        // Last Name
     *     "1990-05-15",                   // Birth Date
     *     Gender.MALE,                    // Gender
     *     "ram.kumar@example.com",        // Email
     *     "+919876543210",                // Mobile Number
     *     "123 Temple Street, Ayodhya",   // Address
     *     "224123",                       // Pincode
     *     StatesAndUnionTerritories.UTTAR_PRADESH, // State
     *     true,                           // Wants to link WhatsApp
     *     null,                           // Photo (Optional)
     *     ResourceType.Patient,           // Resource Type
     *     null                            // Resource ID (Not needed for creation)
     * );
     *
     * patientService.create(newPatient)
     *       .subscribe(
     *          response -> System.out.println("Patient created successfully: " + response),
     *             error -> System.err.println("Failed to create patient: " + error.getMessage())
     *          );
     *
     * // Expected Output (example):
     * // Patient created successfully: { "status": "success", "id": "pat_67890", "message": "Patient created." }
     * }</pre>
     *
     * @param patient The {@link PatientDTO} object containing the new patient's details. Must not be null and must be valid.
     */
    @Override
    public Mono<Object> create(@Valid PatientDTO patient) {
        if (patient == null) {
            return Mono.error(new ValidationError("Patient data cannot be null."));
        }
        try {
            validateNewPatient(patient);
            return handleCreate(RESOURCE_TYPE, patient);
        } catch (ValidationError e) {
            return Mono.error(e);
        } catch (Exception e) {
            return Mono.error(EhrApiError.handleAndLogApiError(e));
        }
    }

    /**
     * <h3>Update an Existing Patient's Details</h3>
     * Modifies an existing patient's record in the EHR system using their unique resource ID.
     *
     * <h3>Input and Validation:</h3>
     * The method accepts an {@link UpdatePatientDTO} object. The {@code @Valid} annotation triggers automatic
     * validation based on the constraints in the DTO:
     * <ul>
     *     <li><b>resourceId:</b> Must not be null or blank. This is the key to identify which patient to update.</li>
     *     <li><b>emailId (Optional):</b> If provided, must be in a valid email format.</li>
     *     <li><b>mobileNumber (Optional):</b> If provided, must match the specified pattern (e.g., {@code ^[+]91[987]\d{9}$}).</li>
     *     <li><b>resourceType:</b> Must not be null.</li>
     * </ul>
     * A manual check also ensures the DTO object itself is not null.
     *
     * <h3>API Request:</h3>
     * This method delegates to a base handler to make a <b>PUT</b> or <b>PATCH</b> request to the patient update endpoint,
     * sending the serialized {@link UpdatePatientDTO} object in the request body.
     *
     * <h3>Output:</h3>
     * @return A {@link Mono<Object>} that, upon success, emits the response from the EHR system, confirming the update.
     *
     * <h3>Error Handling:</h3>
     * <ul>
     *     <li>Returns a {@link Mono#error(Throwable)} with a {@link ValidationError} if the input {@code updatePatientData} object is {@code null}.</li>
     *     <li>Propagates a framework-level exception (e.g., {@code WebExchangeBindException}) if DTO validation fails.</li>
     *     <li>Propagates a {@link Mono#error(Throwable)} if the underlying API call fails, wrapped in a custom {@link EhrApiError}.</li>
     * </ul>
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * UpdatePatientDTO updateData = new UpdatePatientDTO(
     *     "pat_67890",                  // Resource ID of the patient to update
     *     "new.ram.kumar@example.com",  // New Email
     *     "+919998887776",              // New Mobile Number
     *     ResourceType.Patient          // Resource Type
     * );
     *
     * patientService.update(updateData)
     *      .subscribe(
     *           response -> System.out.println("Patient updated successfully: " + response),
     *           error -> System.err.println("Failed to update patient: " + error.getMessage())
     *       );
     *
     * // Expected Output (example):
     * // Patient updated successfully: { "status": "success", "id": "pat_67890", "message": "Patient updated." }
     * }</pre>
     *
     * @param updatePatientData The {@link UpdatePatientDTO} object containing the fields to update. Must not be null and must be valid.
     *
     */
    @Override
    public Mono<Object> update(@Valid UpdatePatientDTO updatePatientData) {
        if (updatePatientData == null) {
            return Mono.error(new ValidationError("Update patient data cannot be null."));
        }
        try {
            validateUpdatePatient(updatePatientData);
            return handleUpdate(RESOURCE_TYPE, updatePatientData);
        } catch (ValidationError e) {
            return Mono.error(e);
        } catch (Exception e) {
            return Mono.error(EhrApiError.handleAndLogApiError(e));
        }
    }

    /**
     * <h3>Search for Patients with Flexible Filters</h3>
     * Fetches a list of patient records from the EHR system based on a flexible set of search criteria and pagination options.
     *
     * <h3>Input and Parameters:</h3>
     * This method accepts a variable number of arguments (varargs) which can include:
     * <ul>
     *     <li><b>{@link SearchFiltersDTO} (Optional):</b> An object containing the search criteria. If not provided, default (empty) filters are used.</li>
     *     <li><b>pageSize (Integer, Optional):</b> The number of records per page.</li>
     *     <li><b>nextPage (String, Optional):</b> A token for fetching the next page of results.</li>
     * </ul>
     *
     * <h3>Available Search Filters in {@code SearchFiltersDTO}:</h3>
     * The search can be refined using fields like {@code firstName}, {@code lastName}, {@code birthDate}, {@code gender}, {@code mobileNumber}, {@code state}, etc.
     * The specific validation rules for these filters are typically enforced by the backend API.
     *
     * <h3>Output:</h3>
     * @return A {@link Mono<Object>} that emits the API response. The response is expected to be a structured object
     *         containing a list of patient records that match the filters, along with pagination information.
     *
     * <h3>Error Handling:</h3>
     * Propagates a {@link Mono#error(Throwable)} if the underlying API call fails, wrapped in a custom {@link EhrApiError}.
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * SearchFiltersDTO filters = new SearchFiltersDTO();
     * filters.setGender(Gender.MALE);
     * filters.setState(StatesAndUnionTerritories.UTTAR_PRADESH);
     *
     * // Search with filters and a page size of 5
     * patientService.findByFilters(filters, 5)
     *     .subscribe(
     *         response -> System.out.println("Search results: " + response),
     *         error -> System.err.println("Error during search: " + error.getMessage())
     *     );
     *
     * // Expected Output (example):
     * // Search results: { "data": [ { "id": "pat_xyz", "firstName": "Ram", ... }, { "id": "pat_abc", "firstName": "Laxman", ... } ], "nextPage": "tokenForNextPage" }
     * }</pre>
     *
     * @param params A sequence of optional parameters: {@link SearchFiltersDTO}, {@link Integer} (pageSize), {@link String} (nextPage).
     */
    @Override
    public Mono<Object> findByFilters(Object... params) {
        return handleFind(RESOURCE_TYPE, params);
    }

    /**
     * <h3>Delete a Patient Record by ID</h3>
     * Permanently removes a patient record from the EHR system using their unique identifier.
     *
     * <h3>Input and Validation:</h3>
     * The method requires a non-null, non-empty patient ID string. This is validated by the base handler.
     *
     * <h3>API Request:</h3>
     * This method delegates to a base handler to make a <b>DELETE</b> request to the patient deletion endpoint (e.g., {@code /patients/{id}}).
     *
     * <h3>Output:</h3>
     * @return A {@link Mono<Object>} that, upon success, emits the response from the EHR system, typically confirming the deletion.
     *
     * <h3>Error Handling:</h3>
     * <ul>
     *     <li>Returns a {@link Mono#error(Throwable)} with a {@link ValidationError} if the provided {@code id} is null or empty.</li>
     *     <li>Propagates a {@link Mono#error(Throwable)} if the API call fails (e.g., 404 Not Found if the patient doesn't exist), wrapped in a custom {@link EhrApiError}.</li>
     * </ul>
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * String patientIdToDelete = "pat_67890";
     *
     * patientService.delete(patientIdToDelete)
     *     .subscribe(
     *         response -> System.out.println("Deletion successful: " + response),
     *         error -> System.err.println("Failed to delete patient " + patientIdToDelete + ": " + error.getMessage())
     *     );
     *
     * // Expected Output (example):
     * // Deletion successful: { "status": "success", "message": "Patient with ID pat_67890 deleted." }
     * }</pre>
     *
     * @param id The unique identifier of the patient to be deleted. Must not be null or empty.
     */
    @Override
    public Mono<Object> delete(String id) {
        return handleDelete("patients", id);
    }

    /**
     * Validates the required fields for creating a new patient.
     *
     * @param patient The DTO to validate.
     * @throws ValidationError if any required field is missing or invalid.
     */
    private void validateNewPatient(PatientDTO patient) {
        if (StringUtils.isNullOrEmpty(patient.getIdNumber())) throw new ValidationError("ID Number is required.");
        if (patient.getIdType() == null) throw new ValidationError("ID Type is required.");
        if (!isValidEnum(patient.getIdType(), PatientIdType.class)) throw new ValidationError("Invalid ID Type.");
        if (StringUtils.isNullOrEmpty(patient.getFirstName())) throw new ValidationError("First Name is required.");
        if (StringUtils.isNullOrEmpty(patient.getLastName())) throw new ValidationError("Last Name is required.");
        if (StringUtils.isNullOrEmpty(patient.getBirthDate())) throw new ValidationError("Birth Date is required.");
        if (patient.getGender() == null) throw new ValidationError("Gender is required.");
        if (!isValidEnum(patient.getGender(), Gender.class)) throw new ValidationError("Invalid Gender.");
        if (StringUtils.isNullOrEmpty(patient.getMobileNumber())) throw new ValidationError("Mobile Number is required.");
        if (StringUtils.isNullOrEmpty(patient.getAddress())) throw new ValidationError("Address is required.");
        if (StringUtils.isNullOrEmpty(patient.getPincode())) throw new ValidationError("Pincode is required.");
        if (patient.getState() == null) throw new ValidationError("State is required.");
        if (!isValidEnum(patient.getState(), StatesAndUnionTerritories.class)) throw new ValidationError("Invalid State.");
        if (patient.getPatientType() == null) throw new ValidationError("Patient Type is required.");
        if (!isValidEnum(patient.getPatientType(), PatientType.class)) throw new ValidationError("Invalid Patient Type.");
        if (patient.getResourceType() == null) throw new ValidationError("Resource Type is required.");
        if (patient.getResourceType()!= ResourceType.Patient) throw new ValidationError("Resource Type must be patient.");
        if (!isValidEnum(patient.getResourceType(), ResourceType.class)) throw new ValidationError("Invalid Resource Type.");
    }

    /**
     * Validates the fields for updating an existing patient.
     *
     * @param updatePatientData The DTO to validate.
     * @throws ValidationError if the resource ID is missing.
     */
    private void validateUpdatePatient(@Valid UpdatePatientDTO updatePatientData) {
        if (StringUtils.isNullOrEmpty(updatePatientData.getResourceId())) {
            throw new ValidationError("Resource ID is required for updating a patient.");
        }

    }
}
