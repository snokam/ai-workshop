package com.example.aiworkshop.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(FoundryProperties.class)
@ConditionalOnProperty(name = "aiworkshop.model.provider", havingValue = "foundry")
public class FoundryConfig {
    @Bean
    @Primary
    ChatModel chatModel(FoundryProperties properties) {
        OpenAiChatModel.OpenAiChatModelBuilder builder = OpenAiChatModel.builder()
                .baseUrl(properties.endpoint())
                .apiKey(properties.apiKey())
                .modelName(properties.deploymentName())
                .maxCompletionTokens(properties.maxCompletionTokens())
                .timeout(properties.timeout())
                .maxRetries(properties.maxRetries())
                .logRequests(properties.logRequests())
                .logResponses(properties.logResponses());

        if (properties.temperature() != null) {
            builder.temperature(properties.temperature());
        }
        return builder.build();
    }

    /**
     * A cheaper deployment, if one is configured. Falls back to the same one when it is not, so the
     * workshop runs either way — task 5 then measures no difference, which is its own answer.
     */
    @Bean
    @Qualifier("cheaper")
    ChatModel cheaperChatModel(FoundryProperties properties) {
        String deployment = properties.cheaperDeploymentName() == null || properties.cheaperDeploymentName().isBlank()
                ? properties.deploymentName()
                : properties.cheaperDeploymentName();
        return OpenAiChatModel.builder()
                .baseUrl(properties.endpoint())
                .apiKey(properties.apiKey())
                .modelName(deployment)
                .maxCompletionTokens(properties.maxCompletionTokens())
                .timeout(properties.timeout())
                .maxRetries(properties.maxRetries())
                .build();
    }

    /** The same deployment, answering a token at a time. Task 7 uses it. */
    @Bean
    StreamingChatModel streamingChatModel(FoundryProperties properties) {
        return OpenAiStreamingChatModel.builder()
                .baseUrl(properties.endpoint())
                .apiKey(properties.apiKey())
                .modelName(properties.deploymentName())
                .timeout(properties.timeout())
                .logRequests(properties.logRequests())
                .logResponses(properties.logResponses())
                .build();
    }
}
