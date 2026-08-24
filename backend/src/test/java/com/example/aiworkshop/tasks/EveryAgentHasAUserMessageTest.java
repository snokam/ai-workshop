package com.example.aiworkshop.tasks;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.aiworkshop.tasks.task_1_first_agent.agent.ClaimTypeClassifier;
import com.example.aiworkshop.tasks.task_3_document_agent.agent.DocumentAnalyzer;
import com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools.agent.ClaimChatAgent;
import com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools.agent.DocumentReader;
import com.example.aiworkshop.tasks.task_5_claim_summary_using_memory.agent.ClaimStatusWriter;
import com.example.aiworkshop.tasks.task_5_claim_summary_using_memory.agent.ClaimSummarizer;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Every agent has to be told what the turn is, not only what the rules are.
 *
 * <p>An interface with instructions and no user message compiles, wires, starts, and fails on the
 * first call with "does not have a user message defined" — so the cost of getting it wrong is a
 * screen that breaks in front of someone rather than a build that stops. This is the cheapest
 * possible test and it exists because that happened.
 */
class EveryAgentHasAUserMessageTest {

    static Stream<Class<?>> agents() {
        return Stream.of(
                ClaimTypeClassifier.class,
                DocumentAnalyzer.class,
                ClaimChatAgent.class,
                ClaimSummarizer.class,
                ClaimStatusWriter.class,
                DocumentReader.class);
    }

    @DisplayName("agent says what the turn is")
    @ParameterizedTest(name = "{0}")
    @MethodSource("agents")
    void hasAUserMessage(Class<?> agent) {
        for (Method method : agent.getMethods()) {
            if (method.getAnnotation(SystemMessage.class) == null) {
                continue;
            }
            boolean onTheMethod = method.getAnnotation(UserMessage.class) != null;
            boolean onAnArgument = Stream.of(method.getParameters())
                    .map(Parameter::getAnnotations)
                    .flatMap(Stream::of)
                    .anyMatch(a -> a.annotationType() == UserMessage.class);

            assertThat(onTheMethod || onAnArgument)
                    .describedAs(
                            "%s.%s has instructions but no user message. Put @UserMessage on the method with a"
                                    + " template, or on the argument that is the turn.",
                            agent.getSimpleName(), method.getName())
                    .isTrue();
        }
    }
}
