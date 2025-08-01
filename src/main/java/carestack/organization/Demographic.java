package carestack.organization;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import carestack.base.Base;
import carestack.organization.dto.ResponseDTOs;

import java.util.Map;

/**
 * Service class for interacting with the Demographic API to fetch and post-data.
 * <p>
 * This class provides methods for making GET and POST requests to the API by leveraging the
 * centralized client configuration and error handling from its {@link Base} parent class.
 * </p>
 *
 * @see WebClient
 * @see Base
 */
@Service
public class Demographic extends Base {

    /**
     * Constructor for the Demographic service class.
     *
     * @param objectMapper The {@link ObjectMapper} for converting JSON to Java objects.
     * @param webClient The centrally configured {@link WebClient} for making asynchronous HTTP requests.
     *                  This is passed to the parent constructor to initialize the base service.
     */
    protected Demographic(ObjectMapper objectMapper, WebClient webClient) {
        super(objectMapper, webClient);
    }

    /**
     * Fetches data from the specified API endpoint using a GET request.
     * <p>
     * This method now delegates to the centralized `get` method in the {@link Base} class,
     * which handles URL construction, headers, and error handling automatically.
     * </p>
     *
     * @param <T> The expected type of the response body.
     * @param endpoint The relative API endpoint to send the GET request to (e.g., "/users/1").
     * @param typeReference The type reference for the expected response body.
     * @return A {@link Mono} containing the response body mapped to the specified type.
     */
    public <T> Mono<T> fetchData(String endpoint, ParameterizedTypeReference<T> typeReference) {
        return super.get(endpoint, typeReference);
    }

    /**
     * Makes a POST request to the specified endpoint with the provided request body.
     * <p>
     * This method now delegates to the centralized `post` method in the {@link Base} class,
     * which handles URL construction, headers, and error handling automatically.
     * </p>
     *
     * @param <T> The type of the request body.
     * @param <R> The expected type of the response body.
     * @param endpoint The relative API endpoint to send the POST request to.
     * @param requestBody The request body to send in the POST request.
     * @param responseType The type reference for the expected response body.
     * @return A {@link Mono} containing the response body mapped to the specified type.
     */
    <T, R> Mono<R> makePostRequestForData(String endpoint, T requestBody, ParameterizedTypeReference<R> responseType) {
        return super.post(endpoint, requestBody, responseType);
    }

    /**
     * Fetches code-value pairs from a specified URL using a GET request.
     * <p>
     * This convenience method fetches code-value pairs from the specified URL and returns the response as a
     * {@link ResponseDTOs.TypeDataResponseDTO} object.
     * </p>
     *
     * @param url The URL to fetch the code-value pairs from.
     * @return A {@link Mono} containing the response as a {@link ResponseDTOs.TypeDataResponseDTO}.
     */
    public Mono<ResponseDTOs.TypeDataResponseDTO> fetchCodeValuePairs(String url) {
        return fetchData(url, new ParameterizedTypeReference<>() {});
    }

    /**
     * Fetches code-value pairs from a specified URL using a POST request.
     * <p>
     * This method sends a POST request to the specified URL with a request body containing key-value pairs and
     * returns the response as a {@link ResponseDTOs.TypeDataResponseDTO}.
     * </p>
     *
     * @param url The URL to fetch the code-value pairs from.
     * @param requestBody The request body to send in the POST request.
     * @return A {@link Mono} containing the response as a {@link ResponseDTOs.TypeDataResponseDTO}.
     */
    public Mono<ResponseDTOs.TypeDataResponseDTO> fetchCodeValuePairs(String url, Map<String, String> requestBody) {
        return makePostRequestForData(url, requestBody, new ParameterizedTypeReference<>() {});
    }
}