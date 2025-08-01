package carestack.organization;

import carestack.base.config.EmbeddedSdkProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import carestack.base.Base;
import carestack.base.errors.EhrApiError;
import carestack.base.errors.ErrorType;
import carestack.base.errors.ValidationError;
import carestack.base.utils.Constants;
import carestack.base.utils.LogUtil;
import carestack.base.utils.StringUtils;
import carestack.organization.dto.OrganizationDTO;
import carestack.organization.dto.ResponseDTOs;
import carestack.organization.dto.SearchOrganizationDTO;
import carestack.organization.dto.UpdateSpocForOrganization;
import carestack.organization.enums.OrganizationIdType;
import carestack.organization.enums.Region;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides specialized services for managing Organization resources.
 * <p>
 * This class handles all operations related to organizations, including searching,
 * registration, and fetching associated demographic or master data (like states,
 * districts, and facility types). Unlike other resource services, this class has
 * custom methods tailored specifically for organization management and does not
 * implement the generic {@code ResourceService} interface.
 * </p>
 */
@Service
public class Organization extends Base {

    @Autowired
    private EmbeddedSdkProperties embeddedProperties;

    private final String googleApiKey;
    private final Demographic demographic;
    private final WebClient webClient;

    /**
     * Constructs the Organization service with its dependencies.
     *
     * @param objectMapper The Jackson ObjectMapper for JSON serialization/deserialization.
     * @param webClient    The Spring WebClient for making reactive HTTP requests.
     * @param googleApiKey The API key for Google services, injected from properties.
     * @param demographic  The service for handling demographic-related API calls.
     */
    @Autowired
    public Organization(ObjectMapper objectMapper,
                        WebClient webClient,
                        @Value("${google.api.key:#{null}}") String googleApiKey,
                        Demographic demographic,
                        EmbeddedSdkProperties embeddedProperties) {
        super(objectMapper, webClient);
        this.googleApiKey = googleApiKey;
        this.demographic = demographic;
        this.webClient = webClient;
    }


    /**
     * Retrieves a paginated list of all organizations.
     * Fetches a list of organization records from the EHR system, with optional pagination.
     *
     * <h3>Parameters and Validation:</h3>
     * <ul>
     *     <li><b>pageSize (Integer, Optional):</b> The number of records per page.
     *         <ul>
     *             <li><b>Validation:</b> Must be between 1 and 100. Defaults to 10 if not provided.</li>
     *         </ul>
     *     </li>
     *     <li><b>nextPage (String, Optional):</b> A token from a previous response to fetch the subsequent page.</li>
     * </ul>
     *
     * <h3>Input:</h3>
     * The method accepts a variable number of arguments. You can call it with a page size, a next page token, both, or neither.
     *
     * <h3>Output:</h3>
     * @return A {@link Mono<Object>} that emits the API response, containing a list of organizations and pagination details.
     *
     * <h3>Error Handling:</h3>
     * <ul>
     *     <li>Returns a {@link Mono#error(Throwable)} with a {@link ValidationError} if {@code pageSize} is outside the valid range (1-100).</li>
     *     <li>Propagates API errors from the underlying service call.</li>
     * </ul>
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * // Fetch the first page with 15 organizations
     * organizationService.findAll(15)
     *     .subscribe(
     *         response -> System.out.println("Organizations list: " + response),
     *         error -> System.err.println("Error fetching organizations: " + error.getMessage())
     *     );
     * }</pre>
     *
     * <h3>Expected Output:</h3>
     * <pre>{@code
     * {
     *   "data": [
     *     { "id": "ORG001", "organizationName": "City Hospital", ... },
     *     { "id": "ORG002", "organizationName": "Community Clinic", ... }
     *   ],
     *   "nextPage": "someOpaqueTokenForPage2"
     * }
     * }</pre>
     *
     * @param params Optional parameters: an {@link Integer} for pageSize and/or a {@link String} for nextPage.
     */
    public Mono<Object> findAll(Object... params) {
        Integer pageSize = 10;
        String nextPage = null;

        for (Object param : params) {
            if (param instanceof Integer val) pageSize = val;
            else if (param instanceof String val) nextPage = val;
        }

        if (pageSize < 1 || pageSize > 100) {
            return Mono.error(new ValidationError("pageSize must be between 1 and 100"));
        }

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("pageSize", String.valueOf(pageSize));
        if (nextPage != null && !nextPage.trim().isEmpty()) {
            queryParams.put("nextPage", nextPage);
        }

        return get(Constants.GET_ORGANIZATIONS_URL, queryParams, new ParameterizedTypeReference<>() {});
    }

