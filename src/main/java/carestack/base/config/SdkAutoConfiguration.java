package carestack.base.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


@Configuration
@ComponentScan(basePackages = {
        "carestack"
})
@EnableConfigurationProperties(EmbeddedSdkProperties.class)
@Import(EmbeddedPropertiesConfiguration.class)
public class SdkAutoConfiguration {

}