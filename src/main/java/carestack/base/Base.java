package carestack.base;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import carestack.base.DTO.SearchFiltersDTO;
import carestack.base.enums.ResourceType;
import carestack.base.errors.EhrApiError;
import carestack.base.errors.ValidationError;
import carestack.base.utils.Constants;
import carestack.base.utils.LogUtil;
import carestack.base.utils.StringUtils;
import carestack.organization.enums.Region;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

/**
 * Provides core functionalities for interacting with the EHR API using a reactive WebClient.
 * <p>
 * This abstract class encapsulates common logic for making GET, POST, PUT, and DELETE requests.
 * It also includes standardized helper methods for handling common resource operations like
 * creating, finding, updating, and deleting, as well as logic for paginated and filtered searches.
 * Services should extend this class to inherit this foundational behavior.
 * </p>
 */
@Service
public abstract class Base {

//    @Autowired
//    private EmbeddedSdkProperties embeddedProperties;
//
//    @Autowired
//    private WebClientConfig webClientConfig;
//
//    @Value("${api.url:#{null}}")
//    private String userApiUrl;

    protected final ObjectMapper objectMapper;
    protected final WebClient webClient;

    protected Base(ObjectMapper objectMapper, WebClient webClient) {
        this.objectMapper = objectMapper;
        this.webClient = webClient;
    }

    /**
     * A helper record to parse and hold parameters from a varargs array.
     */
    private record ParsedParams(SearchFiltersDTO filters, Integer pageSize, String nextPage) {
        static ParsedParams from(Object... params) {
            SearchFiltersDTO f = null;
            Integer ps = null;
            String np = null;

            for (Object param : params) {
                if (param instanceof SearchFiltersDTO dto) {
                    f = dto;
                } else if (param instanceof Integer i) {
                    ps = i;
                } else if (param instanceof String s) {
                    np = s;
                }
            }
            return new ParsedParams(f, ps, np);
        }
    }


