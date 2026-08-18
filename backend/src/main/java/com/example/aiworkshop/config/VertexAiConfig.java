package com.example.aiworkshop.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(VertexAiProperties.class)
@ConditionalOnProperty(name = "aiworkshop.model.provider", havingValue = "vertex", matchIfMissing = true)
class VertexAiConfig {
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
