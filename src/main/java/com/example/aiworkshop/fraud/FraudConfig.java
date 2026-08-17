package com.example.aiworkshop.fraud;

import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(FraudProperties.class)
class FraudConfig {
    @Bean
    @ConditionalOnMissingBean(ReverseImageLookup.class)
    ReverseImageLookup reverseImageLookupNotConfigured() {
        return (image, mimeType) -> Optional.empty();
    }
}
