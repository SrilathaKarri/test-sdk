package carestack.base.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuration class for setting up caching using Caffeine in a Spring Boot application.
 * <p>
 * This class enables caching support through the {@link EnableCaching} annotation and defines a
 * {@link CacheManager} bean that manages caches for different entities such as organizations,
 * practitioners, and patients. It leverages Caffeine, a high-performance caching library for Java,
 * to handle in-memory caching with configurable expiration and size limits.
 * </p>
 *
 * <p><strong>Cache Settings:</strong></p>
 * <ul>
 *   <li><strong>Cache Names:</strong> "organizationCache", "practitionerCache", "patientCache"</li>
 *   <li><strong>Expiration:</strong> Entries expire 10 minutes after being written.</li>
 *   <li><strong>Maximum Size:</strong> Cache can hold up to 1000 entries before evicting the least recently used items.</li>
 *   <li><strong>Async Cache Mode:</strong> Enabled for non-blocking cache operations.</li>
 * </ul>
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configures the {@link CacheManager} bean using Caffeine for in-memory caching.
     * <p>
     * The cache manager is set up to support asynchronous cache operations, with entries expiring
     * 10 minutes after being written. The maximum size of the cache is limited to 1000 entries,
     * beyond which the least recently used (LRU) entries will be evicted.
     * </p>
     *
     * @return a {@link CacheManager} instance configured with Caffeine
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("organizationCache", "practitionerCache", "patientCache");
        cacheManager.setAsyncCacheMode(true);
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES) // Entries expire after 10 minutes of being written
                .maximumSize(1000));                    // Maximum 1000 entries before eviction
        return cacheManager;
    }
}
