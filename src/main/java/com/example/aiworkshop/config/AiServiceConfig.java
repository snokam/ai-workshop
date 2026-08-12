package com.example.aiworkshop.config;

import com.example.aiworkshop.ai.Assistant;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provider-agnostic. Takes whichever {@link ChatModel} bean the active provider configuration
 * contributed — this is the layer that grows chat memory, tools, and RAG as we move toward an agent.
 */
@Configuration
class AiServiceConfig {

    @Bean
    Assistant assistant(ChatModel chatModel) {
        return AiServices.create(Assistant.class, chatModel);
    }
}
