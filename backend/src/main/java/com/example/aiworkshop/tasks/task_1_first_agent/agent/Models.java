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
 * tiers on Vertex, deployment names on Foundry. The names are not interchangeable, and asking one
 * provider for the other's model is an error rather than a near-enough substitution.
 *
 * <p>Which is why {@link #fastest()} is here at all. Task 5 asks you to type a model name, so it
 * has to know which provider it is running on. Task 7 does not care what the model is called, only
 * that it is small enough to stream usefully, so it asks the provider instead of naming one.
 */
public interface Models {

    /** A model with this name, for an agent whose method returns a record. */
    ChatModel named(String modelName);

    /** The same, for an agent whose method returns a {@code TokenStream}. */
    StreamingChatModel streamingNamed(String modelName);

    /** The name of the smallest model this provider serves. What task 7 streams from. */
    String fastest();
}
