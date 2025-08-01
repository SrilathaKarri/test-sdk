package carestack.practitioner;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.cache.annotation.Cacheable;
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

/**
 * Provides services for managing Practitioner resources within the EHR system.
 * <p>
 * This class is the main entry point for all practitioner-related operations. It implements the
 * {@link ResourceService} interface to provide a standardized, resource-oriented API. Built on a
 * reactive stack with {@link WebClient}, all operations are non-blocking and return a {@link Mono},
 * making it suitable for high-performance, scalable applications.
 * </p>
 * <p>
 * The service provides methods for:
 * <ul>
 *     <li>Retrieving all practitioners with pagination</li>
 *     <li>Fetching a practitioner by their unique ID</li>
 *     <li>Checking if a practitioner exists</li>
 *     <li>Creating a new practitioner</li>
 *     <li>Updating an existing practitioner</li>
 *     <li>Searching for practitioners using various filters</li>
 *     <li>Deleting a practitioner</li>
 * </ul>
 * </p>
 */
@Service
@Validated
public class Practitioner extends Base implements ResourceService<PractitionerDTO, PractitionerDTO> {

    private static final ResourceType RESOURCE_TYPE = ResourceType.Practitioner;

    protected Practitioner(ObjectMapper objectMapper, WebClient webClient) {
        super(objectMapper, webClient);
    }

    /**
     * <h3>Retrieve a Paginated List of Practitioners</h3>
     * Fetches a list of practitioner records from the EHR system, with optional pagination parameters.
     *
     * <h3>Input and Parameters:</h3>
     * This method supports flexible, optional parameters for pagination:
     * <ul>
     *     <li><b>pageSize (Integer):</b> Specifies the number of records per page. If not provided, the API's default is used.</li>
     *     <li><b>nextPage (String):</b> A token from a previous response to fetch the further page of results.</li>
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
     * @return A {@link Mono<Object>} that emits the API response, which is expected to be a structured object
     *         containing a list of practitioner records and a {@code nextPage} token for further pagination.
     *
     * <h3>Error Handling:</h3>
     * If an error occurs during the API request (e.g., network failure, server error), the method will return a
     * {@link Mono#error(Throwable)} with the error wrapped in a custom {@link EhrApiError}.
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * // Fetch the first page with 15 practitioners per page
     * practitionerService.findAll(15)
     *     .subscribe(
     *         response -> System.out.println("Practitioners list: " + response),
     *         error -> System.err.println("Error fetching practitioners: " + error.getMessage())
     *     );
     *
     * // Expected Output (example):
     * // Practitioners list: { "data": [ { "id": "prac_1", ... }, { "id": "prac_2", ... } ], "nextPage": "tokenForPage2" }
     * }</pre>
     *
     * @param params Optional parameters: an {@link Integer} for pageSize and/or a {@link String} for nextPage.
     */
    @Override
    public Mono<Object> findAll(Object... params) {
        return handleFindAll(RESOURCE_TYPE, params);
    }

