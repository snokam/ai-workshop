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

@Configuration
class AiServiceConfig {
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

    @Bean
    CaseSummarizer caseSummarizer(ChatModel chatModel) {
        return UnfinishedTasks.wire(
                CaseSummarizer.class,
                WorkshopTask.SUMMARY,
                CaseSummarizer.IMPLEMENTED,
                () -> AiServices.create(CaseSummarizer.class, chatModel));
    }

    @Bean
    CaseStatusWriter caseStatusWriter(ChatModel chatModel) {
        return AiServices.create(CaseStatusWriter.class, chatModel);
    }

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

    @Bean
    DocumentReader documentReader(ChatModel chatModel) {
        return AiServices.create(DocumentReader.class, chatModel);
    }
}
