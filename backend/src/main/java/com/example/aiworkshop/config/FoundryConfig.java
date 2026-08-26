package com.example.aiworkshop.config;

import com.example.aiworkshop.tasks.task_1_first_agent.agent.Models;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import org.springframework.context.annotation.Primary;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(FoundryProperties.class)
@ConditionalOnProperty(name = "aiworkshop.model.provider", havingValue = "foundry")
public class FoundryConfig {
    private static final Logger log = LoggerFactory.getLogger(FoundryConfig.class);

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
     * Foundry has one deployment, so there are no tiers to choose between.
     *
     * <p>The name is ignored and the fact is logged rather than hidden. Task 5 then measures no
     * difference between its two choices, which is a true answer about this deployment — not a bug,
     * and better than silently serving a Gemini model to somebody running on Foundry.
     */
    @Bean
    Models models(FoundryProperties properties) {
        return new Models() {
            @Override
            public ChatModel named(String modelName) {
                log.info("Foundry has one deployment ({}), so '{}' is ignored", properties.deploymentName(), modelName);
                return OpenAiChatModel.builder()
                        .baseUrl(properties.endpoint())
                        .apiKey(properties.apiKey())
                        .modelName(properties.deploymentName())
                        .maxCompletionTokens(properties.maxCompletionTokens())
                        .timeout(properties.timeout())
                        .maxRetries(properties.maxRetries())
                        .build();
            }

            @Override
            public StreamingChatModel streamingNamed(String modelName) {
                log.info("Foundry has one deployment ({}), so '{}' is ignored", properties.deploymentName(), modelName);
                return OpenAiStreamingChatModel.builder()
                        .baseUrl(properties.endpoint())
                        .apiKey(properties.apiKey())
                        .modelName(properties.deploymentName())
                        .timeout(properties.timeout())
                        .logRequests(properties.logRequests())
                        .logResponses(properties.logResponses())
                        .build();
            }
        };
    }
}
