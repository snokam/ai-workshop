package com.example.aiworkshop.tasks.task_2_guardrails;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.aiworkshop.tasks.task_2_guardrails.prompt_injection.PromptInjectionGuardrail;
import dev.langchain4j.guardrail.InputGuardrail;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Injection is checked first, and this is the only thing that says so.
 *
 * <p>LangChain4j runs input guardrails in the order of the list it is given and stops at the first
 * fatal one, and Spring builds that list in whatever order it finds the beans unless told otherwise.
 * {@code GuardrailConfig} tells it, with {@code @Order} — so the sequence is a decision rather than
 * an accident of method naming, and this test is what keeps it one. Without it, a rename could put
 * the claim check first and nothing would look wrong: both guardrails would still work, and
 * manipulated text would quietly start reaching a model one layer earlier than intended.
 */
@SpringBootTest
class GuardrailOrderTest {

    @Autowired
    private List<InputGuardrail> beforeTheCall;

    @Test
    void injectionIsCheckedBeforeAnythingElseReadsTheText() {
        assertThat(beforeTheCall).first().isInstanceOf(PromptInjectionGuardrail.class);
    }
}
