package com.example.aiworkshop.tasks.task_5_claim_summary.agent;

import com.example.aiworkshop.workshop.UnfinishedTasks;
import com.example.aiworkshop.workshop.WorkshopTask;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Task 5's wiring: the expensive agent, and the cheap one beside it. */
@Configuration
class SummaryConfig {

    @Bean
    ClaimSummarizer caseSummarizer(ChatModel chatModel) {
        return UnfinishedTasks.wire(
                ClaimSummarizer.class,
                WorkshopTask.CLAIM_SUMMARY,
                () -> AiServices.create(ClaimSummarizer.class, chatModel));
    }

    @Bean
    ClaimStatusWriter caseStatusWriter(ChatModel chatModel) {
        return AiServices.create(ClaimStatusWriter.class, chatModel);
    }
}
