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

    /**
     * Reading across a folder of documents, noticing where two of them disagree, and writing prose
     * about it is the hardest thing any agent in this workshop does — so it gets the model that is
     * best at exactly that, from a different vendor than the rest of the resource.
     *
     * <p>A summary is written once per claim and then cached, so the price is paid on first open and
     * never again. That is the shape of job worth spending on.
     */
    public static final String READING_EVERY_DOCUMENT = "claude-sonnet-4-6";

    /**
     * One sentence from facts that are already worked out. Nothing here needs reading across
     * anything, so the default deployment is enough and no case has been made for more.
     *
     * <p>The instinct is to reach for the oldest and smallest name on the list, and on this resource
     * that instinct is wrong: gpt-4o is twelve times the input price of gpt-5.6-luna. Age is not
     * price and size is not price — {@link com.example.aiworkshop.tasks.task_5_claim_summary_choosing_models.ModelPrices}
     * is, and the log line under every call is the number that settles it.
     *
     * <p>gpt-5.4-mini is genuinely cheaper again and answers in a third of the time, and it also
     * ignores "one short sentence" — markdown, two fragments, in a line rendered as plain text. Both
     * halves of that are real. The saving is not imaginary and neither is the mess, so the choice is
     * not "use the cheap one where you can" but "this job is small enough for a smaller model IF you
     * can get the format you need out of it".
     */
    public static final String WRITING_THE_STATUS_LINE = "gpt-5.6-luna";

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
