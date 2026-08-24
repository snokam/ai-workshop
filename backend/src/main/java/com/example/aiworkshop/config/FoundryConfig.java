package com.example.aiworkshop.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
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