    /**
     * <h3>Find an Organization by ID</h3>
     * Fetches the complete details of a single organization using a specific identifier and its type.
     *
     * <h3>Caching:</h3>
     * This operation is cached. Successful responses are stored in the {@code organizationCache}.
     * Subsequent calls with the same {@code idType} and {@code id} will return the cached result.
     *
     * <h3>Parameters and Validation:</h3>
     * <ul>
     *     <li><b>idType (String):</b> The type of identifier being used (e.g., "accountId", "facilityId").
     *         <ul><li><b>Validation:</b> Cannot be null or empty. Must correspond to a valid {@link OrganizationIdType}.</li></ul>
     *     </li>
     *     <li><b>id (String):</b> The unique identifier of the organization.
     *         <ul><li><b>Validation:</b> Cannot be null or empty.</li></ul>
     *     </li>
     * </ul>
     *
     * <h3>Output:</h3>
     * @return A {@link Mono<Object>} that emits the found organization's details.
     *
     * <h3>Error Handling:</h3>
     * <ul>
     *     <li>Returns a {@link Mono#error(Throwable)} with a {@link ValidationError} if inputs are null/empty or if the {@code idType} is invalid.</li>
     * </ul>
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * organizationService.findById("facilityId", "HFR-XYZ-123")
     *     .subscribe(
     *         org -> System.out.println("Organization Details: " + org),
     *         error -> System.err.println("Error finding organization: " + error.getMessage())
     *     );
     * }</pre>
     *
     * <h3>Expected Output:</h3>
     * <pre>{@code
     * {
     *   "id": "someInternalId",
     *   "organizationId": "HFR-XYZ-123",
     *   "organizationName": "Sunshine Hospital",
     *   "basicInformation": { ... },
     *   ...
     * }
     * }</pre>
     *
     * @param idType The type of the identifier. See {@link OrganizationIdType}.
     * @param id     The unique identifier of the organization.
     */
    @Cacheable(value = "organizationCache", key = "#idType + '-' + #id")
    public Mono<Object> findById(String idType, String id) {
        if (StringUtils.isNullOrEmpty(idType) || StringUtils.isNullOrEmpty(id)) {
            return Mono.error(new ValidationError("Organization ID and ID Type cannot be null or empty."));
        }
        try {
            OrganizationIdType orgIdType = OrganizationIdType.fromString(idType);
            String endpoint = String.format("%s/%s/%s", Constants.GET_ORGANIZATIONS_URL, orgIdType.getValue(), id);
            return get(endpoint, new ParameterizedTypeReference<>() {});
        } catch (EhrApiError e) {
            return Mono.error(new ValidationError("Invalid Organization ID Type provided."));
        }
    }

    /**
     * <h3>Check if an Organization Exists</h3>
     * Determines if an organization with the given ID and type exists in the system.
     *
     * <h3>Note:</h3>
     * The current implementation is brittle as it relies on parsing specific string messages
     * from the API response (e.g., "Facility Found !!!"). This may break if the API changes.
     *
     * <h3>Parameters and Validation:</h3>
     * <ul>
     *     <li><b>idType (String):</b> The type of the identifier.
     *         <ul><li><b>Validation:</b> Cannot be null or empty.</li></ul>
     *     </li>
     *     <li><b>id (String):</b> The unique identifier of the organization.
     *         <ul><li><b>Validation:</b> Cannot be null or empty.</li></ul>
     *     </li>
     * </ul>
     *
     * <h3>Output:</h3>
     * @return A {@link Mono<Boolean>} emitting {@code true} if the organization exists, {@code false} otherwise.
     *
     * <h3>Error Handling:</h3>
     * <ul>
     *     <li>Returns a {@link Mono#error(Throwable)} with a {@link ValidationError} if inputs are null or empty.</li>
     *     <li>Returns {@code false} if any other error occurs during the API call (e.g., 404 Not Found, 500 Server Error).</li>
     * </ul>
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * organizationService.exists("facilityId", "HFR-XYZ-123")
     *     .subscribe(
     *         exists -> System.out.println("Does organization exist? " + exists),
     *         error -> System.err.println("Error checking existence: " + error.getMessage())
     *     );
     * }</pre>
     *
     * <h3>Expected Output:</h3>
     * <pre>{@code
     * true
     * }</pre>
     */
    public Mono<Boolean> exists(String idType, String id) {
        if (StringUtils.isNullOrEmpty(idType) || StringUtils.isNullOrEmpty(id)) {
            return Mono.error(new ValidationError("ID and ID Type are required to check for existence."));
        }
        return findById(idType, id)
                .map(response -> {
                    if (response instanceof Map<?, ?> responseMap && responseMap.get("message") instanceof String message) {
                        return "Facility Found !!!".equalsIgnoreCase(message.trim()) || "Records Found!!".equalsIgnoreCase(message.trim());
                    }
                    return false;
                })
                .onErrorReturn(false);
    }

