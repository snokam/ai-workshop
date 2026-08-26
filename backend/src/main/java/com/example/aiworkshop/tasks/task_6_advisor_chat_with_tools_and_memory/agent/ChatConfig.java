package com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools_and_memory.agent;

import com.example.aiworkshop.workshop.UnfinishedTasks;
import com.example.aiworkshop.workshop.WorkshopTask;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Task 6's wiring, and the only configuration here longer than a line — because it is the only agent
 * with tools and a memory.
 *
 * <p>Both are one builder call. {@code .tools(...)} is what turns the methods on {@link
 * ClaimChatTools} into things the model may call: LangChain4j reads their {@code @Tool} descriptions,
 * sends them with every request as the list of what is available, and when the model asks for one it
 * invokes the method and hands the result back into the same turn. Nothing else is needed and there
 * is no dispatch to write.
 *
 * <p>{@code .chatMemoryProvider} is the other half. The lambda is handed the {@code @MemoryId} — the
 * claim id — so every claim gets its own window of twenty messages, and two claim handlers talking
 * about two claims never see each other's conversation.
 */
@Configuration
public class ChatConfig {

    @Bean
    ClaimChatAgent claimChatAgent(ChatModel chatModel, ClaimChatTools tools) {
        if (!UnfinishedTasks.written(ChatConfig::claimConversations)) {
            return UnfinishedTasks.notWrittenYet(ClaimChatAgent.class, WorkshopTask.ADVISOR_CHAT_WITH_TOOLS_AND_MEMORY);
        }

        AiServices<ClaimChatAgent> agent = AiServices.builder(ClaimChatAgent.class)
                .chatModel(chatModel)
                .chatMemoryProvider(claimConversations());

        agent.tools(tools);

        return agent.build();
    }

    /**
     * One conversation per claim.
     *
     * <p>{@code @MemoryId} on {@link ClaimChatAgent#answer} is the claim identifier, and it is what
     * this is handed. It is also not optional: LangChain4j refuses to build an agent that declares a
     * memory id without a provider, so there is no version of this chat wired without one.
     *
     * <p>Public so a test can ask about it without a model in the room.
     */
    public static ChatMemoryProvider claimConversations() {
        // Inside the lambda, so each claim gets its own. Twenty messages is fewer turns than it
        // sounds: the opening context is one, and every tool call and tool result is one too.
        return claimId -> MessageWindowChatMemory.builder()
                .id(claimId)
                .maxMessages(20)
                .build();
    }

    @Bean
    DocumentReader documentReader(ChatModel chatModel) {
        return AiServices.create(DocumentReader.class, chatModel);
    }
}
