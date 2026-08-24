package com.example.aiworkshop.tasks.task_2_guardrails;

import com.example.aiworkshop.tasks.task_2_guardrails.claim_description.ClaimCheck;
import com.example.aiworkshop.tasks.task_2_guardrails.prompt_injection.InjectionCheck;
import com.example.aiworkshop.workshop.UnfinishedTasks;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailRequest;
import dev.langchain4j.guardrail.InputGuardrailResult;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import java.util.function.Supplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * Task 2 hands its guardrails to task 1 rather than task 1 reaching for them.
 *
 * <p>This is the shape used wherever a later task changes how an earlier one behaves: the later task
 * publishes beans, the earlier one takes a list of them and knows nothing about who filled it. Task
 * 5 listens for task 3's event for the same reason.
 *
 * <p>The checks are agents like any other, built from the same ChatModel the classifier uses. There
 * is no loop: they are wired without guardrails of their own, so asking one a question does not ask
 * it again.
 *
 * <p>{@code @Order} is doing real work here. Spring builds a {@code List<InputGuardrail>} in whatever
 * order it finds the beans, and LangChain4j runs them in the order of the list and stops at the
 * first fatal one — so without {@code @Order} the sequence would be an accident of bean naming, and
 * it would change the day somebody renames a method.
 */
@Configuration
class GuardrailConfig {

    @Bean
    ClaimCheck claimCheck(ChatModel chatModel) {
        return AiServices.create(ClaimCheck.class, chatModel);
    }

    @Bean
    InjectionCheck injectionCheck(ChatModel chatModel) {
        return AiServices.create(InjectionCheck.class, chatModel);
    }

    @Bean
    @Order(1)
    InputGuardrail promptInjectionGuardrail(InjectionCheck check) {
        return onceWritten(() -> Guardrails.againstPromptInjection(check));
    }

    @Bean
    @Order(2)
    InputGuardrail claimDescriptionGuardrail(ClaimCheck check) {
        return onceWritten(() -> Guardrails.againstWastedCalls(check));
    }

    /**
     * The guardrail if the task has been written, and one that waves everything through if it has
     * not — so an unfinished task 2 leaves task 1 working rather than stopping the application.
     */
    private static InputGuardrail onceWritten(Supplier<InputGuardrail> guardrail) {
        if (UnfinishedTasks.written(guardrail::get)) {
            return guardrail.get();
        }
        return new InputGuardrail() {
            @Override
            public InputGuardrailResult validate(InputGuardrailRequest request) {
                return InputGuardrailResult.success();
            }
        };
    }
}
