package com.example.aiworkshop.tasks.task_3_guardrails;

import com.example.aiworkshop.workshop.UnfinishedTasks;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailRequest;
import dev.langchain4j.guardrail.InputGuardrailResult;
import dev.langchain4j.guardrail.OutputGuardrail;
import dev.langchain4j.guardrail.OutputGuardrailRequest;
import dev.langchain4j.guardrail.OutputGuardrailResult;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Task 3's wiring: the guardrails are contributed to the intake agent rather than fetched by it.
 *
 * <p>Nothing in task 2 mentions this file. That agent takes whatever guardrails are in the context,
 * so task 3 can add checks to it without task 2 being told, and task 2 works on its own before task
 * 3 exists.
 *
 * <p>Until the task is written these are guardrails that pass everything, which is what no guardrail
 * looks like from the outside: the upload works and nothing is checked.
 */
@Configuration
class GuardrailConfig {

    @Bean
    InputGuardrail uploadedFileGuardrail() {
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

    @Bean
    OutputGuardrail analysisGuardrail() {
        if (UnfinishedTasks.written(Guardrails::afterTheCall)) {
            return Guardrails.afterTheCall();
        }
        return new OutputGuardrail() {
            @Override
            public OutputGuardrailResult validate(OutputGuardrailRequest request) {
                return OutputGuardrailResult.success();
            }
        };
    }
}
