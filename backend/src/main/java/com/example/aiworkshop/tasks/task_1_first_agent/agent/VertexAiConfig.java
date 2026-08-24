package com.example.aiworkshop.tasks.task_1_first_agent.agent;

import com.example.aiworkshop.workshop.UnfinishedTasks;
import com.example.aiworkshop.workshop.WorkshopTask;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
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
     * A smaller, cheaper, faster model. Given — task 5 decides what to do with it.
     *
     * <p>Published here beside the good one because the connection belongs to task 1 wherever it is
     * used. Nothing is on it until task 5 puts something there.
     */
    @Bean(destroyMethod = "close")
    @Qualifier("cheaper")
    ChatModel cheaperChatModel(VertexAiProperties properties) {
        return VertexAiGeminiChatModel.builder()
                .project(properties.projectId())
                .location(properties.location())
                .modelName(properties.cheaperModelName())
                .temperature(properties.temperature())
                .maxOutputTokens(properties.maxOutputTokens())
                .maxRetries(properties.maxRetries())
                .build();
    }

    /**
     * The same model, answering a token at a time. Given — task 7 uses it.
     *
     * <p>It is a separate bean rather than a mode on the one above, and that is the shape of the API
     * rather than a choice made here: an agent method returning a record needs the whole answer before
     * it can be a record, and an agent method returning a {@code TokenStream} never has a whole answer
     * to give back. The two cannot be the same call.
     */
    @Bean(destroyMethod = "close")
    StreamingChatModel streamingChatModel(VertexAiProperties properties) {
        return VertexAiGeminiStreamingChatModel.builder()
                .project(properties.projectId())
                .location(properties.location())
                .modelName(properties.modelName())
                .temperature(properties.temperature())
                .maxOutputTokens(properties.maxOutputTokens())
                .build();
    }
}