    /**
     * <h3>Retrieve a Practitioner's Profile by ID</h3>
     * Fetches the complete details of a single practitioner from the EHR system using their unique identifier.
     *
     * <h3>Input and Validation:</h3>
     * The method requires a non-null, non-empty practitioner ID string. This is validated by the base handler.
     *
     * <h3>Caching:</h3>
     * This method is cached using Spring's {@link Cacheable} annotation. Successful responses are stored in the
     * {@code practitionerCache} with a key format of {@code 'practitioner-{id}'}. Further calls with the same ID will
     * return the cached result, improving performance and reducing redundant API calls.
     *
     * <h3>Output:</h3>
     * @return A {@link Mono<Object>} that emits the practitioner's full profile as a structured object (e.g., a Map or a DTO).
     *
     * <h3>Error Handling:</h3>
     * <ul>
     *     <li>Returns a {@link Mono#error(Throwable)} with a {@link ValidationError} if the provided {@code id} is null or empty.</li>
     *     <li>Propagates a {@link Mono#error(Throwable)} if the API call fails (e.g., 404 Not Found, 500 Server Error).</li>
     * </ul>
     *

     * <h3>Example Usage:</h3>
     * <pre>{@code
     * String practitionerId = "prac_12345";
     * practitionerService.findById(practitionerId)
     *     .subscribe(
     *         practitioner -> System.out.println("Practitioner Details: " + practitioner),
     *         error -> System.err.println("Error finding practitioner " + practitionerId + ": " + error.getMessage())
     *     );
     *
     * // Expected Output (example):
     * // Practitioner Details: { "id": "prac_12345", "firstName": "John", "lastName": "Doe", "department": "Cardiology", ... }
     * }</pre>
     *
     *@param id The unique ID(resource id) of the practitioner to retrieve.
      * @return A {@link Mono} emitting the practitioner's details.
     */
    @Override
    @Cacheable(value = "practitionerCache", key = "'practitioner-' + #id")
    public Mono<Object> findById(String id) {
        return handleFindById(RESOURCE_TYPE, id);
    }

    /**
     * <h3>Check if a Practitioner Exists</h3>
     * Determines whether a practitioner with a specific ID exists in the EHR system.
     *
     * <h3>Input and Validation:</h3>
     * The method requires a non-null, non-empty practitioner ID string.
     *
     * <h3>Logic:</h3>
     * This method delegates to a base handler which performs a lightweight check (e.g., a {@code HEAD} request)
     * and evaluates the response status to determine existence.
     *
     * <h3>Output:</h3>
     * @return A {@code Mono<Boolean>} that emits {@code true} if the practitioner exists, and {@code false} if not (e.g., on a 404 response).
     *
     * <h3>Error Handling:</h3>
     * <ul>
     *     <li>Returns a {@link Mono#error(Throwable)} with a {@link ValidationError} if the provided {@code id} is null or empty.</li>
     *     <li>Propagates API or network errors, allowing the caller to distinguish between "not found" (emits {@code false}) and "can't determine due to an error" (emits an error).</li>
     * </ul>
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * String practitionerId = "prac_12345";
     * practitionerService.exists(practitionerId)
     *     .subscribe(
     *         exists -> System.out.println("Practitioner " + practitionerId + " exists: " + exists),
     *         error -> System.err.println("Error checking for practitioner existence: " + error.getMessage())
     *     );
     *
     * // Possible Outputs:
     * // Practitioner prac_12345 exists: true
     * // or
     * // Practitioner prac_12345 exists: false
     * // or (on API failure)
     * // Error checking for practitioner existence: 500 Internal Server Error
     * }</pre>
     *
     * @param id The unique identifier(resource id) of the practitioner. Must not be null or empty.
     * @return A {@link Mono} emitting {@code true} if the practitioner exists, {@code false} otherwise.
     */
    @Override
    public Mono<Boolean> exists(String id) {
        if (StringUtils.isNullOrEmpty(id)) {
            return Mono.error(new ValidationError("Practitioner ID cannot be null or empty."));
        }
        return handleExists(RESOURCE_TYPE, id);
    }

