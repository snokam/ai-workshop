package com.example.aiworkshop.tasks.task_5_summary.agent;

import com.example.aiworkshop.workshop.UnfinishedTasks;
import com.example.aiworkshop.workshop.WorkshopTask;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Task 6's wiring: the expensive agent, and the cheap one beside it. */
@Configuration
class SummaryConfig {

    @Bean
    CaseSummarizer caseSummarizer(ChatModel chatModel) {
        return UnfinishedTasks.wire(
                CaseSummarizer.class,
                WorkshopTask.SUMMARY,
                () -> AiServices.create(CaseSummarizer.class, chatModel));
    }

    @Bean
    CaseStatusWriter caseStatusWriter(ChatModel chatModel) {
        return AiServices.create(CaseStatusWriter.class, chatModel);
    }
}
