package com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools_and_memory.agent;

import com.example.aiworkshop.workshop.TaskNotImplementedException;
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

        // TODO — task 6, part 2. Give the agent its tools.
        //
        // One call:
        //
        //   agent.tools(tools);
        //
        // Until it is there the agent is built without them. It still answers — fluently, and about the
        // claim, because the summary is in its opening context — and it never looks anything up. That is
        // the failure worth seeing once: an agent with no tools does not report that it cannot check.
        // It answers from what it was given.
        //
        // Ask it "what is the total on the repair invoice?" before and after.

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
        // TODO — task 6, part 3. Give each claim its own conversation.
        //
        // Return a ChatMemoryProvider. It is handed the claim id and returns the memory for that claim:
        //
        //   return claimId -> MessageWindowChatMemory.builder().id(claimId).maxMessages(20).build();
        //
        // Build it INSIDE the lambda, never once outside:
        //
        //   ChatMemory shared = MessageWindowChatMemory.withMaxMessages(20);   // WRONG
        //   return claimId -> shared;
        //
        // That compiles and behaves perfectly on one claim. Open a second and one handler is reading
        // another handler's conversation about somebody else's claim. Nothing throws and the answers
        // read as fluently as ever. ChatConfigTest is what catches it.
        //
        // The window is a trade, and it is the part worth spending a minute on. Set it to 2 and hold a
        // four-turn conversation: find the exact turn where it contradicts itself, or asks you something
        // you already answered. Set it to 200 and every question re-sends the whole history, on a claim
        // somebody has been talking about all afternoon.
        //
        // Note what a message is here. It is not one line of chat: the first user message carries the
        // whole claim at a glance, and every tool call and tool result is a message too. A window of 20
        // is fewer turns than it sounds.

        throw new TaskNotImplementedException(WorkshopTask.ADVISOR_CHAT_WITH_TOOLS_AND_MEMORY);
    }

    @Bean
    DocumentReader documentReader(ChatModel chatModel) {
        return AiServices.create(DocumentReader.class, chatModel);
    }
}
