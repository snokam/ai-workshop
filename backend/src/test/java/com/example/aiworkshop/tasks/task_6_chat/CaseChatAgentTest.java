package com.example.aiworkshop.tasks.task_6_chat;

import com.example.aiworkshop.tasks.task_6_chat.agent.CaseChatAgent;
import com.example.aiworkshop.tasks.task_6_chat.agent.CaseChatTools;
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
 * version of the case.
 */
class CaseChatAgentTest {

    private static Method answer() {
        return Arrays.stream(CaseChatAgent.class.getMethods())
                .filter(m -> m.getName().equals("answer"))
                .findFirst()
                .orElseThrow();
    }

    @Test
    void hasASystemMessageOfItsOwn() {
        SystemMessage system = answer().getAnnotation(SystemMessage.class);

        assertThat(system).describedAs("answer() needs an @SystemMessage").isNotNull();
        assertThat(String.join("\n", system.value()))
                .describedAs("the @SystemMessage still holds the brief it shipped with — write the prompt")
                .doesNotContain("TODO");
    }

    @Test
    void remembersPerCase() {
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
        long tools = Arrays.stream(CaseChatTools.class.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(Tool.class))
                .count();

        assertThat(tools).describedAs("the agent needs tools to look anything up").isPositive();
    }
}
