package carestack.base.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * A dedicated configuration class to load the SDK's embedded properties file.
 * By isolating this, we ensure the properties are loaded into the Environment
 * before other components that depend on them are initialized, resolving potential
 * loading order issues.
 */
@Configuration
@PropertySource(value = "classpath:sdk-embedded.properties", ignoreResourceNotFound = true)
public class EmbeddedPropertiesConfiguration {
}