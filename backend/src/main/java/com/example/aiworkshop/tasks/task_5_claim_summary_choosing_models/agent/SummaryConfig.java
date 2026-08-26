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
     * Reading every document on a claim and holding them against each other is the harder of the two
     * jobs, so it gets the model that thinks before it answers.
     */
    public static final String READING_EVERY_DOCUMENT = "gemini-2.5-flash";

    /**
     * Putting facts somebody else worked out into one sentence is easy enough for the small model —
     * but only once the prompt is strict enough to hold it there. Left loose it answers in markdown,
     * in a line that is rendered as plain text.
     */
    public static final String WRITING_THE_STATUS_LINE = "gemini-2.5-flash-lite";


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
