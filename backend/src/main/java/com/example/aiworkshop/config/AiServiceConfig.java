package com.example.aiworkshop.config;

import com.example.aiworkshop.tasks.task_5_chat.CaseChatAgent;
import com.example.aiworkshop.tasks.task_5_chat.CaseChatTools;
import com.example.aiworkshop.tasks.task_6_summary.CaseStatusWriter;
import com.example.aiworkshop.tasks.task_6_summary.CaseSummarizer;
import com.example.aiworkshop.tasks.task_2_document_agent.DocumentAnalyzer;
import com.example.aiworkshop.document.DocumentReader;
import com.example.aiworkshop.tasks.task_3_guardrails.Guardrails;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import com.example.aiworkshop.workshop.UnfinishedTasks;
import com.example.aiworkshop.workshop.WorkshopTask;
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
        return UnfinishedTasks.wire(
                DocumentAnalyzer.class,
                WorkshopTask.DOCUMENT_AGENT,
                DocumentAnalyzer.IMPLEMENTED,
                () -> AiServices.builder(DocumentAnalyzer.class)
                        .chatModel(chatModel)
                        .inputGuardrails(Guardrails.beforeTheCall())
                        .outputGuardrails(Guardrails.afterTheCall())
                        .build());
    }

    /** The Case Summary: what is in a Case's Documents, read across all of them. */
    @Bean
    CaseSummarizer caseSummarizer(ChatModel chatModel) {
        return UnfinishedTasks.wire(
                CaseSummarizer.class,
                WorkshopTask.SUMMARY,
                CaseSummarizer.IMPLEMENTED,
                () -> AiServices.create(CaseSummarizer.class, chatModel));
    }

    /** The situation report: derived facts in, one short piece of prose out. */
    @Bean
    CaseStatusWriter caseStatusWriter(ChatModel chatModel) {
        return AiServices.create(CaseStatusWriter.class, chatModel);
    }

    /**
     * The Case Chat: the first agent here that is more than one line, because it is the first with
     * tools and a memory.
     *
     * <p>The memory is keyed by Case identifier, which is also what every tool receives as its
     * {@code @ToolMemoryId}. One key does both jobs: it is what makes the conversation resume where
     * it left off, and what stops the model addressing a Case the handler does not have open.
     *
     * <p>Twenty messages is a window, not a transcript. It is lost on restart like every other store
     * here; what a Case Handler reads back is {@code CaseChatStore}, which is a different thing for
     * a different reader.
     */
    @Bean
    CaseChatAgent caseChatAgent(ChatModel chatModel, CaseChatTools tools) {
        return UnfinishedTasks.wire(
                CaseChatAgent.class,
                WorkshopTask.CHAT,
                CaseChatAgent.IMPLEMENTED,
                () -> AiServices.builder(CaseChatAgent.class)
                        .chatModel(chatModel)
                        .tools(tools)
                        .chatMemoryProvider(caseId -> MessageWindowChatMemory.withMaxMessages(20))
                        .build());
    }

    /** The document reader: one file, one question, no case context at all. */
    @Bean
    DocumentReader documentReader(ChatModel chatModel) {
        return AiServices.create(DocumentReader.class, chatModel);
    }
}
