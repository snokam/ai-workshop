package com.example.aiworkshop.tasks.task_1_first_agent.agent;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;

/**
 * Builds a model by name, so a task can pick one without knowing who is serving it.
 *
 * <p>Given. Task 1 builds the one model the workshop runs on, and tasks 2, 3, 4 and 6 all use that
 * one. Two tasks need something else: task 5, where choosing the model is the exercise, and task 7,
 * which needs a streaming model rather than a stronger one.
 *
 * <p>It is an interface because there are two providers, each with its own model names — Gemini
 * tiers on Vertex, deployment names on Foundry. Either vocabulary works on either provider: a name
 * the provider does not serve is resolved to its nearest local equivalent, and the substitution is
 * logged as a warning, because the cost printed beside the answer is priced against the name that
 * was asked for.
 */
public interface Models {

    /** A model with this name, for an agent whose method returns a record. */
    ChatModel named(String modelName);

    /** The same, for an agent whose method returns a {@code TokenStream}. */
    StreamingChatModel streamingNamed(String modelName);
}
