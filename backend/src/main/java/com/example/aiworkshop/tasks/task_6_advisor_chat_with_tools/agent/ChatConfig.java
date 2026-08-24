package com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools.agent;

import com.example.aiworkshop.workshop.UnfinishedTasks;
import com.example.aiworkshop.workshop.WorkshopTask;
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
class ChatConfig {

    @Bean
    ClaimChatAgent caseChatAgent(ChatModel chatModel, ClaimChatTools tools) {
        return UnfinishedTasks.wire(
                ClaimChatAgent.class,
                WorkshopTask.ADVISOR_CHAT_WITH_TOOLS,
                () -> {
                    AiServices<ClaimChatAgent> agent = AiServices.builder(ClaimChatAgent.class)
                            .chatModel(chatModel)
                            .chatMemoryProvider(claimId -> MessageWindowChatMemory.withMaxMessages(20));

                    // TODO — task 6, part 2. Give the model its tools.
                    //
                    // One call:
                    //
                    //   agent.tools(tools);
                    //
                    // Until it is there the agent is built without them. It still answers — fluently, and
                    // about the claim, because the summary is in its opening context — and it never looks
                    // anything up. That is the failure worth seeing once: an agent with no tools does not
                    // report that it cannot check, it just answers from what it was given.
                    //
                    // Ask it "what is the total on the repair invoice?" before and after.

                    return agent.build();
                });
    }

    @Bean
    DocumentReader documentReader(ChatModel chatModel) {
        return AiServices.create(DocumentReader.class, chatModel);
    }
}
