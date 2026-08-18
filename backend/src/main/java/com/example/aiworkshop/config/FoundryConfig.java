package com.example.aiworkshop.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Active when {@code aiworkshop.model.provider=foundry}.
 *
 * <p>Talks to Foundry's OpenAI-compatible surface ({@code https://<resource>.services.ai.azure.com/openai/v1})
 * with the OpenAI client, so the endpoint is used verbatim as the base URL. The Azure-SDK client
 * ({@code AzureOpenAiChatModel}) is the wrong fit here: it appends
 * {@code /openai/deployments/<name>/chat/completions} to whatever endpoint it is given, which
 * double-prefixes a URL that already ends in {@code /openai/v1}. Use that one only with a classic
 * {@code https://<resource>.openai.azure.com/} endpoint.
 */
@Configuration
@EnableConfigurationProperties(FoundryProperties.class)
@ConditionalOnProperty(name = "aiworkshop.model.provider", havingValue = "foundry")
class FoundryConfig {

    /** Same {@link ChatModel} contract as the Vertex bean, so nothing downstream changes. */
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
