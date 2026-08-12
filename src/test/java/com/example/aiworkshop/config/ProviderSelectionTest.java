package com.example.aiworkshop.config;

import static org.assertj.core.api.Assertions.assertThat;

import dev.langchain4j.model.azure.AzureOpenAiChatModel;
import dev.langchain4j.model.chat.ChatModel;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/**
 * Verifies the {@code aiworkshop.model.provider} switch, without calling either provider. Dummy
 * Azure credentials are enough because the client is only built here, never used.
 */
class ProviderSelectionTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(VertexAiConfig.class, FoundryConfig.class))
            .withPropertyValues(
                    "vertex-ai.project=dummy-project",
                    "vertex-ai.location=europe-west4",
                    "vertex-ai.model-name=dummy-model",
                    "foundry.endpoint=https://example.openai.azure.com/",
                    "foundry.api-key=dummy-key",
                    "foundry.deployment-name=dummy-deployment");

    @Test
    void foundryProviderContributesAzureModel() {
        runner.withPropertyValues("aiworkshop.model.provider=foundry")
                .run(context -> assertThat(context)
                        .getBean(ChatModel.class)
                        .isInstanceOf(AzureOpenAiChatModel.class));
    }

    @Test
    void foundryProviderExcludesVertexConfig() {
        runner.withPropertyValues("aiworkshop.model.provider=foundry")
                .run(context -> assertThat(context).doesNotHaveBean(VertexAiConfig.class));
    }

    @Test
    void vertexIsTheDefaultProvider() {
        runner.run(context -> assertThat(context)
                .hasSingleBean(VertexAiConfig.class)
                .doesNotHaveBean(FoundryConfig.class));
    }
}
