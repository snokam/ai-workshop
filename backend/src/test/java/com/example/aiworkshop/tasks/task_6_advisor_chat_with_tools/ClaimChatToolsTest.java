package com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools.agent.ClaimChatTools;
import dev.langchain4j.agent.tool.Tool;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * A tool description is a prompt, and an unwritten one fails silently.
 *
 * <p>The method still compiles, still appears in the list of tools sent with every request, and the
 * model still decides whether to call it — from a placeholder. So it gets called for everything, or
 * for nothing, and either way the chat looks like a model that is bad at its job rather than a tool
 * that was never described.
 */
class ClaimChatToolsTest {

    @Test
    void everyToolSaysWhenItShouldBeUsed() {
        List<String> unwritten = Arrays.stream(ClaimChatTools.class.getDeclaredMethods())
                .filter(method -> {
                    Tool described = method.getAnnotation(Tool.class);
                    return described != null && String.join(" ", described.value()).contains("TODO");
                })
                .map(Method::getName)
                .sorted()
                .toList();

        assertThat(unwritten)
                .describedAs("these tools still hold the note they shipped with — task 6, part 1 is"
                        + " writing the sentence the model reads when deciding whether to call them")
                .isEmpty();
    }
}
