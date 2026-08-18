package com.example.aiworkshop.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "foundry")
public record FoundryProperties(
        String endpoint,
        String apiKey,
        String deploymentName,
        Double temperature,
        @DefaultValue("1024") Integer maxCompletionTokens,
        @DefaultValue("60s") Duration timeout,
        @DefaultValue("3") Integer maxRetries,
        @DefaultValue("false") Boolean logRequests,
        @DefaultValue("false") Boolean logResponses) {}
