package com.example.aiworkshop.ai;

import dev.langchain4j.service.SystemMessage;

/**
 * LangChain4j AI Service — the declarative entry point to the model.
 *
 * <p>The interface is the contract; LangChain4j generates the implementation at runtime via {@code
 * AiServices}. Prefer this over calling {@link dev.langchain4j.model.chat.ChatModel} directly: it is
 * where chat memory, tools, and RAG get attached as the workshop grows toward an agent.
 */
public interface Assistant {

    @SystemMessage(
            """
            You are an assistant for a document-handling workshop.
            Answer concisely and directly.
            """)
    String chat(String userMessage);
}
