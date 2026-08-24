package com.example.aiworkshop.tasks.task_2_guardrails.guardrails;

import com.example.aiworkshop.workshop.UnfinishedTasks;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailRequest;
import dev.langchain4j.guardrail.InputGuardrailResult;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Task 2 hands its guardrail to task 1 rather than task 1 reaching for it.
 *
 * <p>This is the shape used wherever a later task changes how an earlier one behaves: the later task
 * publishes a bean, the earlier one takes a list of them and knows nothing about who filled it. Task
 * 5 listens for task 3's event for the same reason.
 *
 * <p>The check is an agent like any other, built from the same ChatModel the classifier uses. There
 * is no loop: it is wired without guardrails of its own, so asking it a question does not ask it
 * again.
 */
@Configuration
class GuardrailConfig {

    @Bean
    ClaimCheck claimCheck(ChatModel chatModel) {
        return AiServices.create(ClaimCheck.class, chatModel);
    }

    @Bean
    InputGuardrail claimDescriptionGuardrail(ClaimCheck check) {
        if (UnfinishedTasks.written(() -> Guardrails.beforeTheCall(check))) {
            return Guardrails.beforeTheCall(check);
        }
        return new InputGuardrail() {
            @Override
            public InputGuardrailResult validate(InputGuardrailRequest request) {
                return InputGuardrailResult.success();
            }
        };
    }
}
