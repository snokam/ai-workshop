package com.example.aiworkshop.tasks.task_1_first_agent.agent;

import com.example.aiworkshop.workshop.UnfinishedTasks;
import com.example.aiworkshop.workshop.WorkshopTask;
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
        // TODO — task 1, part 1. Build the model.
        //
        // Foundry speaks the OpenAI API, so the builder is OpenAiChatModel.builder() rather than
        // anything Azure-branded. Every value it needs is already bound in FoundryProperties, which
        // is a record beside this file:
        //
        //   .baseUrl(properties.endpoint())          the /openai/v1 endpoint on the resource
        //   .apiKey(properties.apiKey())             from AZURE_OPENAI_API_KEY
        //   .modelName(properties.deploymentName())  the deployment, not the model family
        //   .maxCompletionTokens(properties.maxCompletionTokens())
        //   .timeout(properties.timeout())           .maxRetries(properties.maxRetries())
        //   .logRequests(...)                        .logResponses(...)
        //
        // modelName is the deployment name, which is the one that trips people up: on Foundry you
        // deploy a model under a name of your choosing, and that name is what the API wants. Ours
        // happen to match the model they serve, which hides the distinction until it bites.
        //
        // Leave temperature alone unless properties.temperature() is set — a reasoning model
        // rejects the request outright if it is given one, so it is null by default:
        //
        //   if (properties.temperature() != null) { builder.temperature(properties.temperature()); }
        //
        // Nothing else in the workshop works until this returns a model.

        return UnfinishedTasks.notWrittenYet(ChatModel.class, WorkshopTask.FIRST_AGENT);
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
