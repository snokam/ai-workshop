package com.example.aiworkshop.tasks.task_1_first_agent.agent;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "vertex-ai")
public record VertexAiProperties(
        String project,
        String location,
        String modelName,
        @DefaultValue("0.2") Float temperature,
        @DefaultValue("1024") Integer maxOutputTokens,
        @DefaultValue("3") Integer maxRetries,
        @DefaultValue("false") Boolean logRequests,
        @DefaultValue("false") Boolean logResponses) {}
