package com.example.aiworkshop.tasks.task_5_chat;

import com.example.aiworkshop.workshop.UnfinishedTasks;
import com.example.aiworkshop.workshop.WorkshopTask;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Task 5's wiring, and the only configuration here longer than a line — because it is the only
 * agent with tools and a memory.
 */
@Configuration
class ChatConfig {

    @Bean
    CaseChatAgent caseChatAgent(ChatModel chatModel, CaseChatTools tools) {
        return UnfinishedTasks.wire(
                CaseChatAgent.class,
                WorkshopTask.CHAT,
                CaseChatAgent.IMPLEMENTED,
                () -> AiServices.builder(CaseChatAgent.class)
                        .chatModel(chatModel)
                        .tools(tools)
                        .chatMemoryProvider(caseId -> MessageWindowChatMemory.withMaxMessages(20))
                        .build());
    }

    @Bean
    DocumentReader documentReader(ChatModel chatModel) {
        return AiServices.create(DocumentReader.class, chatModel);
    }
}
