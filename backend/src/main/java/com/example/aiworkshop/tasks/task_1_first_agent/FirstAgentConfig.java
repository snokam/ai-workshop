package com.example.aiworkshop.tasks.task_1_first_agent;

import com.example.aiworkshop.workshop.WorkshopTask;
import com.example.aiworkshop.workshop.UnfinishedTasks;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Where the SDK is put to work for the first time, and the whole of task 1's wiring.
 *
 * <p>Two halves make an agent. {@link ChatModel} is the connection — which provider, which model,
 * which credentials — and it is built in {@code config/VertexAiConfig} or {@code
 * config/FoundryConfig} from {@code application.properties}, whichever {@code aiworkshop.model
 * .provider} selects. {@link AiServices#create} is the other half: hand it an interface and a
 * model and it writes the implementation, so nothing in this application ever calls an HTTP API or
 * parses a response.
 *
 * <p>That is the one line below, and it is the same line for five of the six tasks. What differs
 * between agents is never the wiring, it is the interface.
 */
@Configuration
class FirstAgentConfig {

    /** The case intake agent: reads what a Claimant typed and picks which case type to open. */
    @Bean
    CaseTypeClassifier caseTypeClassifier(ChatModel chatModel) {
        return UnfinishedTasks.wire(
                CaseTypeClassifier.class,
                WorkshopTask.FIRST_AGENT,
                CaseTypeClassifier.IMPLEMENTED,
                () -> AiServices.create(CaseTypeClassifier.class, chatModel));
    }
}
