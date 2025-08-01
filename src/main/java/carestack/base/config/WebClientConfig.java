package carestack.base.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import carestack.base.errors.EhrApiError;

import static carestack.base.errors.EhrApiError.mapHttpStatusToErrorType;

/**
 * Configuration class for setting up {@link WebClient} to interact with external APIs.
 * <p>
 * This class initializes a {@link WebClient} bean with common headers and error handling mechanisms.
 * It reads configuration properties like API key, base URL, and optional HPRID authentication from
 * the application properties.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 *   <li><strong>Base URL Configuration:</strong> Sets the API base URL for all outgoing requests.</li>
 *   <li><strong>Authorization Header:</strong> Adds an Authorization header using the provided API key.</li>
 *   <li><strong>Optional HPRID Auth:</strong> Adds a header if HPRID auth is configured.</li>
 *   <li><strong>Error Handling:</strong> Transforms HTTP error responses into {@link EhrApiError} for better error management.</li>
 * </ul>
 */
@Configuration
public class WebClientConfig {

    @Autowired
    private EmbeddedSdkProperties embeddedProperties;

    // User configurable values (required)
    @Value("${api.key:}")
    private String userApiKey;

    // Optional user overrides (fallback to embedded values if not provided)
    @Value("${api.url:#{null}}")
    private String userApiUrl;

    @Value("${facility.hpridAuth:#{null}}")
    private String userHpridAuth;

    @Value("${google.api.key:#{null}}")
    private String userGoogleApiKey;

    /**
     * Creates a {@link WebClient} bean with base URL, common headers, and error handling filters.
     * <p>
     * This client automatically includes authorization and content-type headers for each request
     * and handles API errors gracefully by mapping them to custom error types.
     * </p>
     *
     * @param builder the {@link WebClient.Builder} instance for building the client
     * @return a configured {@link WebClient} instance
     */
    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        String effectiveApiUrl = determineApiUrl();

        WebClient.Builder webClientBuilder = builder;
        if (effectiveApiUrl != null && !effectiveApiUrl.trim().isEmpty()) {
            webClientBuilder = webClientBuilder.baseUrl(effectiveApiUrl);
        }

        return webClientBuilder
                .filters(exchangeFilterFunctions -> {
                    exchangeFilterFunctions.add(addCommonHeaders());
                    exchangeFilterFunctions.add(handleErrors());
                })
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(20 * 1024 * 1024))
                .build();
    }

    private String determineApiUrl() {
        // User configuration takes precedence, fallback to embedded
        return (userApiUrl != null && !userApiUrl.trim().isEmpty())
                ? userApiUrl
                : embeddedProperties.getBaseUrl();
    }
    /**
     * Adds common headers (e.g., Authorization, Content-Type) to all outgoing API requests.
     * <p>
     * If the optional {@code hpridAuth} value is provided, an additional header "x-hprid-auth" is added.
     * </p>
     *
     * @return an {@link ExchangeFilterFunction} that modifies request headers
     */
    private ExchangeFilterFunction addCommonHeaders() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            ClientRequest.Builder requestBuilder = ClientRequest.from(clientRequest)
                    .header("Content-Type", "application/json");

            // API Key (user must provide this)
            if (userApiKey != null && !userApiKey.trim().isEmpty()) {
                if (userApiKey.toLowerCase().startsWith("bearer ")) {
                    requestBuilder.header("Authorization", userApiKey);
                } else {
                    requestBuilder.header("Authorization", "Bearer " + userApiKey);
                }
            }

            // HPRID Auth with fallback
            String effectiveHpridAuth = (userHpridAuth != null && !userHpridAuth.trim().isEmpty())
                    ? userHpridAuth
                    : embeddedProperties.getFacilityHpridAuth();

            if (effectiveHpridAuth != null && !effectiveHpridAuth.trim().isEmpty()) {
                requestBuilder.header("x-hprid-auth", effectiveHpridAuth);
            }

            return Mono.just(requestBuilder.build());
        });
    }

    /**
     * Handles API errors by inspecting the response status code and mapping error responses to {@link EhrApiError}.
     * <p>
     * This helps in transforming HTTP errors into a consistent error format for easier debugging and logging.
     * </p>
     *
     * @return an {@link ExchangeFilterFunction} that handles API response errors
     */
    private ExchangeFilterFunction handleErrors() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (clientResponse.statusCode().isError()) {
                return clientResponse.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(
                                new EhrApiError("Error: " + errorBody,
                                        mapHttpStatusToErrorType(clientResponse.statusCode()))
                        ));
            }
            return Mono.just(clientResponse);
        });
    }
}