    /**
     * Find organizations based on a set of filter criteria.
     * Searches for organizations based on a set of filter criteria provided in a DTO.
     *
     * <h3>Parameters and Validation:</h3>
     * <ul>
     *     <li><b>searchData (SearchOrganizationDTO):</b> A DTO containing filter criteria.
     *         <ul><li><b>Validation:</b> The DTO is validated via {@code @Valid}. It cannot be null.</li></ul>
     *     </li>
     * </ul>
     *
     * <h3>Output:</h3>
     * @return A {@link Mono<Object>} emitting the list of matching organizations.
     *
     * <h3>Error Handling:</h3>
     * <ul>
     *     <li>Returns a {@link Mono#error(Throwable)} with a {@link ValidationError} if {@code searchData} is null.</li>
     *     <li>Propagates validation exceptions if the DTO is invalid.</li>
     * </ul>
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * SearchOrganizationDTO filters = new SearchOrganizationDTO();
     * filters.setStateLGDCode("27"); // Maharashtra
     * filters.setPincode("400053");
     * filters.setPage(1);
     * filters.setResultsPerPage(20);
     *
     * organizationService.findByFilters(filters)
     *     .subscribe(results -> System.out.println("Search Results: " + results));
     * }</pre>
     *
     * <h3>Expected Output:</h3>
     * <pre>{@code
     * {
     *   "data": [ { "organizationName": "Andheri West Clinic", ... } ],
     *   "total": 1,
     *   "page": 1
     * }
     * }</pre>
     *
     * @param searchData A {@link SearchOrganizationDTO} containing the filter criteria.
     */
    public Mono<Object> findByFilters(@Valid SearchOrganizationDTO searchData) {
        if (searchData == null) {
            return Mono.error(new ValidationError("Search data cannot be null."));
        }
        return post(Constants.SEARCH_ORGANIZATION_URL, searchData, new ParameterizedTypeReference<>() {});
    }

    /**
     * <h3>Register a New Organization</h3>
     * Creates a new organization record in the system.
     *
     * <h3>Parameters and Validation:</h3>
     * <ul>
     *     <li><b>organizationDTO (OrganizationDTO):</b> A DTO containing all details of the new organization.
     *         <ul><li><b>Validation:</b> The DTO is validated via {@code @Valid} and an internal {@code validateOrganization} method, checking for required nested objects and fields.</li></ul>
     *     </li>
     * </ul>
     *
     * <h3>Output:</h3>
     * @return A {@link Mono<Object>} emitting the response from the registration API, typically confirming creation.
     *
     * <h3>Error Handling:</h3>
     * <ul>
     *     <li>Returns a {@link Mono#error(Throwable)} with a {@link ValidationError} if the DTO is null or fails validation checks.</li>
     * </ul>
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * OrganizationDTO newOrg = // ... build a complete OrganizationDTO object
     *
     * organizationService.register(newOrg)
     *     .subscribe(response -> System.out.println("Registration Response: " + response));
     * }</pre>
     *
     * <h3>Expected Output:</h3>
     * <pre>{@code
     * {
     *   "status": "success",
     *   "message": "Organization registered successfully.",
     *   "facilityId": "HFR-NEW-456"
     * }
     * }</pre>
     *
     * @param organizationDTO A {@link OrganizationDTO} containing all the details of the new organization.
     */
    public Mono<Object> register(@Valid OrganizationDTO organizationDTO) {
        return validateOrganization(organizationDTO)
                .flatMap(validatedOrg -> post(Constants.REGISTER_ORGANIZATION_URL, validatedOrg, new ParameterizedTypeReference<>() {}));
    }

    /**
     * <h3>Update an Organization's SPOC</h3>
     * Updates the "Single Point of Contact" (SPOC) for an existing organization.
     *
     * <h3>Parameters and Validation:</h3>
     * <ul>
     *     <li><b>updateData (UpdateSpocForOrganization):</b> A DTO containing the organization ID and new SPOC details.
     *         <ul><li><b>Validation:</b> The DTO is validated via {@code @Valid}. It cannot be null, and its {@code id} field must not be empty.</li></ul>
     *     </li>
     * </ul>
     *
     * <h3>Output:</h3>
     * @return A {@link Mono<Object>} emitting the response from the update API.
     *
     * <h3>Error Handling:</h3>
     * <ul>
     *     <li>Returns a {@link Mono#error(Throwable)} with an {@link IllegalArgumentException} if {@code updateData} is null.</li>
     *     <li>Returns a {@link Mono#error(Throwable)} with a {@link ValidationError} if the organization {@code id} is missing.</li>
     * </ul>
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * UpdateSpocForOrganization spocUpdate = new UpdateSpocForOrganization();
     * spocUpdate.setId("HFR-XYZ-123");
     * spocUpdate.setSpocName("Jane Doe");
     * spocUpdate.setSpocId("EMP-002");
     *
     * organizationService.updateSpoc(spocUpdate)
     *     .subscribe(response -> System.out.println("SPOC Update Response: " + response));
     * }</pre>
     *
     * <h3>Expected Output:</h3>
     * <pre>{@code
     * {
     *   "status": "success",
     *   "message": "SPOC updated successfully."
     * }
     * }</pre>
     *
     * @param updateData A {@link UpdateSpocForOrganization} DTO containing the organization ID and new SPOC details.
     */
    public Mono<Object> updateSpoc(@Valid UpdateSpocForOrganization updateData) {
        if (updateData == null) {
            return Mono.error(new IllegalArgumentException("Update SPOC data cannot be null."));
        }
        if (StringUtils.isNullOrEmpty(updateData.getId())) {
            return Mono.error(new ValidationError("Organization ID is required for updating SPOC."));
        }
        return put(Constants.UPDATE_ORGANIZATION_URL, updateData, new ParameterizedTypeReference<>() {});
    }

