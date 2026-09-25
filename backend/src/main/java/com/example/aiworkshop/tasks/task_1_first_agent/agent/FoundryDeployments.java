package com.example.aiworkshop.tasks.task_1_first_agent.agent;

import java.util.Set;

/**
 * What is deployed on the Foundry resource.
 *
 * <p>Given. Task 5 asks you to name a model, and these are the names it can be handed — five
 * deployments of genuinely different size and price, so the measurement that task asks for has
 * something to measure.
 *
 * <p>A name that is not one of them is refused rather than quietly swapped for something near it.
 * That matters more here than it looks: {@code SummaryDesk} prints the cost of each call by looking
 * the price up under the name you asked for, so a silent substitution would print a real-looking
 * number for a model that never ran. In the one task built to teach measurement, a plausible wrong
 * number is worse than an error.
 */
public final class FoundryDeployments {

    /** Deployed on ai-wshp-p. Anything else is not a model as far as this application is concerned. */
    public static final Set<String> DEPLOYED =
            Set.of("gpt-5.6-luna", "claude-sonnet-4-6", "gpt-5.4-mini", "gpt-4o", "o4-mini");

    private FoundryDeployments() {}

    /** The name itself when it is deployed here, or an exception naming the ones that are. */
    public static String require(String modelName) {
        if (DEPLOYED.contains(modelName)) {
            return modelName;
        }
        throw new IllegalArgumentException("'%s' is not deployed on Foundry. Deployed: %s"
                .formatted(modelName, DEPLOYED.stream().sorted().toList()));
    }
}
