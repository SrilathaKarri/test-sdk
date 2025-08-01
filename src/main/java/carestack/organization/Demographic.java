package carestack.organization;

import carestack.base.config.EmbeddedSdkProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import carestack.base.Base;
import carestack.base.errors.EhrApiError;
import carestack.organization.dto.ResponseDTOs;

import java.time.Duration;
import java.util.Map;

import static carestack.base.errors.EhrApiError.mapHttpStatusToErrorType;

/**
 * Service class for interacting with the Demographic API to fetch and post-data.
 * <p>
 * This class provides methods for making GET and POST requests to the API, with error handling and response mapping.
 * It utilizes {@link WebClient} from Spring WebFlux for making non-blocking HTTP requests.
 * </p>
 * <p>
 * The service is responsible for fetching data from the API and performing the required transformations
 * or processing of that data based on the provided request.
 * </p>
 *
 * @see WebClient
 */
@Service
public class Demographic extends Base {

    private final String apiKey;
    private final WebClient webClient;
    private final String baseUrl;

    @Autowired
    public Demographic(EmbeddedSdkProperties embeddedSdkProperties,
                       @Value("${api.url:}") String apiURL,
                       @Value("${api.key}") String apiKey,
                       WebClient webClient,
                       ObjectMapper objectMapper) {
        super(objectMapper, webClient);
        String apiURL1 = apiURL != null && !apiURL.trim().isEmpty() ? apiURL : embeddedSdkProperties.getBaseUrl();
        this.apiKey = apiKey;
        this.webClient = webClient;
        this.baseUrl = apiURL1;
    }
    /**
     * Fetches data from the specified API endpoint using a GET request.
     * <p>
     * This method sends an HTTP GET request to the specified API endpoint and returns a Mono containing the
     * response mapped to the provided type. It handles client and server errors gracefully and raises an
     * {@link EhrApiError} if any errors occur.
     * </p>
     *
     * @param <T> The expected type of the response body.
     * @param endpoint The API endpoint to send the GET request to.
     * @param typeReference The type reference for the expected response body.
     * @return A {@link Mono} containing the response body mapped to the specified type.
     * @throws EhrApiError If there is a client or server error in the request.
     */
    public <T> Mono<T> fetchData(String endpoint, ParameterizedTypeReference<T> typeReference) {
        return webClient.get()
                .uri(baseUrl + endpoint)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        response.bodyToMono(String.class)
                                .defaultIfEmpty("Client error occurred")
                                .flatMap(body -> Mono.error(new EhrApiError("Client error: " + body, mapHttpStatusToErrorType(response.statusCode()))))
                )
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        response.bodyToMono(String.class)
                                .defaultIfEmpty("Server error occurred")
                                .flatMap(body -> Mono.error(new EhrApiError("Server error: " + body, mapHttpStatusToErrorType(response.statusCode()))))
                )
                .bodyToMono(typeReference);
    }

    /**
     * Makes a POST request to the specified endpoint with the provided request body.
     * <p>
     * This method sends an HTTP POST request to the specified API endpoint with the given request body and
     * returns a Mono containing the response mapped to the specified type. It handles client and server errors
     * gracefully and raises an {@link EhrApiError} if any errors occur.
     * </p>
     *
     * @param <T> The type of the request body.
     * @param <R> The expected type of the response body.
     * @param endpoint The API endpoint to send the POST request to.
     * @param requestBody The request body to send in the POST request.
     * @param responseType The type reference for the expected response body.
     * @return A {@link Mono} containing the response body mapped to the specified type.
     * @throws EhrApiError If there is a client or server error in the request.
     */
    <T, R> Mono<R> makePostRequestForData(String endpoint, T requestBody, ParameterizedTypeReference<R> responseType) {
        return webClient.post()
                .uri(baseUrl + endpoint)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        response.bodyToMono(String.class)
                                .defaultIfEmpty("Client error occurred")
                                .flatMap(body -> Mono.error(new EhrApiError("Client error: " + body, mapHttpStatusToErrorType(response.statusCode()))))
                )
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        response.bodyToMono(String.class)
                                .defaultIfEmpty("Server error occurred")
                                .flatMap(body -> Mono.error(new EhrApiError("Server error: " + body, mapHttpStatusToErrorType(response.statusCode()))))
                )
                .bodyToMono(responseType)
                .timeout(Duration.ofSeconds(5));
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