    /**
     * <h3>Create a New Practitioner</h3>
     *  Sends a request to the EHR system to create a new practitioner record.
     *
     * <h3>Input and Validation:</h3>
     * The method accepts a {@link PractitionerDTO} object. The {@code @Valid} annotation triggers automatic
     * validation based on constraints in the DTO (e.g., {@code @NotBlank}, {@code @NotNull}). Additionally,
     * this method performs a manual validation check via the private {@code validate} method for business-critical fields.
     *
     * <h3>API Request:</h3>
     * This method delegates to a base handler to make a <b>POST</b> request to the practitioner creation endpoint,
     * sending the serialized {@link PractitionerDTO} in the request body.
     *
     * <h3>Output:</h3>
     * @return A {@link Mono<Object>} that, upon success, emits the response from the EHR system. This response typically
     *         confirms the creation and includes the new practitioner's unique ID.
     *
     * <h3>Error Handling:</h3>
     * <ul>
     *     <li>Returns a {@link Mono#error(Throwable)} with a {@link ValidationError} if the input DTO is {@code null} or fails manual validation.</li>
     *     <li>Propagates a {@link Mono#error(Throwable)} if the underlying API call fails, wrapped in a custom {@link EhrApiError}.</li>
     * </ul>
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * PractitionerDTO newPractitioner = PractitionerDTO.builder()
     *     .registrationId("REG-54321")
     *     .department("Pediatrics")
     *     .designation("Consultant")
     *     .status("active")
     *     .joiningDate("2023-01-15")
     *     .staffType("Full-Time")
     *     .firstName("Jane")
     *     .lastName("Doe")
     *     .birthDate("1985-10-20")
     *     .gender(Gender.FEMALE)
     *     .mobileNumber("9876543210")
     *     .emailId("jane.doe@hospital.com")
     *     .address("456 Health Ave")
     *     .pincode("500081")
     *     .state(StatesAndUnionTerritories.TELANGANA)
     *     .resourceType(ResourceType.Practitioner)
     *     .build();
     *
     * practitionerService.create(newPractitioner)
     *       .subscribe(
     *          response -> System.out.println("Practitioner created successfully: " + response),
     *          error -> System.err.println("Failed to create practitioner: " + error.getMessage())
     *       );
     *
     * // Expected Output (example):
     * // Practitioner created successfully: { "status": "success", "id": "prac_67890", "message": "Practitioner created." }
     * }</pre>
     *
     * @param practitioner The {@link PractitionerDTO} object containing the new practitioner's details. Must not be null and must be valid.
     */
    @Override
    public Mono<Object> create(@Valid PractitionerDTO practitioner) {
        if (practitioner == null) {
            return Mono.error(new ValidationError("Practitioner data cannot be null."));
        }
        try {
            validate(practitioner);
            return handleCreate(RESOURCE_TYPE, practitioner);
        } catch (ValidationError e) {
            return Mono.error(e);
        } catch (Exception e) {
            return Mono.error(EhrApiError.handleAndLogApiError(e));
        }
    }


    /**
     * <h3>Update an Existing Practitioner</h3>
     * Modifies an existing practitioner's record in the EHR system using their unique resource ID.
     *
     * <h3>Input and Validation:</h3>
     * The method accepts a {@link PractitionerDTO} object.
     * <ul>
     *     <li><b>resourceId:</b> Must not be null or blank within the DTO. This is crucial for identifying which practitioner to update.</li>
     *     <li>The {@code @Valid} annotation triggers automatic validation for other fields based on DTO constraints.</li>
     *     <li>A manual validation check is also performed via the private {@code validate} method.</li>
     * </ul>
     *
     * <h3>API Request:</h3>
     * This method delegates to a base handler to make a <b>PUT</b> or <b>PATCH</b> request to the practitioner update endpoint,
     * sending the serialized {@link PractitionerDTO} in the request body.
     *
     * <h3>Output:</h3>
     * @return A {@link Mono<Object>} that, upon success, emits the response from the EHR system, confirming the update.
     *
     * <h3>Error Handling:</h3>
     * <ul>
     *     <li>Returns a {@link Mono#error(Throwable)} with a {@link ValidationError} if the input DTO is {@code null}, fails validation, or is missing a {@code resourceId}.</li>
     *     <li>Propagates a {@link Mono#error(Throwable)} if the underlying API call fails, wrapped in a custom {@link EhrApiError}.</li>
     * </ul>
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * // Assume practitioner 'prac_67890' exists. We are updating their designation.
     * PractitionerDTO updateData = PractitionerDTO.builder()
     *     .resourceId("prac_67890") // Required for update
     *     .registrationId("REG-54321")
     *     .department("Pediatrics")
     *     .designation("Senior Consultant") // Updated field
     *     .status("active")
     *     .joiningDate("2023-01-15")
     *     .staffType("Full-Time")
     *     .firstName("Jane")
     *     .lastName("Doe")
     *     .birthDate("1985-10-20")
     *     .gender(Gender.FEMALE)
     *     .mobileNumber("9876543211") // Updated field
     *     .emailId("jane.doe@hospital.com")
     *     .address("456 Health Ave")
     *     .pincode("500081")
     *     .state(StatesAndUnionTerritories.TELANGANA)
     *     .resourceType(ResourceType.Practitioner)
     *     .build();
     *
     * practitionerService.update(updateData)
     *      .subscribe(
     *          response -> System.out.println("Practitioner updated successfully: " + response),
     *         error -> System.err.println("Failed to update practitioner: " + error.getMessage())
     *      );
     *
     * // Expected Output (example):
     * // Practitioner updated successfully: { "status": "success", "id": "prac_67890", "message": "Practitioner updated." }
     *  }</pre>
     *
     *  @param updatePractitionerData The DTO containing the fields to update.
     */
    @Override
    public Mono<Object> update(@Valid PractitionerDTO updatePractitionerData) {
        if (updatePractitionerData == null) {
            return Mono.error(new ValidationError("Update practitioner data cannot be null."));
        }
        try {
            if (StringUtils.isNullOrEmpty(updatePractitionerData.getResourceId())) {
                throw new ValidationError("Resource ID is required for updating a practitioner.");
            }
            validate(updatePractitionerData);
            return handleUpdate(RESOURCE_TYPE, updatePractitionerData);
        } catch (ValidationError e) {
            return Mono.error(e);
        } catch (Exception e) {
            return Mono.error(EhrApiError.handleAndLogApiError(e));
        }
    }

