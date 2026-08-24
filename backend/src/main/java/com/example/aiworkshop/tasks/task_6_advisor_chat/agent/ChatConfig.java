package com.example.aiworkshop.tasks.task_6_advisor_chat.agent;

import com.example.aiworkshop.workshop.UnfinishedTasks;
import com.example.aiworkshop.workshop.WorkshopTask;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Task 6's wiring, and the only configuration here longer than a line — because it is the only
 * agent with tools and a memory.
 */
@Configuration
class ChatConfig {

    @Bean
    ClaimChatAgent caseChatAgent(ChatModel chatModel, ClaimChatTools tools) {
        return UnfinishedTasks.wire(
                ClaimChatAgent.class,
                WorkshopTask.ADVISOR_CHAT,
                () -> AiServices.builder(ClaimChatAgent.class)
                        .chatModel(chatModel)
                        .tools(tools)
                        .chatMemoryProvider(claimId -> MessageWindowChatMemory.withMaxMessages(20))
                        .build());
    }

    @Bean
    DocumentReader documentReader(ChatModel chatModel) {
        return AiServices.create(DocumentReader.class, chatModel);
    }
}
