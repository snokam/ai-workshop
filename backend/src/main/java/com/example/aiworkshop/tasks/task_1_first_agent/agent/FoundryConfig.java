package com.example.aiworkshop.tasks.task_1_first_agent.agent;

import com.example.aiworkshop.workshop.UnfinishedTasks;
import com.example.aiworkshop.workshop.WorkshopTask;
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
@ConditionalOnProperty(name = "aiworkshop.model.provider", havingValue = "foundry", matchIfMissing = true)
public class FoundryConfig {
    private static final Logger log = LoggerFactory.getLogger(FoundryConfig.class);

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
     * <p>Several models are deployed here, so task 5's choice is a real one — see {@link
     * FoundryDeployments} for the names it can be given.
     */
    @Bean
    Models models(FoundryProperties properties) {
        return new Models() {
            @Override
            public ChatModel named(String modelName) {
                return OpenAiChatModel.builder()
                        .baseUrl(properties.endpoint())
                        .apiKey(properties.apiKey())
                        .modelName(deploymentFor(modelName, properties))
                        .maxCompletionTokens(properties.maxCompletionTokens())
                        .timeout(properties.timeout())
                        .maxRetries(properties.maxRetries())
                        .build();
            }

            @Override
            public StreamingChatModel streamingNamed(String modelName) {
                return OpenAiStreamingChatModel.builder()
                        .baseUrl(properties.endpoint())
                        .apiKey(properties.apiKey())
                        .modelName(deploymentFor(modelName, properties))
                        .timeout(properties.timeout())
                        .logRequests(properties.logRequests())
                        .logResponses(properties.logResponses())
                        .build();
            }
        };
    }

    /**
     * Which deployment a requested name lands on, said out loud whenever it is not the one asked
     * for — because the cost logged beside the answer is priced against the name, not the
     * deployment.
     */
    private static String deploymentFor(String modelName, FoundryProperties properties) {
        String deployment = FoundryDeployments.resolve(modelName);
        if (deployment == null) {
            log.warn(
                    "'{}' is not deployed on Foundry, using {} instead — the cost logged for it will be wrong",
                    modelName,
                    properties.deploymentName());
            return properties.deploymentName();
        }
        if (!deployment.equals(modelName)) {
            log.warn(
                    "'{}' is a Vertex model, using {} instead — the cost logged for it will be wrong",
                    modelName,
                    deployment);
        }
        return deployment;
    }
}
