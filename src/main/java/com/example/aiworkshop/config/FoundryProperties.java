package com.example.aiworkshop.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuration for a model deployment on Azure AI Foundry, reached through its OpenAI-compatible
 * {@code /openai/v1} endpoint.
 *
 * <p>{@code deploymentName} is the name you gave the deployment in the Foundry portal, which is
 * usually not the underlying model name. It is sent as the {@code model} field.
 */
@ConfigurationProperties(prefix = "foundry")
public record FoundryProperties(
        String endpoint,
        String apiKey,
        String deploymentName,
        /* Optional on purpose: several newer models accept only the default and reject an explicit
         * temperature with a 400. Leave unset unless you know the deployment allows it. */
        Double temperature,
        @DefaultValue("1024") Integer maxCompletionTokens,
        @DefaultValue("60s") Duration timeout,
        @DefaultValue("3") Integer maxRetries,
        @DefaultValue("false") Boolean logRequests,
        @DefaultValue("false") Boolean logResponses) {}
