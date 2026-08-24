package com.example.aiworkshop.tasks.task_3_document_agent.agent;

import com.example.aiworkshop.workshop.UnfinishedTasks;
import com.example.aiworkshop.workshop.WorkshopTask;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Task 2's wiring.
 *
 * <p>Guardrails arrive rather than being fetched. This agent takes whichever ones exist and knows
 * nothing about where they came from — so a later task can add checks to it without task 3 being
 * told, and task 3 works on its own regardless. Reach forward for them instead and the
 * order the workshop is done in stops being yours to choose.
 */
@Configuration
class DocumentAgentConfig {

    @Bean
    DocumentAnalyzer documentAnalyzer(
            ChatModel chatModel) {
        return UnfinishedTasks.wire(DocumentAnalyzer.class, WorkshopTask.DOCUMENT_AGENT, () -> {
            var agent = AiServices.builder(DocumentAnalyzer.class).chatModel(chatModel);
            return agent.build();
        });
    }
}
