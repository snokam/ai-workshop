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

@Configuration
@EnableConfigurationProperties(VertexAiProperties.class)
@ConditionalOnProperty(name = "aiworkshop.model.provider", havingValue = "vertex", matchIfMissing = true)
public class VertexAiConfig {
    @Bean(destroyMethod = "close")
    @Primary
    ChatModel chatModel(VertexAiProperties properties) {
        // TODO — task 1, part 1. Build the model.
        //
        // VertexAiGeminiChatModel.builder() is the builder. Every value it needs is already bound in
        // VertexAiProperties, which is a record beside this file:
        //
        //   .project(properties.projectId())        which project to bill and authorise against
        //   .location(properties.location())        europe-west4 unless something says otherwise
        //   .modelName(properties.modelName())      gemini-2.5-flash by default
        //   .temperature(properties.temperature())  .maxOutputTokens(properties.maxOutputTokens())
        //   .maxRetries(properties.maxRetries())    .logRequests(...) .logResponses(...)
        //
        // Use projectId() rather than project(): it falls back to the credentials already on the machine
        // when GOOGLE_CLOUD_PROJECT is not set, which is why nothing has to be exported.
        //
        // Nothing else in the workshop works until this returns a model.

        return UnfinishedTasks.notWrittenYet(ChatModel.class, WorkshopTask.FIRST_AGENT);
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
        };
    }
}
