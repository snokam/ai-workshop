package com.example.aiworkshop.tasks.task_7_dynamic_form_with_streaming.agent;

import com.example.aiworkshop.tasks.task_1_first_agent.agent.Models;
import com.example.aiworkshop.workshop.UnfinishedTasks;
import com.example.aiworkshop.workshop.WorkshopTask;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Task 7's wiring, and the only agent in the workshop built on a streaming model.
 *
 * <p>Same one line as every other agent; the only difference is which model goes in, and that follows
 * from the return type of its method. An interface whose method returns a {@code TokenStream} needs
 * {@code .streamingChatModel(...)}, and one returning a record needs {@code .chatModel(...)} — you
 * cannot give a builder both and have it mean anything.
 */
@Configuration
class FormHelpConfig {

    /** The agent that decides which follow-up fields the form should show. Answers into a record. */
    @Bean
    ClaimIntakeInterviewer claimIntakeInterviewer(ChatModel chatModel) {
        return UnfinishedTasks.wire(
                ClaimIntakeInterviewer.class,
                WorkshopTask.DYNAMIC_FORM_WITH_STREAMING,
                () -> AiServices.create(ClaimIntakeInterviewer.class, chatModel));
    }

    /**
     * The model this task streams from, and the smallest one on purpose.
     *
     * <p>A reasoning model thinks before it writes, so it emits nothing for seconds and then the whole
     * answer at once — measured here, gemini-2.5-flash gave its first token after 4.72s in a single
     * chunk, and this one started in 0.44s across four. On a stronger model this feature would not be
     * streaming, it would be arriving late.
     */
    static final String FOR_STREAMING = "gemini-2.5-flash-lite";

    @Bean
    ClaimFormHelper claimFormHelper(Models models) {
        return AiServices.builder(ClaimFormHelper.class)
                .streamingChatModel(models.streamingNamed(FOR_STREAMING))
                .build();
    }
}