    /**
     * Sends a GET request with query parameters to a relative path.
     *
     * @param relativePath The relative endpoint path (e.g., "/patients").
     * @param queryParams  The query parameters to be included.
     * @param responseType The expected response type.
     * @param <T>          The response object type.
     * @return A Mono containing the API response.
     */
    public <T> Mono<T> get(String relativePath, Map<String, String> queryParams, ParameterizedTypeReference<T> responseType) {
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        if (queryParams != null && !queryParams.isEmpty()) {
            queryParams.forEach(multiValueMap::add);
        }

        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path(relativePath).queryParams(multiValueMap).build())
                .retrieve()
                .bodyToMono(responseType)
                .onErrorMap(EhrApiError::handleAndLogApiError);
    }

    /**
     * Sends a GET request to a relative path without query parameters.
     *
     * @param relativePath The relative endpoint path (e.g., "/patients/123").
     * @param responseType The expected response type.
     * @param <T>          The response object type.
     * @return A Mono containing the API response.
     */
    public <T> Mono<T> get(String relativePath, ParameterizedTypeReference<T> responseType) {
//        String fullUrl = String.format("%s%s", webClientConfig.determineApiUrl(), relativePath);
        return webClient.get()
                .uri(relativePath)
                .retrieve()
                .bodyToMono(responseType)
                .onErrorMap(EhrApiError::handleAndLogApiError);
    }

    /**
     * Sends a POST request with a request body to a relative path.
     *
     * @param relativePath The relative API endpoint path.
     * @param requestBody  The request payload.
     * @param responseType The expected response type.
     * @param <T>          The response object type.
     * @return A Mono containing the API response.
     */
    public <T> Mono<T> post(String relativePath, Object requestBody, ParameterizedTypeReference<T> responseType) {
        return webClient.post()
                .uri(relativePath)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(responseType)
                .onErrorMap(EhrApiError::handleAndLogApiError);
    }

    /**
     * Sends a PUT request with a request body to a relative path.
     *
     * @param relativePath The relative API endpoint path.
     * @param requestBody  The request payload.
     * @param responseType The expected response type.
     * @param <T>          The response object type.
     * @return A Mono containing the API response.
     */
    public <T> Mono<T> put(String relativePath, Object requestBody, ParameterizedTypeReference<T> responseType) {
        return webClient.put()
                .uri(relativePath)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(responseType)
                .onErrorMap(EhrApiError::handleAndLogApiError);
    }

    /**
     * Sends a DELETE request to a relative path.
     *
     * @param relativePath The relative API endpoint path.
     * @param responseType The expected response type reference.
     * @param <T>          The response object type.
     * @return A Mono containing the API response.
     */
    public <T> Mono<T> delete(String relativePath, ParameterizedTypeReference<T> responseType) {
        return webClient.delete()
                .uri(relativePath)
                .retrieve()
                .bodyToMono(responseType)
                .onErrorMap(EhrApiError::handleAndLogApiError);
    }


    /**
     * Handles a generic request to find all resources of a given type, with optional pagination.
     *
     * @param resourceType The type of resource to find (e.g., Patient, Appointment).
     * @param params       Optional parameters for pagination, such as page size (Integer) and next page token (String).
     * @return A {@link Mono} emitting the paginated list of resources.
     */

    public Mono<Object> handleFindAll(ResourceType resourceType,Object... params) {
        Integer pageSize = null;
        String nextPage = null;

        // Determine parameter types dynamically
        for (Object param : params) {
            if (param instanceof Integer) {
                pageSize = (Integer) param;
            } else if (param instanceof String) {
                nextPage = (String) param;
            }
        }

        // Apply default value if pageSize is not provided
        if (pageSize == null) {
            pageSize = 10;
        }
        if (pageSize < 1 || pageSize > 100) {
            return Mono.error(new IllegalArgumentException("pageSize must be between 1 and 100"));
        }
        try {
            Map<String, Object> filters = new HashMap<>();
            filters.put("_count", pageSize);

            String encodedFilters = URLEncoder.encode(objectMapper.writeValueAsString(filters), StandardCharsets.UTF_8);

            String url = String.format("%s/%s?filters=%s%s",
                    Constants.GET_PROFILES_URL,
                    resourceType.name(),
                    encodedFilters,
                    Optional.ofNullable(nextPage)
                            .filter(s -> !s.isEmpty())
                            .map(s -> "&nextPage=" + URLEncoder.encode(s, StandardCharsets.UTF_8))
                            .orElse("")
            );

            return get(url, new ParameterizedTypeReference<>() {});

        } catch (Exception e) {
            return Mono.error(EhrApiError.handleAndLogApiError(e));
        }
    }


    /**
     * Handles a generic request to find a single resource by its unique ID.
     *
     * @param resourceType The type of resource to find.
     * @param id           The unique identifier of the resource.
     * @return A {@link Mono} emitting the found resource.
     */

    public Mono<Object> handleFindById(ResourceType resourceType, String id) {
        if (StringUtils.isNullOrEmpty(id)) {
            return Mono.error(new ValidationError(resourceType.name() + " ID cannot be null or empty."));
        }
        String endpoint = String.format("/get/%s/%s", resourceType.name(), id);
        return get(endpoint, new ParameterizedTypeReference<>() {});
    }

    /**
     * Handles a generic request to check for the existence of a resource by its ID.
     *
     * @param resourceType The type of resource to check.
     * @param id           The unique identifier of the resource.
     * @return A {@link Mono} emitting {@code true} if the resource exists, {@code false} otherwise.
     */
    public Mono<Boolean> handleExists(ResourceType resourceType, String id) {
        return handleFindById(resourceType, id)
                .map(response -> {
                    if (response instanceof Map<?, ?> responseMap) {
                        Object totalRecords = responseMap.get("totalNumberOfRecords");
                        return totalRecords instanceof Integer && (Integer) totalRecords > 0;
                    }
                    return false;
                })
                .onErrorReturn(false);
    }

    /**
     * Handles a generic request to create a new resource.
     *
     * @param resourceType The type of resource to create.
     * @param createDto    The DTO containing the data for the new resource.
     * @return A {@link Mono} emitting the created resource.
     */
    public Mono<Object> handleCreate(ResourceType resourceType, Object createDto) {
        String endpoint = String.format("/add/%s", resourceType.name());
        return post(endpoint, createDto, new ParameterizedTypeReference<>() {});
    }

    /**
     * Handles a generic request to update an existing resource.
     *
     * @param resourceType The type of resource to update.
     * @param updateDto    The DTO containing the updated data.
     * @return A {@link Mono} emitting the updated resource.
     */
    public Mono<Object> handleUpdate(ResourceType resourceType, Object updateDto) {
        String endpoint = String.format("/update/%s", resourceType.name());
        return put(endpoint, updateDto, new ParameterizedTypeReference<>() {});
    }

    /**
     * Handles a generic request to find resources based on a set of filters.
     *
     * @param resourceType The type of resource to find.
     * @param params       An array of filter parameters, including a {@link SearchFiltersDTO},
     *                     page size (Integer), and next page token (String).
     * @return A {@link Mono} emitting the list of matching resources.
     */
    public Mono<Object> handleFind(ResourceType resourceType, Object... params) {
        ParsedParams p = ParsedParams.from(params);
        SearchFiltersDTO filters = Optional.ofNullable(p.filters()).orElse(new SearchFiltersDTO());
        int pageSize = Optional.ofNullable(p.pageSize()).orElse(10);
        String nextPage = p.nextPage();

        try {
            Map<String, Object> transformedFilters = transformFilterKeys(filters);

            if (!StringUtils.isNullOrEmpty(filters.getCount())) {
                try {
                    pageSize = Integer.parseInt(filters.getCount());
                } catch (NumberFormatException e) {
                    return Mono.error(new IllegalArgumentException("Invalid count value, must be a positive integer"));
                }
            }
            transformedFilters.put("_count", pageSize);

            String encodedFilters = URLEncoder.encode(objectMapper.writeValueAsString(transformedFilters), StandardCharsets.UTF_8);

            String url = String.format("%s/%s?filters=%s%s",
                    Constants.GET_PROFILES_URL,
                    resourceType.name(),
                    encodedFilters,
                    Optional.ofNullable(nextPage)
                            .filter(s -> !s.isEmpty())
                            .map(s -> "&nextPage=" + URLEncoder.encode(s, StandardCharsets.UTF_8))
                            .orElse("")
            );
            return get(url, new ParameterizedTypeReference<>() {});

        } catch (Exception e) {
            return Mono.error(EhrApiError.handleAndLogApiError(e));
        }
    }

    /**
     * Handles a generic request to delete a resource by its ID.
     *
     * @param relativePath The base path for the resource type (e.g., "patients").
     * @param id           The unique identifier of the resource to delete.
     * @return A {@link Mono} emitting the result of the deletion operation.
     */
    public Mono<Object> handleDelete(String relativePath, String id) {
        if (StringUtils.isNullOrEmpty(id)) {
            return Mono.error(new ValidationError("ID cannot be null or empty for deletion."));
        }
        String endpoint = String.format("/%s/%s", relativePath, id);
        return delete(endpoint, new ParameterizedTypeReference<>() {});
    }



    /**
     * Validates if a given value is a valid enum constant.
     *
     * @param value     The enum value to check.
     * @param enumClass The class of the enum type.
     * @param <T>       The type parameter for the enum class.
     * @return {@code true} if the value is a valid enum constant, {@code false} otherwise.
     */
    protected static <T extends Enum<T>> boolean isValidEnum(Enum<?> value, Class<T> enumClass) {
        if (value == null || enumClass == null) {
            return false;
        }
        for (T constant : enumClass.getEnumConstants()) {
            if (constant.name().equals(value.name())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validates if a given string value matches an enum constant, including aliases for Region.
     *
     * @param value The string value to validate.
     * @param enumClass The class of the enum.
     * @param <T> The enum type.
     * @return {@code true} if the value is a valid enum constant or alias, {@code false} otherwise.
     */
    public static <T extends Enum<T>> boolean isValidEnum(String value, Class<T> enumClass) {
        if (value == null || enumClass == null) {
            return false;
        }
        return Arrays.stream(enumClass.getEnumConstants())
                .anyMatch(enumConstant -> enumConstant.name().equalsIgnoreCase(value) ||
                        (enumConstant instanceof Region region &&
                                Arrays.stream(region.getAliases()).anyMatch(alias -> alias.equalsIgnoreCase(value))));
    }


    /**
     * Transforms search filters from a DTO into a Map suitable for an API request.
     *
     * @param filters The DTO containing search filter criteria.
     * @return A map with API-compliant filter keys and values.
     */
    public Map<String, Object> transformFilterKeys(SearchFiltersDTO filters) {
        Map<String, Object> updatedFilters = new HashMap<>();

        if (!StringUtils.isNullOrEmpty(filters.getFirstName())) updatedFilters.put("name", filters.getFirstName());
        if (!StringUtils.isNullOrEmpty(filters.getLastName())) updatedFilters.put("family", filters.getLastName());
        if (!StringUtils.isNullOrEmpty(filters.getBirthDate())) updatedFilters.put("birthdate", filters.getBirthDate());
        if (filters.getGender() != null) updatedFilters.put("gender", filters.getGender().getValue());
        if (!StringUtils.isNullOrEmpty(filters.getPhone())) updatedFilters.put("phone", filters.getPhone());
        if (filters.getState() != null) updatedFilters.put("address-state", filters.getState());
        if (!StringUtils.isNullOrEmpty(filters.getPincode())) updatedFilters.put("address-postalcode", filters.getPincode());
        if (!StringUtils.isNullOrEmpty(filters.getCount())) updatedFilters.put("_count", filters.getCount());
        if (!StringUtils.isNullOrEmpty(filters.getEmailId())) updatedFilters.put("email", filters.getEmailId());
        if (!StringUtils.isNullOrEmpty(filters.getOrganizationId())) updatedFilters.put("identifier", filters.getOrganizationId());
        if (!StringUtils.isNullOrEmpty(filters.getRegistrationId())) updatedFilters.put("identifier", filters.getRegistrationId());
        if (!StringUtils.isNullOrEmpty(filters.getIdentifier())) updatedFilters.put("identifier", filters.getIdentifier());

        handleDateFilters(filters, updatedFilters);

        return updatedFilters;
    }

    private void handleDateFilters(SearchFiltersDTO filters, Map<String, Object> updatedFilters) {
        if (!StringUtils.isNullOrEmpty(filters.getFromDate())) {
            LocalDate fromDate = LocalDate.parse(filters.getFromDate());
            if (fromDate.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("fromDate cannot be in the future");
            }
            updatedFilters.put("_lastUpdated", Collections.singletonList("ge" + fromDate));
        }

        if (!StringUtils.isNullOrEmpty(filters.getToDate())) {
            LocalDate toDate = LocalDate.parse(filters.getToDate());
            if (toDate.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("toDate cannot be in the future");
            }
            if (!StringUtils.isNullOrEmpty(filters.getFromDate())) {
                LocalDate fromDate = LocalDate.parse(filters.getFromDate());
                if (toDate.isBefore(fromDate)) {
                    throw new IllegalArgumentException("toDate cannot be before fromDate");
                }
            }

            List<String> lastUpdatedList = (List<String>) updatedFilters.getOrDefault("_lastUpdated", new ArrayList<>());
            lastUpdatedList.add("le" + toDate);
            updatedFilters.put("_lastUpdated", lastUpdatedList);
        }
    }

    /**
     * Serializes a given model object into a Map representation.
     *
     * @param model The object to serialize.
     * @return A map representing the serialized object.
     * @throws RuntimeException if serialization fails.
     */
    public Map<String, Object> serializeModel(Object model) {
        try {
            return objectMapper.convertValue(model, new TypeReference<>() {});
        } catch (Exception e) {
            LogUtil.logger.error("Error serializing model: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to serialize model", e);
        }
    }
}
