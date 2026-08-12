package com.example.aiworkshop.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuration for the Gemini model hosted on Google Cloud Vertex AI.
 *
 * <p>Authentication uses Application Default Credentials — there is no API key. Locally that means
 * {@code gcloud auth application-default login}; on GCP it resolves the attached service account.
 */
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
