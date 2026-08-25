package com.example.aiworkshop.tasks.task_1_first_agent.agent;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.vertexai.gemini.VertexAiGeminiChatModel;
import dev.langchain4j.model.vertexai.gemini.VertexAiGeminiStreamingChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.beans.factory.annotation.Qualifier;

@Configuration
@EnableConfigurationProperties(VertexAiProperties.class)
@ConditionalOnProperty(name = "aiworkshop.model.provider", havingValue = "vertex", matchIfMissing = true)
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
     * The cheaper model, answering a token at a time. Given — task 7 uses it.
     *
     * <p>It is a separate bean rather than a mode on the one above, and that is the shape of the API
     * rather than a choice made here: an agent method returning a record needs the whole answer before
     * it can be a record, and an agent method returning a {@code TokenStream} never has a whole answer
     * to give back. The two cannot be the same call.
     *
     * <p>Built on the cheaper model on purpose, and it is the difference between streaming working and
     * streaming being a decoration. A reasoning model thinks before it writes, so it emits nothing for
     * seconds and then the whole answer at once — measured here, gemini-2.5-flash gave its first token
     * after 4.72s in a single chunk. The smaller model started in 0.44s across four. Streaming is only
     * worth wiring up on a model that does not stop to think first, and putting words on a decision
     * somebody else made is exactly the easy job task 5 argues belongs on the cheaper one.
     */
    @Bean(destroyMethod = "close")
    StreamingChatModel streamingChatModel(VertexAiProperties properties) {
        return VertexAiGeminiStreamingChatModel.builder()
                .project(properties.projectId())
                .location(properties.location())
                .modelName(properties.cheaperModelName())
                .temperature(properties.temperature())
                .maxOutputTokens(properties.maxOutputTokens())
                .build();
    }
}