    /**
     * <h3>Search for Practitioners with Flexible Filters</h3>
     * Fetches a list of practitioner records based on a flexible set of search criteria and pagination options.
     *
     * <h3>Input and Parameters:</h3>
     * This method accepts a variable number of arguments (varargs) which can include:
     * <ul>
     *     <li><b>{@link SearchFiltersDTO} (Optional):</b> An object containing the search criteria.</li>
     *     <li><b>pageSize (Integer, Optional):</b> The number of records per page.</li>
     *     <li><b>nextPage (String, Optional):</b> A token for fetching the next page of results.</li>
     * </ul>
     *
     * <h3>Available Search Filters in {@code SearchFiltersDTO}:</h3>
     * The search can be refined using fields like {@code firstName}, {@code lastName}, {@code department}, {@code designation}, {@code mobileNumber}, {@code state}, etc.
     *
     * <h3>Output:</h3>
     * @return A {@link Mono<Object>} that emits the API response, containing a list of matching practitioner records and pagination information.
     *
     * <h3>Error Handling:</h3>
     * Propagates a {@link Mono#error(Throwable)} if the underlying API call fails, wrapped in a custom {@link EhrApiError}.
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * SearchFiltersDTO filters = new SearchFiltersDTO();
     * filters.setDepartment("Pediatrics");
     * filters.setStatus("active");
     *
     * // Search for active practitioners in the Pediatrics department, with a page size of 5
     * practitionerService.findByFilters(filters, 5)
     *     .subscribe(
     *         response -> System.out.println("Search results: " + response),
     *         error -> System.err.println("Error during search: " + error.getMessage())
     *     );
     *
     * // Expected Output (example):
     * // Search results: { "data": [ { "id": "prac_xyz", "firstName": "Jane", ... }, ... ], "nextPage": "tokenForNextPage" }
     * }</pre>
     *
     * @param params A sequence of optional parameters: {@link SearchFiltersDTO}, {@link Integer} (pageSize), {@link String} (nextPage).
     */
    @Override
    public Mono<Object> findByFilters(Object... params) {
        return handleFind(RESOURCE_TYPE, params);
    }

