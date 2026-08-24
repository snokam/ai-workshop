package com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools;

import com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools.agent.ClaimChatAgent;
import com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools.agent.ClaimChatTools;
import static org.assertj.core.api.Assertions.assertThat;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

/**
 * The three things that make this agent different from the four before it.
 *
 * <p>Each has a failure mode that looks like nothing: a memory id that is not bound and every
 * question starts a new conversation, a return type without {@link Result} and the tool calls are
 * lost after the answer is built, a tool with logic in it and the agent quietly gets a private
 * version of the claim.
 */
class ClaimChatAgentTest {

    private static Method answer() {
        return Arrays.stream(ClaimChatAgent.class.getMethods())
                .filter(m -> m.getName().equals("answer"))
                .findFirst()
                .orElseThrow();
    }

    @Test
    void hasASystemMessageOfItsOwn() {
        SystemMessage system = answer().getAnnotation(SystemMessage.class);

        assertThat(system).describedAs("answer() needs an @SystemMessage").isNotNull();
    }

    @Test
    void remembersPerClaim() {
        Method answer = answer();

        assertThat(answer.getParameters()[0].getAnnotation(MemoryId.class))
                .describedAs("without a @MemoryId every question starts a conversation from nothing")
                .isNotNull();
        assertThat(answer.getParameters()[1].getAnnotation(UserMessage.class)).isNotNull();
    }

    @Test
    void keepsTheToolCallsItMade() {
        assertThat(answer().getReturnType())
                .describedAs(
                        "Result carries toolExecutions(); return the bare type and the answer survives but"
                                + " what it was worked out from does not")
                .isEqualTo(Result.class);
    }

    @Test
    void hasToolsThatOnlyHandOver() {
        long tools = Arrays.stream(ClaimChatTools.class.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(Tool.class))
                .count();

        assertThat(tools).describedAs("the agent needs tools to look anything up").isPositive();
    }
}
