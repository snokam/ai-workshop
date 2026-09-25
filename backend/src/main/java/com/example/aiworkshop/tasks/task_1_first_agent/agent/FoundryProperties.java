package com.example.aiworkshop.tasks.task_1_first_agent.agent;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "foundry")
public record FoundryProperties(
        String endpoint,
        String anthropicEndpoint,
        String apiKey,
        String deploymentName,
        @DefaultValue("gpt-5.4-mini") String cheaperDeploymentName,
        Double temperature,
        @DefaultValue("1024") Integer maxCompletionTokens,
        @DefaultValue("60s") Duration timeout,
        @DefaultValue("3") Integer maxRetries,
        @DefaultValue("false") Boolean logRequests,
        @DefaultValue("false") Boolean logResponses) {}