    /**
     * <h3>Delete an Organization by ID</h3>
     * Permanently removes an organization record from the system using its unique identifier.
     *
     * <h3>Parameters and Validation:</h3>
     * <ul>
     *     <li><b>id (String):</b> The unique identifier of the organization to delete.
     *         <ul><li><b>Validation:</b> Cannot be null or empty.</li></ul>
     *     </li>
     * </ul>
     *
     * <h3>Output:</h3>
     * @return A {@link Mono<Void>} that completes when the deletion is successful. It does not emit any value.
     *
     * <h3>Error Handling:</h3>
     * <ul>
     *     <li>Returns a {@link Mono#error(Throwable)} with a {@link ValidationError} if the {@code id} is null or empty.</li>
     *     <li>Propagates API errors (e.g., 404 if the organization doesn't exist, 500 for server issues).</li>
     * </ul>
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * String orgIdToDelete = "HFR-XYZ-123";
     * organizationService.delete(orgIdToDelete)
     *     .subscribe(
     *         () -> System.out.println("Organization " + orgIdToDelete + " deleted successfully."),
     *         error -> System.err.println("Error deleting organization: " + error.getMessage())
     *     );
     * }</pre>
     *
     * <h3>Expected Output:</h3>
     * The Mono completes successfully, and a log message is printed to the console. No object is returned.
     *
     * @param id The unique ID of the organization to delete.
     */
    public Mono<Void> delete(String id) {
        if (StringUtils.isNullOrEmpty(id)) {
            return Mono.error(new ValidationError("Organization ID cannot be null or empty."));
        }
        String endpoint = String.format("%s/%s", Constants.DELETE_ORGANIZATION_URL, id);
        return delete(endpoint, new ParameterizedTypeReference<Void>() {})
                .doOnSuccess(v -> LogUtil.logger.info("Organization with ID '{}' deleted successfully.", id))
                .doOnError(e -> LogUtil.logger.error("Error deleting organization with ID '{}': {}", id, e.getMessage()));
    }


    /**
     * <h3>Fetch All States from LGD</h3>
     * Fetches a complete list of all states and their associated districts from the LGD (Local Government Directory) API.
     *
     * <h3>Parameters and Validation:</h3>
     * This method takes no parameters.
     *
     * <h3>Output:</h3>
     * @return A {@link Mono} that emits a {@code List<ResponseDTOs.StateDTO>}. Each {@code StateDTO} contains details about a state, including a nested list of its districts.
     *
     * <h3>Error Handling:</h3>
     * Propagates any API errors encountered during the fetch operation.
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * organizationService.getLgdStates()
     *     .subscribe(states -> {
     *         System.out.println("Total states found: " + states.size());
     *         states.forEach(state -> System.out.println("- " + state.getName()));
     *     });
     * }</pre>
     *
     * <h3>Expected Output:</h3>
     * <pre>{@code
     * [
     *   { "code": "27", "name": "MAHARASHTRA", "districts": [...] },
     *   { "code": "29", "name": "KARNATAKA", "districts": [...] },
     *   ...
     * ]
     * }</pre>
     */
    public Mono<List<ResponseDTOs.StateDTO>> getLgdStates() {
        return get(Constants.FETCH_STATES_URL, new ParameterizedTypeReference<>() {});
    }

    /**
     * <h3>Extract Districts for a Specific State</h3>
     * Filters a pre-fetched list of states to find and return the districts for a specific state, identified by its state code.
     * This is a client-side filtering operation and does not make an API call.
     *
     * <h3>Parameters and Validation:</h3>
     * <ul>
     *     <li><b>stateCode (String):</b> The LGD code of the state for which to get districts (e.g., "27" for Maharashtra).
     *         <ul><li><b>Validation:</b> Should be a valid, non-null string.</li></ul>
     *     </li>
     *     <li><b>states (List&lt;ResponseDTOs.StateDTO&gt;):</b> The complete list of states, typically fetched via {@link #getLgdStates()}.
     *         <ul><li><b>Validation:</b> Should be a non-null list.</li></ul>
     *     </li>
     * </ul>
     *
     * <h3>Output:</h3>
     * @return A {@link Mono} emitting a {@code List<ResponseDTOs.DistrictDTO>} for the given state. If the state code is not found, it emits an empty list.
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * // First, fetch all states
     * List<ResponseDTOs.StateDTO> allStates = organizationService.getLgdStates().block();
     *
     * // Then, extract districts for a specific state
     * organizationService.getDistrictsFromState("27", allStates)
     *     .subscribe(districts -> System.out.println("Districts in Maharashtra: " + districts));
     * }</pre>
     *
     * <h3>Expected Output:</h3>
     * <pre>{@code
     * [
     *   { "code": "519", "name": "Mumbai" },
     *   { "code": "520", "name": "Pune" },
     *   ...
     * ]
     * }</pre>
     *
     * @param stateCode The code of the state for which to get districts.
     * @param states    The complete list of states and their districts.
     */
    public Mono<List<ResponseDTOs.DistrictDTO>> getDistrictsFromState(String stateCode, List<ResponseDTOs.StateDTO> states) {
        return Mono.fromCallable(() ->
                states.stream()
                        .filter(state -> state.getCode().equals(stateCode))
                        .findFirst()
                        .map(ResponseDTOs.StateDTO::getDistricts)
                        .orElse(Collections.emptyList())
        );
    }

