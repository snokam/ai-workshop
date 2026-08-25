package com.example.aiworkshop.tasks.task_7_streaming_form_help.agent;

import com.example.aiworkshop.workshop.UnfinishedTasks;
import com.example.aiworkshop.workshop.WorkshopTask;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
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
                WorkshopTask.STREAMING_FORM_HELP,
                () -> AiServices.create(ClaimIntakeInterviewer.class, chatModel));
    }

    @Bean
    ClaimFormHelper claimFormHelper(StreamingChatModel streamingChatModel) {
        return AiServices.builder(ClaimFormHelper.class)
                .streamingChatModel(streamingChatModel)
                .build();
    }
}
