package com.example.aiworkshop.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuration for a model deployment on Azure AI Foundry.
 *
 * <p>Note {@code deploymentName} is the name <em>you</em> gave the deployment in the Foundry portal,
 * not the underlying model name — they are often different.
 */
@ConfigurationProperties(prefix = "foundry")
public record FoundryProperties(
        String endpoint,
        String apiKey,
        String deploymentName,
        @DefaultValue("0.2") Double temperature,
        @DefaultValue("1024") Integer maxCompletionTokens,
        @DefaultValue("60s") Duration timeout,
        @DefaultValue("3") Integer maxRetries,
        @DefaultValue("false") Boolean logRequestsAndResponses) {}