    /**
     * <h3>Fetch Sub-Districts by District Code</h3>
     * Fetches a list of sub-districts (or talukas/tehsils) that belong to a specific district, identified by its LGD code.
     *
     * <h3>Parameters and Validation:</h3>
     * <ul>
     *     <li><b>districtCode (Integer):</b> The LGD code of the district.
     *         <ul><li><b>Validation:</b> Cannot be null.</li></ul>
     *     </li>
     * </ul>
     *
     * <h3>Output:</h3>
     * @return A {@link Mono} emitting a list of {@link ResponseDTOs.DistrictDTO} objects, where each object represents a subdistrict with its own code and name.
     *
     * <h3>Error Handling:</h3>
     * <ul>
     *     <li>Returns a {@link Mono#error(Throwable)} with an {@link IllegalArgumentException} if {@code districtCode} is null.</li>
     *     <li>Propagates API errors from the underlying demographic service call.</li>
     * </ul>
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * Integer puneDistrictCode = 520;
     * organizationService.getSubDistricts(puneDistrictCode)
     *     .subscribe(subDistricts -> System.out.println("Sub-districts in Pune: " + subDistricts));
     * }</pre>
     *
     * <h3>Expected Output:</h3>
     * <pre>{@code
     * [
     *   { "code": "4185", "name": "Haveli" },
     *   { "code": "4186", "name": "Maval" },
     *   ...
     * ]
     * }</pre>
     *
     * @param districtCode The LGD code of the district.
     */
    public Mono<List<ResponseDTOs.DistrictDTO>> getSubDistricts(Integer districtCode) {
        if (districtCode == null) {
            return Mono.error(new IllegalArgumentException("District code cannot be null"));
        }
        String url = String.format("%s%s", Constants.FETCH_SUB_DISTRICTS_URL,
                String.format(Constants.DISTRICT_CODE_PARAM, districtCode));

        return demographic.fetchData(url, new ParameterizedTypeReference<>() {});
    }

