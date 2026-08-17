package com.example.aiworkshop.fraud;

import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wiring for the fraud checks.
 *
 * <p>The checks themselves need none — each is a {@code @Component} and {@link FraudScreener} is
 * handed every one that exists, so a new check is a new class and nothing else. This is here for
 * the one collaborator that comes from outside the process.
 */
@Configuration
@EnableConfigurationProperties(FraudProperties.class)
class FraudConfig {

    /**
     * The reverse image lookup when no provider is configured — the default.
     *
     * <p>It answers {@link Optional#empty()}, which the package reads as "not answered" rather than
     * "nothing found". So out of the box the application runs, uploads work, every other check
     * still reports, and no Document acquires a finding that a switched-off service cannot support.
     *
     * <p>A stub returning invented matches would be worse than this. It would demo beautifully once
     * and then leave a room of people believing the check works.
     */
    @Bean
    @ConditionalOnMissingBean(ReverseImageLookup.class)
    ReverseImageLookup reverseImageLookupNotConfigured() {
        return (image, mimeType) -> Optional.empty();
    }
}
