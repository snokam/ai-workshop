package com.example.aiworkshop.tasks.task_2_document_agent;

import com.example.aiworkshop.workshop.UnfinishedTasks;
import com.example.aiworkshop.workshop.WorkshopTask;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.OutputGuardrail;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Task 2's wiring.
 *
 * <p>Guardrails arrive rather than being fetched. This agent takes whichever ones exist and knows
 * nothing about where they came from — so task 3 can add checks to it without task 2 being told,
 * and task 2 works on its own before task 3 is written. Reach forward for them instead and the
 * order the workshop is done in stops being yours to choose.
 */
@Configuration
class DocumentAgentConfig {

    @Bean
    DocumentAnalyzer documentAnalyzer(
            ChatModel chatModel, List<InputGuardrail> beforeTheCall, List<OutputGuardrail> afterTheCall) {
        return UnfinishedTasks.wire(DocumentAnalyzer.class, WorkshopTask.DOCUMENT_AGENT, () -> {
            var agent = AiServices.builder(DocumentAnalyzer.class).chatModel(chatModel);
            if (!beforeTheCall.isEmpty()) {
                agent.inputGuardrails(beforeTheCall);
            }
            if (!afterTheCall.isEmpty()) {
                agent.outputGuardrails(afterTheCall);
            }
            return agent.build();
        });
    }
}
