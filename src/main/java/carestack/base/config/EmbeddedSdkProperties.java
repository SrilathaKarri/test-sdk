package carestack.base.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for SDK embedded values.
 * These values are set during build time from GitHub secrets.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties(prefix = "sdk.embedded")
public class EmbeddedSdkProperties {

    private String baseUrl;
    private String certificatePem;
    private String abhaCertificatePem;
    private String facilityHpridAuth;
    private String googleApiKey;
}