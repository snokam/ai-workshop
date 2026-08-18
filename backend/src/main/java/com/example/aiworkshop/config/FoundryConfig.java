package com.example.aiworkshop.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
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
}
