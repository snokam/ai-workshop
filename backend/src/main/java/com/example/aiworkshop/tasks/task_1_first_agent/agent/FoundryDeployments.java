package com.example.aiworkshop.tasks.task_1_first_agent.agent;

import java.util.Map;
import java.util.Set;

/**
 * What is deployed on the Foundry resource, and what to do with a name that is not one of them.
 *
 * <p>Given. Task 5 asks you to name a model, and these are the names it can be handed. Five
 * deployments of genuinely different size and price, so the measurement the task asks for has
 * something to measure.
 *
 * <p>The Gemini tiers resolve too, onto whichever deployment is nearest. That is a safety net, not
 * the way in: a solution written against Vertex runs here without being rewritten, and the
 * substitution is logged as a warning because the cost line printed beside it is then priced against
 * the model that was asked for rather than the one that answered.
 */
public final class FoundryDeployments {

    /** Deployed on ai-wshp-p. A name in here is served exactly as asked. */
    public static final Set<String> DEPLOYED =
            Set.of("gpt-5.6-luna", "claude-sonnet-4-6", "gpt-5.4-mini", "gpt-4o", "o4-mini");

    /** The Vertex names, each pointed at the closest thing Foundry serves. */
    private static final Map<String, String> NEAREST = Map.of(
            "gemini-2.5-flash-lite", "gpt-5.4-mini",
            "gemini-2.5-flash", "gpt-5.6-luna",
            "gemini-2.5-pro", "claude-sonnet-4-6");

    private FoundryDeployments() {}

    /**
     * The deployment to call for this name, or {@code null} when the name means nothing here and the
     * caller should fall back to whatever it is configured with.
     */
    public static String resolve(String modelName) {
        if (modelName == null) {
            return null;
        }
        return DEPLOYED.contains(modelName) ? modelName : NEAREST.get(modelName);
    }
}
