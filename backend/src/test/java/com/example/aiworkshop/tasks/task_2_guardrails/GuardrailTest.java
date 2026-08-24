package com.example.aiworkshop.tasks.task_2_guardrails;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.aiworkshop.tasks.task_2_guardrails.guardrails.ClaimCheck;
import com.example.aiworkshop.tasks.task_2_guardrails.guardrails.Guardrails;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrailResult;
import org.junit.jupiter.api.Test;

/**
 * The guardrail in front of the first agent.
 *
 * <p>The judgement belongs to a model, so it is not tested here — what a model says about "hi" is
 * task 4's question, measured over a set rather than asserted one case at a time. What is tested
 * here is everything around the judgement, which is ordinary code and fails in ordinary ways.
 */
class GuardrailTest {

    private static final ClaimCheck ALWAYS_YES = description -> new ClaimCheck.Verdict(true, "");
    private static final ClaimCheck ALWAYS_NO =
            description -> new ClaimCheck.Verdict(false, "Tell us what happened.");

    @Test
    void aNoBecomesARefusalCarryingWhatTheCheckWrote() {
        InputGuardrailResult result = refuse(ALWAYS_NO, "hi");

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.toString()).contains("Tell us what happened.");
    }

    @Test
    void aYesGoesThrough() {
        assertThat(refuse(ALWAYS_YES, "Someone reversed into my parked car.").isSuccess())
                .isTrue();
    }




    private static InputGuardrailResult refuse(ClaimCheck check, String description) {
        return Guardrails.beforeTheCall(check).validate(UserMessage.from(description));
    }
}
