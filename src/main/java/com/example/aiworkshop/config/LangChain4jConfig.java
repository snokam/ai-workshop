package com.example.aiworkshop.config;

import com.example.aiworkshop.ai.Assistant;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.vertexai.gemini.VertexAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(VertexAiProperties.class)
class LangChain4jConfig {

    /**
     * Exposed as {@link ChatModel} rather than the concrete type so swapping providers later is a
     * change to this method only. The model implements {@link java.io.Closeable}; Spring calls
     * {@code close()} on shutdown.
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

    @Bean
    Assistant assistant(ChatModel chatModel) {
        return AiServices.create(Assistant.class, chatModel);
    }
}
