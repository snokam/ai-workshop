package com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools_and_memory;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools_and_memory.agent.ChatConfig;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import org.junit.jupiter.api.Test;

/**
 * One conversation per claim, and the reason it has to be checked.
 *
 * <p>A provider that ignores its id and hands the same memory to everyone compiles, starts, and
 * behaves perfectly on one claim. The failure needs two: open a second and a handler is reading
 * somebody else's conversation about somebody else's claim. Nothing throws, and the answers read as
 * fluently as ever.
 *
 * <p>No model here. Whether each claim gets its own conversation is decided by ordinary code, which
 * is why this is the part of task 6 that can be asserted at all — the tool descriptions are prompts
 * and the tools call is something you see rather than something a test reports.
 */
class ChatConfigTest {

    @Test
    void eachClaimGetsItsOwnConversation() {
        ChatMemoryProvider conversations = ChatConfig.claimConversations();

        ChatMemory one = conversations.get("claim-1");
        ChatMemory another = conversations.get("claim-2");

        assertThat(one)
                .describedAs("two claims sharing one memory means one handler reads another's"
                        + " conversation — build the memory inside the lambda, not once outside it")
                .isNotSameAs(another);
    }

    @Test
    void theConversationIsKeyedByTheClaimItBelongsTo() {
        assertThat(ChatConfig.claimConversations().get("claim-1").id()).isEqualTo("claim-1");
    }
}
