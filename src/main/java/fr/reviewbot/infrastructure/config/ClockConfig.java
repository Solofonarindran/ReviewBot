package fr.reviewbot.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ClockConfig {

    /** Horloge unique de l'application (remplaçable dans les tests). */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
