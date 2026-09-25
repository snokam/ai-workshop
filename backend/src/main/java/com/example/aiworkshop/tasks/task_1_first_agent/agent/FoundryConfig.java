package com.example.aiworkshop.tasks.task_1_first_agent.agent;

import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.anthropic.AnthropicStreamingChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import org.springframework.context.annotation.Primary;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(FoundryProperties.class)
@ConditionalOnProperty(name = "aiworkshop.model.provider", havingValue = "foundry", matchIfMissing = true)
public class FoundryConfig {
    @Bean
    @Primary
    ChatModel chatModel(FoundryProperties properties) {
        OpenAiChatModel.OpenAiChatModelBuilder builder = OpenAiChatModel.builder()
                .baseUrl(properties.endpoint())
                .apiKey(properties.apiKey())
                // The deployment name, not the model family. On Foundry you deploy a model under a
                // name of your choosing and that name is what the API wants; ours happen to match
                // the model they serve, which hides the distinction until it bites.
                .modelName(properties.deploymentName())
                .maxCompletionTokens(properties.maxCompletionTokens())
                .timeout(properties.timeout())
                .maxRetries(properties.maxRetries())
                .logRequests(properties.logRequests())
                .logResponses(properties.logResponses());

        // Only when one is configured. A reasoning model rejects the request outright if it is
        // given a temperature, so the default is to say nothing about it at all.
        if (properties.temperature() != null) {
            builder.temperature(properties.temperature());
        }
        return builder.build();
    }

    /**
     * Builds a model by name, for the two tasks that need one other than the default.
     *
     * <p>Several models are deployed here, so task 5's choice is a real one. A name that is not one
     * of them is an error rather than a near-enough substitute — see {@link FoundryDeployments}.
     *
     * <p>And one of them is not served by the endpoint the rest of them are. Foundry puts two APIs on
     * the one resource: {@code /openai/v1}, which the GPT deployments answer on, and
     * {@code /anthropic/v1}, which is where the Claude deployment lives. Asking for
     * {@code claude-sonnet-4-6} over the OpenAI path returns
     * {@code 404 api_not_supported} — the deployment exists, the protocol does not. So the name
     * decides the client as well as the model, and everything above this line is unaware of it.
     */
    @Bean
    Models models(FoundryProperties properties) {
        return new Models() {
            @Override
            public ChatModel named(String modelName) {
                String name = FoundryDeployments.require(modelName);
                if (FoundryDeployments.speaksAnthropic(name)) {
                    return AnthropicChatModel.builder()
                            .baseUrl(properties.anthropicEndpoint())
                            .apiKey(properties.apiKey())
                            .modelName(name)
                            .maxTokens(properties.maxCompletionTokens())
                            .timeout(properties.timeout())
                            .maxRetries(properties.maxRetries())
                            .logRequests(properties.logRequests())
                            .logResponses(properties.logResponses())
                            .build();
                }
                return OpenAiChatModel.builder()
                        .baseUrl(properties.endpoint())
                        .apiKey(properties.apiKey())
                        .modelName(name)
                        .maxCompletionTokens(properties.maxCompletionTokens())
                        .timeout(properties.timeout())
                        .maxRetries(properties.maxRetries())
                        .build();
            }

            @Override
            public StreamingChatModel streamingNamed(String modelName) {
                String name = FoundryDeployments.require(modelName);
                if (FoundryDeployments.speaksAnthropic(name)) {
                    return AnthropicStreamingChatModel.builder()
                            .baseUrl(properties.anthropicEndpoint())
                            .apiKey(properties.apiKey())
                            .modelName(name)
                            .maxTokens(properties.maxCompletionTokens())
                            .timeout(properties.timeout())
                            .logRequests(properties.logRequests())
                            .logResponses(properties.logResponses())
                            .build();
                }
                return OpenAiStreamingChatModel.builder()
                        .baseUrl(properties.endpoint())
                        .apiKey(properties.apiKey())
                        .modelName(name)
                        .timeout(properties.timeout())
                        .logRequests(properties.logRequests())
                        .logResponses(properties.logResponses())
                        .build();
            }

            @Override
            public String fastest() {
                return FoundryDeployments.require(properties.cheaperDeploymentName());
            }
        };
    }
}
