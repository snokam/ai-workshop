package com.example.aiworkshop.tasks.task_2_document_agent;

import com.example.aiworkshop.tasks.task_3_guardrails.Guardrails;
import com.example.aiworkshop.workshop.UnfinishedTasks;
import com.example.aiworkshop.workshop.WorkshopTask;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Task 2's wiring: the builder rather than AiServices.create, because this is the agent that
 * carries guardrails — which is task 3.
 */
@Configuration
class DocumentAgentConfig {

    @Bean
    DocumentAnalyzer documentAnalyzer(ChatModel chatModel) {
        return UnfinishedTasks.wire(
                DocumentAnalyzer.class,
                WorkshopTask.DOCUMENT_AGENT,
                () -> {
                    var agent = AiServices.builder(DocumentAnalyzer.class).chatModel(chatModel);
                    if (UnfinishedTasks.written(Guardrails::beforeTheCall)) {
                        agent.inputGuardrails(Guardrails.beforeTheCall())
                                .outputGuardrails(Guardrails.afterTheCall());
                    }
                    return agent.build();
                });
    }
}
