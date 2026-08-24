package com.example.aiworkshop.tasks.task_7_streaming_file_claim_chat.agent;

import com.example.aiworkshop.workshop.UnfinishedTasks;
import com.example.aiworkshop.workshop.WorkshopTask;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Task 7's wiring: the optional intake agent that may ask before it commits. */
@Configuration
class CreateClaimChatConfig {

    @Bean
    ClaimIntakeInterviewer caseIntakeInterviewer(ChatModel chatModel) {
        return UnfinishedTasks.wire(
                ClaimIntakeInterviewer.class,
                WorkshopTask.STREAMING_FILE_CLAIM_CHAT,
                () -> AiServices.create(ClaimIntakeInterviewer.class, chatModel));
    }

    /**
     * The voice, built on the streaming model rather than the ordinary one. Same one-line wiring; the
     * only difference is which model goes in, and that follows from the return type of its method.
     */
    @Bean
    ClaimIntakeSpeaker claimIntakeSpeaker(StreamingChatModel streamingChatModel) {
        return AiServices.builder(ClaimIntakeSpeaker.class)
                .streamingChatModel(streamingChatModel)
                .build();
    }
}
