package com.example.aiworkshop.tasks.task_7_create_case_chat;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.aiworkshop.cases.interview.InterviewTurn;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

/**
 * What the interviewer is, checked without calling a model.
 *
 * <p>As with every agent, none of this asks whether the prompt is any good — no test can, and one
 * that called the model would be slow, need credentials, and go red for reasons unrelated to your
 * work. What it can check is the wiring: a prompt still holding its TODO, the taxonomy hard-coded
 * instead of rendered, the transcript smuggled into the instructions, or the two-in-one answer shape
 * quietly lost.
 */
class CaseIntakeInterviewerTest {

    private static Method next() {
        return Arrays.stream(CaseIntakeInterviewer.class.getMethods())
                .filter(method -> method.getName().equals("next"))
                .findFirst()
                .orElseThrow();
    }

    @Test
    void hasASystemMessageOfItsOwn() {
        SystemMessage system = next().getAnnotation(SystemMessage.class);

        assertThat(system)
                .describedAs("next() needs an @SystemMessage: that is where the agent is defined")
                .isNotNull();
        assertThat(String.join("\n", system.value()))
                .describedAs("the @SystemMessage still holds the brief it shipped with — write the prompt")
                .doesNotContain("TODO");
    }

    @Test
    void rendersTheScenarioCatalogueInsteadOfHardCodingIt() {
        assertThat(String.join("\n", next().getAnnotation(SystemMessage.class).value()))
                .describedAs(
                        "the prompt should render {{scenarios}}, so the taxonomy the agent chooses from is the"
                                + " enum a case is opened from rather than a second copy that goes stale")
                .contains("{{scenarios}}");
    }

    @Test
    void keepsTheTranscriptInTheUserTurn() {
        Method next = next();

        assertThat(next.getParameters()[0].getAnnotation(V.class))
                .describedAs("the scenario catalogue is a template variable, so it belongs in the system message")
                .isNotNull();

        UserMessage user = next.getAnnotation(UserMessage.class);
        assertThat(user)
                .describedAs(
                        "the conversation is untrusted free text and belongs in the user turn, not the"
                                + " instructions")
                .isNotNull();
        assertThat(String.join("\n", user.value()))
                .describedAs("the user turn should render {{transcript}} — the running conversation the agent reads")
                .contains("{{transcript}}");
    }

    @Test
    void returnsTheDiscriminatedInterviewTurn() {
        assertThat(next().getReturnType())
                .describedAs("one call does two things — ask or decide — which is the shape of InterviewTurn")
                .isEqualTo(InterviewTurn.class);
    }
}