    /**
     * <h3>Delete a Practitioner Record by ID</h3>
     * Removes a practitioner record from the EHR system using their unique identifier.
     *
     * <h3>Input and Validation:</h3>
     * The method requires a non-null, non-empty practitioner ID string. This is validated by the base handler.
     *
     * <h3>API Request:</h3>
     * This method delegates to a base handler to make a <b>DELETE</b> request to the practitioner deletion endpoint (e.g., {@code /practitioners/{id}}).
     *
     * <h3>Output:</h3>
     * @return A {@link Mono<Object>} that, upon success, emits the response from the EHR system, typically confirming the deletion.
     *
     * <h3>Error Handling:</h3>
     * <ul>
     *     <li>Returns a {@link Mono#error(Throwable)} with a {@link ValidationError} if the provided {@code id} is null or empty.</li>
     *     <li>Propagates a {@link Mono#error(Throwable)} if the API call fails (e.g., 404 Not Found), wrapped in a custom {@link EhrApiError}.</li>
     * </ul>
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * String practitionerIdToDelete = "prac_67890";
     *
     * practitionerService.delete(practitionerIdToDelete)
     *     .subscribe(
     *         response -> System.out.println("Deletion successful: " + response),
     *         error -> System.err.println("Failed to delete practitioner " + practitionerIdToDelete + ": " + error.getMessage())
     *     );
     *
     * // Expected Output (example):
     * // Deletion successful: { "status": "success", "message": "Practitioner with ID prac_67890 deleted." }
     * }</pre>
     *
     * @param id The unique identifier of the practitioner to be deleted. Must not be null or empty.
     */
    @Override
    public Mono<Object> delete(String id) {
        return handleDelete("practitioners", id);
    }

    /**
     * Performs manual validation on a {@link PractitionerDTO} object before a create or update operation.
     * This ensures that all business-critical fields are present and valid.
     *
     * @param practitioner The DTO to validate.
     * @throws ValidationError if any required field is missing or invalid.
     */
    private void validate(PractitionerDTO practitioner) {
        if (StringUtils.isNullOrEmpty(practitioner.getRegistrationId())) throw new ValidationError("Registration ID is required.");
        if (StringUtils.isNullOrEmpty(practitioner.getDepartment())) throw new ValidationError("Department is required.");
        if (StringUtils.isNullOrEmpty(practitioner.getDesignation())) throw new ValidationError("Designation is required.");
        if (StringUtils.isNullOrEmpty(practitioner.getStatus())) throw new ValidationError("Status is required.");
        if (practitioner.getJoiningDate() == null) throw new ValidationError("Joining Date is required.");
        if (StringUtils.isNullOrEmpty(practitioner.getFirstName())) throw new ValidationError("First Name is required.");
        if (StringUtils.isNullOrEmpty(practitioner.getLastName())) throw new ValidationError("Last Name is required.");
        if (practitioner.getBirthDate() == null) throw new ValidationError("Birth Date is required.");
        if (practitioner.getGender() == null) throw new ValidationError("Gender is required.");
        if (!isValidEnum(practitioner.getGender(), Gender.class)) throw new ValidationError("Invalid Gender.");
        if (StringUtils.isNullOrEmpty(practitioner.getMobileNumber())) throw new ValidationError("Mobile Number is required.");
        if (StringUtils.isNullOrEmpty(practitioner.getEmailId())) throw new ValidationError("Email ID is required.");
        if (StringUtils.isNullOrEmpty(practitioner.getAddress())) throw new ValidationError("Address is required.");
        if (StringUtils.isNullOrEmpty(practitioner.getPincode())) throw new ValidationError("Pincode is required.");
        if (practitioner.getState() == null) throw new ValidationError("State is required.");
        if (!isValidEnum(practitioner.getState(), StatesAndUnionTerritories.class)) throw new ValidationError("Invalid State.");
        if (practitioner.getResourceType() == null) throw new ValidationError("Resource Type is required.");
        if (practitioner.getResourceType()!= ResourceType.Practitioner) throw new ValidationError("Resource Type must be practitioner.");
        if (!isValidEnum(practitioner.getResourceType(), ResourceType.class)) throw new ValidationError("Invalid Resource Type.");
    }

}