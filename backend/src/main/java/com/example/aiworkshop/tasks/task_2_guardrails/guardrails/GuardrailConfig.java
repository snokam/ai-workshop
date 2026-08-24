package com.example.aiworkshop.tasks.task_2_guardrails.guardrails;

import com.example.aiworkshop.workshop.UnfinishedTasks;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailRequest;
import dev.langchain4j.guardrail.InputGuardrailResult;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Task 2 hands its guardrail to task 1 rather than task 1 reaching for it.
 *
 * <p>This is the shape the workshop uses wherever a later task changes how an earlier one behaves:
 * the later task publishes a bean, the earlier one takes a list of them and knows nothing about who
 * filled it. Task 5 listens for task 3's event for the same reason.
 *
 * <p>Until the task is written this is a guardrail that passes everything, which is what no
 * guardrail looks like from the outside: the box works and nothing is checked.
 */
@Configuration
class GuardrailConfig {

    @Bean
    InputGuardrail claimDescriptionGuardrail() {
        if (UnfinishedTasks.written(Guardrails::beforeTheCall)) {
            return Guardrails.beforeTheCall();
        }
        return new InputGuardrail() {
            @Override
            public InputGuardrailResult validate(InputGuardrailRequest request) {
                return InputGuardrailResult.success();
            }
        };
    }
}
