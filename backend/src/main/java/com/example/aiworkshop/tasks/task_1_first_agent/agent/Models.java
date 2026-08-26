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
 * <p>It is an interface because there are two providers. Vertex is the default and takes a Gemini
 * model name. Snokam staff run on Foundry, which has one deployment and no tiers to choose between,
 * so its implementation ignores the name and says so in the log. Without this, a task naming
 * "gemini-2.5-pro" would build a Vertex model on a machine configured for Foundry — and the person
 * running it would have no idea.
 */
public interface Models {

    /** A model with this name, for an agent whose method returns a record. */
    ChatModel named(String modelName);

    /** The same, for an agent whose method returns a {@code TokenStream}. */
    StreamingChatModel streamingNamed(String modelName);
}
