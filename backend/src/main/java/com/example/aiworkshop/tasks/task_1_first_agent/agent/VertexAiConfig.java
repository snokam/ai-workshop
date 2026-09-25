package com.example.aiworkshop.tasks.task_1_first_agent.agent;

import com.example.aiworkshop.workshop.UnfinishedTasks;
import com.example.aiworkshop.workshop.WorkshopTask;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.vertexai.gemini.VertexAiGeminiChatModel;
import dev.langchain4j.model.vertexai.gemini.VertexAiGeminiStreamingChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * The other provider, given and already written.
 *
 * <p>The workshop runs on Azure AI Foundry, so {@link FoundryConfig} is the one task 1 asks you to
 * fill in and this is here as the alternative — set {@code aiworkshop.model.provider=vertex} and
 * everything downstream works unchanged, which is most of the point of building agents against an
 * interface rather than against a vendor.
 */
@Configuration
@EnableConfigurationProperties(VertexAiProperties.class)
@ConditionalOnProperty(name = "aiworkshop.model.provider", havingValue = "vertex")
public class VertexAiConfig {
    @Bean(destroyMethod = "close")
    @Primary
    ChatModel chatModel(VertexAiProperties properties) {
        return VertexAiGeminiChatModel.builder()
                .project(properties.projectId())
                .location(properties.location())
                .modelName(properties.modelName())
                .temperature(properties.temperature())
                .maxOutputTokens(properties.maxOutputTokens())
                .maxRetries(properties.maxRetries())
                .logRequests(properties.logRequests())
                .logResponses(properties.logResponses())
                .build();
    }

    /**
     * Builds a model by name for the two tasks that need one other than the default.
     *
     * <p>Given, and deliberately thin: it is the same builder as above with the name passed in. Task
     * 5 uses it to try one model against another, and task 7 to get a streaming one.
     */
    @Bean
    Models models(VertexAiProperties properties) {
        return new Models() {
            @Override
            public ChatModel named(String modelName) {
                return VertexAiGeminiChatModel.builder()
                        .project(properties.projectId())
                        .location(properties.location())
                        .modelName(modelName)
                        .temperature(properties.temperature())
                        .maxOutputTokens(properties.maxOutputTokens())
                        .maxRetries(properties.maxRetries())
                        .build();
            }

            @Override
            public StreamingChatModel streamingNamed(String modelName) {
                return VertexAiGeminiStreamingChatModel.builder()
                        .project(properties.projectId())
                        .location(properties.location())
                        .modelName(modelName)
                        .temperature(properties.temperature())
                        .maxOutputTokens(properties.maxOutputTokens())
                        .build();
            }

            @Override
            public String fastest() {
                return properties.cheaperModelName();
            }
        };
    }
}
