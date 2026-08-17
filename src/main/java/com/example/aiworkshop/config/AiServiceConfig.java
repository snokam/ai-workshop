package com.example.aiworkshop.config;

import com.example.aiworkshop.cases.CaseStatusWriter;
import com.example.aiworkshop.cases.CaseSummarizer;
import com.example.aiworkshop.document.DocumentAnalyzer;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provider-agnostic. Takes whichever {@link ChatModel} bean the active provider configuration
 * contributed — this is the layer that grows chat memory, tools, and RAG as we move toward an agent.
 *
 * <p>Every agent in the application is one line here. {@link AiServices#create} generates the
 * implementation from the interface, so the interface is the whole of the agent's definition.
 */
@Configuration
class AiServiceConfig {

    /** The intake agent: reads an uploaded file and returns structured findings about it. */
    @Bean
    DocumentAnalyzer documentAnalyzer(ChatModel chatModel) {
        return AiServices.create(DocumentAnalyzer.class, chatModel);
    }

    /** The Case Summary: what is in a Case's Documents, read across all of them. */
    @Bean
    CaseSummarizer caseSummarizer(ChatModel chatModel) {
        return AiServices.create(CaseSummarizer.class, chatModel);
    }

    /** The situation report: derived facts in, one short piece of prose out. */
    @Bean
    CaseStatusWriter caseStatusWriter(ChatModel chatModel) {
        return AiServices.create(CaseStatusWriter.class, chatModel);
    }
}
