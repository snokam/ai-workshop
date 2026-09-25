package com.example.aiworkshop.tasks.task_1_first_agent;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.aiworkshop.tasks.task_1_first_agent.agent.FoundryConfig;
import com.example.aiworkshop.tasks.task_1_first_agent.agent.VertexAiConfig;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.vertexai.gemini.VertexAiGeminiChatModel;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/**
 * Which provider gets wired, not whether task 1 is written.
 *
 * <p>The distinction matters: until {@code FoundryConfig.chatModel} is filled in there is still a
 * {@code ChatModel} bean, it is just one that refuses to be used. Whether it is a real model is
 * {@code TaskCompletionTest}'s question. Whether the right config was switched on is this one's.
 */
class ProviderSelectionTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(VertexAiConfig.class, FoundryConfig.class))
            .withPropertyValues(
                    "vertex-ai.project=dummy-project",
                    "vertex-ai.location=europe-west4",
                    "vertex-ai.model-name=dummy-model",
                    "foundry.endpoint=https://example.services.ai.azure.com/openai/v1",
                    "foundry.api-key=dummy-key",
                    "foundry.deployment-name=dummy-deployment");

    @Test
    void foundryIsTheDefaultProvider() {
        runner.run(context -> assertThat(context)
                .hasSingleBean(FoundryConfig.class)
                .doesNotHaveBean(VertexAiConfig.class));
    }

    @Test
    void theDefaultProviderStillContributesAChatModel() {
        runner.run(context -> assertThat(context).hasSingleBean(ChatModel.class));
    }

    @Test
    void vertexProviderContributesAGeminiModel() {
        runner.withPropertyValues("aiworkshop.model.provider=vertex")
                .run(context -> assertThat(context)
                        .getBean(ChatModel.class)
                        .isInstanceOf(VertexAiGeminiChatModel.class));
    }

    @Test
    void vertexProviderExcludesFoundryConfig() {
        runner.withPropertyValues("aiworkshop.model.provider=vertex")
                .run(context -> assertThat(context).doesNotHaveBean(FoundryConfig.class));
    }
}
