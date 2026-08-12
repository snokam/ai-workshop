package com.example.aiworkshop.config;

import dev.langchain4j.model.azure.AzureOpenAiChatModel;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Active when {@code aiworkshop.model.provider=foundry}. */
@Configuration
@EnableConfigurationProperties(FoundryProperties.class)
@ConditionalOnProperty(name = "aiworkshop.model.provider", havingValue = "foundry")
class FoundryConfig {

    /**
     * Same {@link ChatModel} contract as the Vertex bean, so {@code Assistant} and everything above
     * it are unchanged when you flip providers.
     *
     * <p>Uses API-key auth. For Entra ID instead, add the {@code com.azure:azure-identity} dependency
     * and swap {@code .apiKey(...)} for {@code .tokenCredential(new DefaultAzureCredentialBuilder().build())}.
     */
    @Bean
    ChatModel chatModel(FoundryProperties properties) {
        return AzureOpenAiChatModel.builder()
                .endpoint(properties.endpoint())
                .apiKey(properties.apiKey())
                .deploymentName(properties.deploymentName())
                .temperature(properties.temperature())
                .maxCompletionTokens(properties.maxCompletionTokens())
                .timeout(properties.timeout())
                .maxRetries(properties.maxRetries())
                .logRequestsAndResponses(properties.logRequestsAndResponses())
                .build();
    }
}
