package carestack.base.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan(basePackages = {
        "carestack"
})
@EnableConfigurationProperties(EmbeddedSdkProperties.class)
@PropertySource(value = "classpath:sdk-embedded.properties", ignoreResourceNotFound = true)
public class SdkAutoConfiguration {

}