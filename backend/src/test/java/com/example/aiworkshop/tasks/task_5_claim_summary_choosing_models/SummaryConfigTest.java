package com.example.aiworkshop.tasks.task_5_claim_summary_choosing_models;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.example.aiworkshop.tasks.task_5_claim_summary_choosing_models.agent.SummaryConfig;
import com.example.aiworkshop.tasks.task_5_claim_summary_choosing_models.agent.SummaryConfig.Job;
import dev.langchain4j.model.chat.ChatModel;
import org.junit.jupiter.api.Test;

/**
 * That a choice was made at all.
 *
 * <p>Deliberately not asserting which model goes where — that is the exercise, and a test that
 * encoded the answer would hand it over. What it does pin is that the two jobs are told apart: a
 * {@code modelFor} that returns the same model whatever it is asked has not decided anything, and it
 * is indistinguishable at runtime from the version this task started with.
 *
 * <p>No model is built here. Which model each job needs is a decision in ordinary code, which is why
 * it can be asserted without credentials or a call.
 */
class SummaryConfigTest {

    private final ChatModel best = mock(ChatModel.class);
    private final ChatModel cheaper = mock(ChatModel.class);

    @Test
    void theTwoJobsAreNotGivenTheSameModel() {
        ChatModel forSummary = SummaryConfig.modelFor(Job.READING_EVERY_DOCUMENT, best, cheaper);
        ChatModel forStatusLine = SummaryConfig.modelFor(Job.WRITING_THE_STATUS_LINE, best, cheaper);

        assertThat(forSummary)
                .describedAs("reading every document on a claim and writing one sentence from facts"
                        + " already worked out are not the same job — decide which model each needs")
                .isNotSameAs(forStatusLine);
    }

    @Test
    void bothJobsAreAnsweredWithOneOfTheModelsItWasHanded() {
        for (Job job : Job.values()) {
            assertThat(SummaryConfig.modelFor(job, best, cheaper))
                    .describedAs("modelFor chooses between the two it is given; it does not build one")
                    .isIn(best, cheaper);
        }
    }
}
