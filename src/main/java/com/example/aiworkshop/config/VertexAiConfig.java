package com.example.aiworkshop.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Active when {@code aiworkshop.model.provider=vertex} (the default). */
@Configuration
@EnableConfigurationProperties(VertexAiProperties.class)
@ConditionalOnProperty(name = "aiworkshop.model.provider", havingValue = "vertex", matchIfMissing = true)
class VertexAiConfig {

    /**
     * Exposed as {@link ChatModel} so nothing downstream binds to a provider. The model implements
     * {@link java.io.Closeable}; Spring calls {@code close()} on shutdown.
     */
    @Bean(destroyMethod = "close")
    ChatModel chatModel(VertexAiProperties properties) {
        return VertexAiGeminiChatModel.builder()
                .project(properties.project())
                .location(properties.location())
                .modelName(properties.modelName())
                .temperature(properties.temperature())
                .maxOutputTokens(properties.maxOutputTokens())
                .maxRetries(properties.maxRetries())
                .logRequests(properties.logRequests())
                .logResponses(properties.logResponses())
                .build();
    }
}
