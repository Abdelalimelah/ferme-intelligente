package ma.ferme.fermeintelligente.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Default (dev) cache — Caffeine in-process cache.
     * No external infrastructure needed.
     *
     * TTLs:
     *   capteurs        30 s  — live values change frequently
     *   alertes         15 s  — near-realtime
     *   alertes_count   15 s
     *   parcelles        2 min — changes rarely
     *   dashboard        1 min — aggregate stats
     */
    @Bean
    @Profile("!prod")
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager(
                "capteurs", "capteurs_parcelle",
                "alertes", "alertes_unread", "alertes_count",
                "parcelles", "dashboard"
        );
        manager.setCaffeine(
                Caffeine.newBuilder()
                        .expireAfterWrite(30, TimeUnit.SECONDS)
                        .maximumSize(1000)
                        .recordStats()
        );
        return manager;
    }
}