    /**
     * <h3>Geocode a Location String</h3>
     * Retrieves the latitude and longitude for a given location string (e.g., a full address) by querying the Google Maps Geocoding API.
     *
     * <h3>Parameters and Validation:</h3>
     * <ul>
     *     <li><b>location (String):</b> A human-readable address or location description.
     *         <ul><li><b>Validation:</b> The string is URL-encoded before being sent. It should be a meaningful location for best results. Cannot be null or empty.</li></ul>
     *     </li>
     * </ul>
     *
     * <h3>Output:</h3>
     * @return A {@link Mono} emitting a {@link ResponseDTOs.LocationResponseDTO} containing the geographic coordinates and other location details.
     *
     * <h3>Error Handling:</h3>
     * <ul>
     *     <li>Returns a {@link Mono#error(Throwable)} with a {@link ValidationError} if the {@code location} is null or empty.</li>
     *     <li>Returns a {@link Mono#error(Throwable)} with an {@link EhrApiError} (type {@code NOT_FOUND}) if the Google API returns no results for the location.</li>
     *     <li>Propagates other API or network errors from the WebClient call.</li>
     * </ul>
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * String address = "1600 Amphitheatre Parkway, Mountain View, CA";
     * organizationService.getLatitudeLongitude(address)
     *     .subscribe(locationResponse -> {
     *         if (!locationResponse.getResults().isEmpty()) {
     *             ResponseDTOs.LocationDTO coords = locationResponse.getResults().get(0).getGeometry().getLocation();
     *             System.out.println("Latitude: " + coords.getLat() + ", Longitude: " + coords.getLng());
     *         }
     *     });
     * }</pre>
     *
     * <h3>Expected Output:</h3>
     * <pre>{@code
     * { "results": [ { "geometry": { "location": { "lat": 37.4224764, "lng": -122.0842499 } }, ... } ], "status": "OK" }
     * }</pre>
     *
     * @param location A string representing the location (e.g., "1600 Amphitheatre Parkway, Mountain View, CA").
     */
    public Mono<ResponseDTOs.LocationResponseDTO> getLatitudeLongitude(String location) {
        String encodedLocation = URLEncoder.encode(location, StandardCharsets.UTF_8);
        String googleApi = (googleApiKey != null && !googleApiKey.trim().isEmpty())
                ? googleApiKey
                : embeddedProperties.getGoogleApiKey();
        String fullUrl = Constants.GOOGLE_LOCATION_URL + "?address=" + encodedLocation + "&key=" + googleApi;

        return webClient.get()
                .uri(fullUrl)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ResponseDTOs.LocationResponseDTO>() {})
                .flatMap(response -> {
                    if (response == null || response.getResults().isEmpty()) {
                        return Mono.error(new EhrApiError(
                                "No data found for this location. Please enter a valid location.",
                                ErrorType.NOT_FOUND));
                    }
                    return Mono.just(response);
                })
                .onErrorMap(EhrApiError::handleAndLogApiError);
    }

    /**
     * <h3>Fetch Master Data Types</h3>
     * Fetches a list of all available master data categories from the demographic service. These types can then be used to fetch specific master data lists.
     *
     * <h3>Parameters and Validation:</h3>
     * This method takes no parameters.
     *
     * <h3>Output:</h3>
     * @return A {@link Mono} emitting a {@link ResponseDTOs.masterTypesDTO} containing the list of types.
     *
     * <h3>Error Handling:</h3>
     * Propagates API errors from the underlying demographic service call.
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * organizationService.getMasterTypes()
     *     .subscribe(masterTypes -> {
     *         System.out.println("Available Master Data Types:");
     *         masterTypes.getMasterTypes().forEach(type -> System.out.println("- " + type.getType() + ": " + type.getDesc()));
     *     });
     * }</pre>
     *
     * <h3>Expected Output:</h3>
     * <pre>{@code
     * {
     *   "masterTypes": [
     *     { "type": "ownership", "desc": "Facility Ownership" },
     *     { "type": "speciality", "desc": "Medical Specialities" },
     *     ...
     *   ]
     * }
     * }</pre>
     */
    public Mono<ResponseDTOs.masterTypesDTO> getMasterTypes() {
        return demographic.fetchData(Constants.FETCH_MASTER_TYPES_URL, new ParameterizedTypeReference<>() {});
    }

    /**
     * <h3>Fetch Master Data by Type</h3>
     * Fetches a list of code-value pairs for a specific master data type (e.g., all "ownership" types).
     *
     * <h3>Parameters and Validation:</h3>
     * <ul>
     *     <li><b>type (String):</b> The master data type to fetch (e.g., "ownership").
     *         <ul><li><b>Validation:</b> Cannot be null or empty.</li></ul>
     *     </li>
     * </ul>
     *
     * <h3>Input:</h3>
     * A string representing the master data category.
     *
     * <h3>Output:</h3>
     * @return A {@link Mono} emitting a {@link ResponseDTOs.TypeDataResponseDTO} containing the data for the specified type.
     *
     * <h3>Error Handling:</h3>
     * <ul>
     *     <li>Returns a {@link Mono#error(Throwable)} with a {@link ValidationError} if the {@code type} is null or empty.</li>
     *     <li>Propagates API errors from the underlying demographic service call.</li>
     * </ul>
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * organizationService.getMasterDataByType("ownership")
     *     .subscribe(data -> {
     *         System.out.println("Ownership Types:");
     *         data.getData().forEach(item -> System.out.println("- Code: " + item.getCode() + ", Value: " + item.getValue()));
     *     });
     * }</pre>
     *
     * <h3>Expected Output:</h3>
     * <pre>{@code
     * {
     *   "type": "ownership",
     *   "data": [
     *     { "code": "PVT", "value": "Private" },
     *     { "code": "GOV", "value": "Government" }
     *   ]
     * }
     * }</pre>
     *
     * @param type The master data type to fetch (e.g., "ownership").
     */
    public Mono<ResponseDTOs.TypeDataResponseDTO> getMasterDataByType(String type){
        String url = String.format("%s/%s",Constants.FETCH_MASTER_DATA_URL,type);
        return demographic.fetchCodeValuePairs(url);
    }

    /**
     * <h3>Fetch Ownership Subtypes</h3>
     * Fetches a list of ownership subtypes based on a parent ownership type code. For example, if the parent is "Government", subtypes might include "State Government" or "Central Government".
     *
     * <h3>Parameters and Validation:</h3>
     * <ul>
     *     <li><b>ownershipTypeCode (String):</b> The code of the parent ownership type.
     *         <ul><li><b>Validation:</b> Cannot be null or empty.</li></ul>
     *     </li>
     * </ul>
     *
     * <h3>Input:</h3>
     * A string representing the parent ownership code.
     *
     * <h3>Output:</h3>
     * @return A {@link Mono} emitting a {@link ResponseDTOs.TypeDataResponseDTO} containing the list of subtypes.
     *
     * <h3>Error Handling:</h3>
     * <ul>
     *     <li>Returns a {@link Mono#error(Throwable)} with a {@link ValidationError} if the {@code ownershipTypeCode} is null or empty.</li>
     *     <li>Propagates API errors from the underlying demographic service call.</li>
     * </ul>
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * organizationService.getOwnershipSubTypes("GOV")
     *     .subscribe(subTypes -> {
     *         System.out.println("Government Ownership Subtypes:");
     *         subTypes.getData().forEach(item -> System.out.println("- " + item.getValue()));
     *     });
     * }</pre>
     *
     * <h3>Expected Output:</h3>
     * <pre>{@code
     * {
     *   "type": "ownershipSubType",
     *   "data": [
     *     { "code": "SG", "value": "State Government" },
     *     { "code": "CG", "value": "Central Government" }
     *   ]
     * }
     * }</pre>
     *
     * @param ownershipTypeCode The code of the parent ownership type for which to fetch subtypes.
     */
    public Mono<ResponseDTOs.TypeDataResponseDTO> getOwnershipSubTypes(String ownershipTypeCode) {
        return demographic.fetchCodeValuePairs(Constants.FETCH_OWNERSHIP_SUBTYPE_URL, Map.of("ownershipCode", ownershipTypeCode));
    }

    /**
     * <h3>Fetch Medical Specialities by System of Medicine</h3>
     * Fetches a list of medical specialities available for a given system of medicine (e.g., "Allopathy", "Ayurveda").
     *
     * <h3>Parameters and Validation:</h3>
     * <ul>
     *     <li><b>systemOfMedicineCode (String):</b> The code for the system of medicine.
     *         <ul><li><b>Validation:</b> Cannot be null or empty.</li></ul>
     *     </li>
     * </ul>
     *
     * <h3>Input:</h3>
     * A string representing the system of medicine code.
     *
     * <h3>Output:</h3>
     * @return A {@link Mono} emitting a {@link ResponseDTOs.TypeDataResponseDTO} containing the list of specialities.
     *
     * <h3>Error Handling:</h3>
     * <ul>
     *     <li>Returns a {@link Mono#error(Throwable)} with a {@link ValidationError} if the {@code systemOfMedicineCode} is null or empty.</li>
     *     <li>Propagates API errors from the underlying demographic service call.</li>
     * </ul>
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * organizationService.getSpecialitiesForMedicine("AYUSH")
     *     .subscribe(specialities -> {
     *         System.out.println("AYUSH Specialities:");
     *         specialities.getData().forEach(item -> System.out.println("- " + item.getValue()));
     *     });
     * }</pre>
     *
     * <h3>Expected Output:</h3>
     * <pre>{@code
     * {
     *   "type": "speciality",
     *   "data": [
     *     { "code": "AYUR", "value": "Ayurveda" },
     *     { "code": "YOGA", "value": "Yoga & Naturopathy" }
     *   ]
     * }
     * }</pre>
     *
     * @param systemOfMedicineCode The code for the system of medicine (e.g., "ALLO" for Allopathy).
     */
    public Mono<ResponseDTOs.TypeDataResponseDTO> getSpecialitiesForMedicine(String systemOfMedicineCode) {
        return demographic.fetchCodeValuePairs(Constants.FETCH_SPECIALITIES_URL, Map.of("systemOfMedicineCode", systemOfMedicineCode));
    }

    /**
     * <h3>Fetch Organization Types by Ownership</h3>
     * Fetches a list of valid organization types (e.g., "Hospital", "Clinic") based on a parent ownership code.
     *
     * <h3>Parameters and Validation:</h3>
     * <ul>
     *     <li><b>ownershipCode (String):</b> The code of the ownership type.
     *         <ul><li><b>Validation:</b> Cannot be null or empty.</li></ul>
     *     </li>
     * </ul>
     *
     * <h3>Input:</h3>
     * A string representing the ownership code.
     *
     * <h3>Output:</h3>
     * @return A {@link Mono} emitting a {@link ResponseDTOs.TypeDataResponseDTO} containing the list of organization types.
     *
     * <h3>Error Handling:</h3>
     * <ul>
     *     <li>Returns a {@link Mono#error(Throwable)} with a {@link ValidationError} if the {@code ownershipCode} is null or empty.</li>
     *     <li>Propagates API errors from the underlying demographic service call.</li>
     * </ul>
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * organizationService.getOrganizationTypes("PVT")
     *     .subscribe(orgTypes -> {
     *         System.out.println("Organization Types for Private Ownership:");
     *         orgTypes.getData().forEach(item -> System.out.println("- " + item.getValue()));
     *     });
     * }</pre>
     *
     * <h3>Expected Output:</h3>
     * <pre>{@code
     * {
     *   "type": "facilityType",
     *   "data": [
     *     { "code": "HOSP", "value": "Hospital" },
     *     { "code": "CLIN", "value": "Clinic" }
     *   ]
     * }
     * }</pre>
     *
     * @param ownershipCode The code of the ownership type for which to fetch organization types.
     */
    public Mono<ResponseDTOs.TypeDataResponseDTO> getOrganizationTypes(String ownershipCode) {
        return demographic.fetchCodeValuePairs(Constants.FETCH_ORGANIZATION_TYPE_URL, Map.of("ownershipCode", ownershipCode));
    }

    /**
     * <h3>Fetch Organization Subtypes by Type</h3>
     * Fetches a list of organization subtypes based on a parent organization type code. For example, if the type is "Hospital", subtypes might include "General" or "Super Speciality".
     *
     * <h3>Parameters and Validation:</h3>
     * <ul>
     *     <li><b>organizationTypeCode (String):</b> The code of the parent organization type.
     *         <ul><li><b>Validation:</b> Cannot be null or empty.</li></ul>
     *     </li>
     * </ul>
     *
     * <h3>Input:</h3>
     * A string representing the parent organization type code.
     *
     * <h3>Output:</h3>
     * @return A {@link Mono} emitting a {@link ResponseDTOs.TypeDataResponseDTO} containing the list of subtypes.
     *
     * <h3>Error Handling:</h3>
     * <ul>
     *     <li>Returns a {@link Mono#error(Throwable)} with a {@link ValidationError} if the {@code organizationTypeCode} is null or empty.</li>
     *     <li>Propagates API errors from the underlying demographic service call.</li>
     * </ul>
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * organizationService.getOrganizationSubTypes("HOSP")
     *     .subscribe(subTypes -> {
     *         System.out.println("Hospital Subtypes:");
     *         subTypes.getData().forEach(item -> System.out.println("- " + item.getValue()));
     *     });
     * }</pre>
     *
     * <h3>Expected Output:</h3>
     * <pre>{@code
     * {
     *   "type": "facilitySubType",
     *   "data": [
     *     { "code": "GEN", "value": "General Hospital" },
     *     { "code": "SUP", "value": "Super Speciality Hospital" }
     *   ]
     * }
     * }</pre>
     *
     * @param organizationTypeCode The code of the parent organization type for which to fetch subtypes.
     */
    public Mono<ResponseDTOs.TypeDataResponseDTO> getOrganizationSubTypes(String organizationTypeCode) {
        return demographic.fetchCodeValuePairs(Constants.FETCH_ORGANIZATION_SUBTYPE_URL, Map.of("facilityTypeCode", organizationTypeCode));
    }


    /**
     * <h3>Validate an Organization DTO</h3>
     * Performs server-side validation on the mandatory fields and nested objects within an {@link OrganizationDTO} before it is sent for registration.
     *
     * <h3>Parameters and Validation:</h3>
     * <ul>
     *     <li><b>organizationDTO (OrganizationDTO):</b> The DTO to validate.
     *         <ul>
     *              <li><b>Validation:</b> The DTO itself cannot be null. The following nested objects must also not be null:
     *                  <ul>
     *                      <li>{@code basicInformation}</li>
     *                      <li>{@code contactInformation}</li>
     *                      <li>{@code organizationDetails}</li>
     *                  </ul>
     *              </li>
     *              <li>The {@code accountId} and {@code region} fields are also checked for non-nullity.</li>
     *              <li>The {@code region} is validated to ensure it is a valid enum value.</li>
     *         </ul>
     *     </li>
     * </ul>
     *
     * <h3>Input:</h3>
     * An {@link OrganizationDTO} object intended for registration.
     *
     * <h3>Output:</h3>
     * @return A {@link Mono} that emits the validated {@link OrganizationDTO} if all checks pass. If validation fails, it emits a {@link Mono#error(Throwable)} with a {@link ValidationError}.
     *
     * <h3>Error Handling:</h3>
     * Returns a {@link Mono#error(Throwable)} with a specific {@link ValidationError} message indicating which field failed validation.
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * // This method is typically used internally by the register() method.
     * OrganizationDTO myOrg = new OrganizationDTO();
     * // ... populate DTO, but forget to set contact info
     * myOrg.setBasicInformation(new BasicInformation());
     *
     * organizationService.validateOrganization(myOrg)
     *     .subscribe(
     *         validatedOrg -> System.out.println("Validation successful!"),
     *         error -> System.err.println("Validation failed: " + error.getMessage())
     *     );
     * }</pre>
     *
     * <h3>Expected Output (for the example):</h3>
     * <pre>{@code
     * Validation failed: Contact information is required.
     * }</pre>
     *
     * @param organizationDTO The DTO to validate.
     * @return A {@link Mono} emitting the validated DTO, or an error if validation fails.
     */
    Mono<OrganizationDTO> validateOrganization(OrganizationDTO organizationDTO) {
        return Mono.justOrEmpty(organizationDTO)
                .switchIfEmpty(Mono.error(new ValidationError("Organization data cannot be null.")))
                .flatMap(org -> {
                    if (org.getBasicInformation() == null) return Mono.error(new ValidationError("Basic information is required."));
                    if (org.getContactInformation() == null) return Mono.error(new ValidationError("Contact information is required."));
                    if (org.getOrganizationDetails() == null) return Mono.error(new ValidationError("Organization details are required."));
                    if (StringUtils.isNullOrEmpty(org.getAccountId())) return Mono.error(new ValidationError("Account ID is required."));
                    if (org.getBasicInformation().getRegion() == null) return Mono.error(new ValidationError("Region is required."));

                    return isValidEnum(org.getBasicInformation().getRegion(), Region.class)
                            ? Mono.just(org)
                            : Mono.error(new ValidationError("Invalid Region Type provided."));
                });
    }
}