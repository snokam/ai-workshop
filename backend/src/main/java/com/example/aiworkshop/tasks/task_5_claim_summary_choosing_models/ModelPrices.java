package com.example.aiworkshop.tasks.task_5_claim_summary_choosing_models;

import dev.langchain4j.model.output.TokenUsage;
import java.util.Locale;
import java.util.Map;

/**
 * What a call costs, so task 5 can print money rather than only tokens.
 *
 * <p>Given. Published prices per million tokens, text input, for the standard global deployment of
 * each model. They will go stale — that is why they are in one block with a date on it rather than
 * scattered through the code. If a number here looks wrong, it probably is; go and read the page.
 *
 * <p>How firm each number is differs, and it is worth knowing which is which before quoting one:
 *
 * <table border="1">
 *   <caption>where these came from</caption>
 *   <tr><th>models</th><th>checked</th><th>against</th></tr>
 *   <tr><td>gemini-*</td><td>26 August 2026</td>
 *       <td><a href="https://ai.google.dev/gemini-api/docs/pricing">Google's pricing page</a></td></tr>
 *   <tr><td>gpt-4o, o4-mini</td><td>25 September 2026</td>
 *       <td>Azure's retail price API, which is the billing meter itself</td></tr>
 *   <tr><td>gpt-5.6-luna, claude-sonnet-4-6</td><td>25 September 2026</td>
 *       <td>vendor pricing pages, agreeing with each other</td></tr>
 *   <tr><td>gpt-5.4-mini</td><td>25 September 2026</td>
 *       <td>third-party trackers only — Azure publishes no line item. Treat as approximate</td></tr>
 * </table>
 *
 * <p>The arithmetic in {@link #dollarsFor} is the interesting part, and it is not
 * {@code input + output}. A reasoning model bills its thinking at the <em>output</em> rate, and
 * thinking tokens appear in neither the prompt nor the answer — they show up only in the gap between
 * {@code totalTokenCount} and the two you can see. So everything that is not input is charged as
 * output, whether or not you ever read it.
 */
public final class ModelPrices {

    /** Dollars per million tokens. */
    public record Price(double perMillionIn, double perMillionOut) {}

    private static final Map<String, Price> PUBLISHED = Map.of(
            "gemini-2.5-flash-lite", new Price(0.10, 0.40),
            "gemini-2.5-flash", new Price(0.30, 2.50),
            "gemini-2.5-pro", new Price(1.25, 10.00),
            "gpt-5.6-luna", new Price(0.20, 1.20),
            "gpt-5.4-mini", new Price(0.75, 4.50),
            "o4-mini", new Price(1.10, 4.40),
            "gpt-4o", new Price(2.50, 10.00),
            "claude-sonnet-4-6", new Price(3.00, 15.00));

    private ModelPrices() {}

    /**
     * What one call cost, or {@code -1} when the model is not in the table above.
     *
     * <p>Charged output is {@code total - input}, not {@code outputTokenCount}: the difference is the
     * thinking, which is billed at the output rate and is invisible everywhere else.
     */
    public static double dollarsFor(String modelName, TokenUsage usage) {
        Price price = PUBLISHED.get(modelName);
        if (price == null || usage == null || usage.totalTokenCount() == null) {
            return -1;
        }
        int in = usage.inputTokenCount() == null ? 0 : usage.inputTokenCount();
        int billedOut = Math.max(0, usage.totalTokenCount() - in);
        return (in * price.perMillionIn() + billedOut * price.perMillionOut()) / 1_000_000;
    }

    /** How many tokens were billed as output but never shown: the thinking. */
    public static int hiddenThinking(TokenUsage usage) {
        if (usage == null || usage.totalTokenCount() == null) {
            return 0;
        }
        int in = usage.inputTokenCount() == null ? 0 : usage.inputTokenCount();
        int out = usage.outputTokenCount() == null ? 0 : usage.outputTokenCount();
        return Math.max(0, usage.totalTokenCount() - in - out);
    }

    /**
     * A price as dollars, with a decimal point wherever it is read.
     *
     * <p>{@code Locale.ROOT} rather than the default: on a Norwegian machine {@code %.6f} formats
     * $0.000635 as "$0,000635", which reads as a different number entirely.
     */
    public static String asDollars(double dollars) {
        return dollars < 0 ? "no published price" : String.format(Locale.ROOT, "$%.6f", dollars);
    }

    /** True when we have a published price for this model. */
    public static boolean known(String modelName) {
        return PUBLISHED.containsKey(modelName);
    }
}
