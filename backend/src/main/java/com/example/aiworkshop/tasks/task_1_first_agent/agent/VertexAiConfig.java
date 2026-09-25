package com.example.aiworkshop.tasks.task_1_first_agent.agent;

import com.example.aiworkshop.workshop.UnfinishedTasks;
import com.example.aiworkshop.workshop.WorkshopTask;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.vertexai.gemini.VertexAiGeminiChatModel;
import dev.langchain4j.model.vertexai.gemini.VertexAiGeminiStreamingChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * The other provider, given and already written.
 *
 * <p>The workshop runs on Azure AI Foundry, so {@link FoundryConfig} is the one task 1 asks you to
 * fill in and this is here as the alternative — set {@code aiworkshop.model.provider=vertex} and
 * everything downstream works unchanged, which is most of the point of building agents against an
 * interface rather than against a vendor.
 */
@Configuration
@EnableConfigurationProperties(VertexAiProperties.class)
@ConditionalOnProperty(name = "aiworkshop.model.provider", havingValue = "vertex")
public class VertexAiConfig {
    private static final Logger log = LoggerFactory.getLogger(VertexAiConfig.class);

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
     * The Foundry deployments, each pointed at the closest Gemini tier, so a solution written on
     * Foundry runs here without being rewritten. The mirror image of {@code FoundryDeployments}.
     */
    private static final java.util.Map<String, String> NEAREST_GEMINI = java.util.Map.of(
            "gpt-5.4-mini", "gemini-2.5-flash-lite",
            "o4-mini", "gemini-2.5-flash-lite",
            "gpt-4o", "gemini-2.5-flash",
            "gpt-5.6-luna", "gemini-2.5-flash",
            "claude-sonnet-4-6", "gemini-2.5-pro");

    /** The Gemini model to call for this name, warning when it is not the one that was asked for. */
    private static String geminiFor(String modelName) {
        String gemini = NEAREST_GEMINI.get(modelName);
        if (gemini == null) {
            return modelName;
        }
        log.warn(
                "'{}' is a Foundry deployment, using {} instead — the cost logged for it will be wrong",
                modelName,
                gemini);
        return gemini;
    }

    /**
     * Builds a model by name for the two tasks that need one other than the default.
     *
     * <p>Given, and deliberately thin: it is the same builder as above with the name passed in. Task
     * 5 uses it to try one model against another, and task 7 to get a streaming one.
     */
    @Bean
    Models models(VertexAiProperties properties) {
        return new Models() {
            @Override
            public ChatModel named(String modelName) {
                return VertexAiGeminiChatModel.builder()
                        .project(properties.projectId())
                        .location(properties.location())
                        .modelName(geminiFor(modelName))
                        .temperature(properties.temperature())
                        .maxOutputTokens(properties.maxOutputTokens())
                        .maxRetries(properties.maxRetries())
                        .build();
            }

            @Override
            public StreamingChatModel streamingNamed(String modelName) {
                return VertexAiGeminiStreamingChatModel.builder()
                        .project(properties.projectId())
                        .location(properties.location())
                        .modelName(geminiFor(modelName))
                        .temperature(properties.temperature())
                        .maxOutputTokens(properties.maxOutputTokens())
                        .build();
            }
        };
    }
}
