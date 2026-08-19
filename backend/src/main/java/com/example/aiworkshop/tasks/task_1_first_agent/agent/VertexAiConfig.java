package com.example.aiworkshop.tasks.task_1_first_agent.agent;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(VertexAiProperties.class)
@ConditionalOnProperty(name = "aiworkshop.model.provider", havingValue = "vertex", matchIfMissing = true)
public class VertexAiConfig {
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

        // ── To set this task again ────────────────────────────────────────────────────────
        // TODO — task 1, part 1. Build the model.
        //
        // This is the connection every agent in the workshop runs on: which provider, which model,
        // which credentials. VertexAiGeminiChatModel.builder() takes the values already
        // bound in VertexAiProperties — read that record to see what is
        // configurable, and application.properties to see where it comes from.
        //
        // Returning the stand-in is what no model looks like: the application starts, and anything
        // that needs a model says which file to open.
        // return UnfinishedTasks.notWrittenYet(ChatModel.class, WorkshopTask.FIRST_AGENT);
    }
}
