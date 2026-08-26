package com.example.aiworkshop.tasks.task_5_claim_summary_choosing_models.agent;

import com.example.aiworkshop.tasks.task_1_first_agent.agent.Models;
import com.example.aiworkshop.workshop.UnfinishedTasks;
import com.example.aiworkshop.workshop.WorkshopTask;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Task 5's wiring: two agents, two jobs of very different difficulty, and the question of whether
 * they need the same model.
 *
 * <p>Everything before this task has had one model, and never asked. Here there are two agents side
 * by side and the difference between their jobs is obvious once it is pointed at:
 *
 * <table border="1">
 *   <caption>the two jobs</caption>
 *   <tr><th></th><th>is given</th><th>has to</th></tr>
 *   <tr><td>{@link ClaimSummarizer}</td>
 *       <td>every document on the claim at once</td>
 *       <td>read across them, notice where they disagree, write prose</td></tr>
 *   <tr><td>{@link ClaimStatusWriter}</td>
 *       <td>facts already worked out — the status, what is outstanding</td>
 *       <td>put them in one sentence</td></tr>
 * </table>
 */
@Configuration
public class SummaryConfig {

    // TODO — task 5. Name the model each job runs on.
    //
    // Two strings. The whole of the task is which name goes where, and whether you can say why.
    //
    //   gemini-2.5-flash-lite   fastest and cheapest. Thinks less, and does not reliably keep a
    //                           format it was asked for.
    //   gemini-2.5-flash        what the rest of the workshop runs on. Thinks before it answers.
    //   gemini-2.5-pro          strongest, slowest, dearest.
    //
    // Measured on the status line while this was written:
    //
    //   gemini-2.5-flash        2.07s   87 tokens   "The motor claim is awaiting a police report to
    //                                                proceed."
    //   gemini-2.5-flash-lite   0.63s   64 tokens   "**Claim:** The motor claim is pending the
    //                                                arrival of the police report. **Next Move:** ..."
    //
    // Three times faster and cheaper, and it ignored "one short sentence" — markdown, two fragments,
    // in a line that is rendered as plain text. Both halves of that are real: the saving is not
    // imaginary and neither is the mess. So it is not "use the cheap one where you can". It is: this
    // job is easy enough for a smaller model IF you can get the format you need out of it. If you
    // cannot, tighten that agent's prompt until you can, or pay for the better one and know what you
    // are paying for.
    //
    //   cd backend && ./mvnw test -Pevaluate -Dtest=SummaryEvaluation
    //
    // prints what each choice cost — in tokens and in money — and scores the summary against a
    // rubric, so you can tell whether a cheaper model made the answer worse rather than only cheaper.
    public static final String READING_EVERY_DOCUMENT = "TODO";

    public static final String WRITING_THE_STATUS_LINE = "TODO";

    /**
     * Both agents wait on the one decision, because until it is made there is no model to build them
     * on. An unwritten task stops the feature it provides and nothing else: the claim screen says
     * which file to open, and everything around it keeps working.
     */
    @Bean
    ClaimSummarizer claimSummarizer(Models models) {
        if (!decided()) {
            return UnfinishedTasks.notWrittenYet(ClaimSummarizer.class, WorkshopTask.CLAIM_SUMMARY_CHOOSING_MODELS);
        }
        return AiServices.create(ClaimSummarizer.class, models.named(READING_EVERY_DOCUMENT));
    }

    @Bean
    ClaimStatusWriter claimStatusWriter(Models models) {
        if (!decided()) {
            return UnfinishedTasks.notWrittenYet(ClaimStatusWriter.class, WorkshopTask.CLAIM_SUMMARY_CHOOSING_MODELS);
        }
        return AiServices.create(ClaimStatusWriter.class, models.named(WRITING_THE_STATUS_LINE));
    }

    /** Whether both names have been filled in. A name still starting with TODO is not a model. */
    public static boolean decided() {
        return !READING_EVERY_DOCUMENT.startsWith("TODO") && !WRITING_THE_STATUS_LINE.startsWith("TODO");
    }
}
