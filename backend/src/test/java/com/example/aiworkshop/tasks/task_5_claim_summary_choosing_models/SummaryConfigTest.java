package com.example.aiworkshop.tasks.task_5_claim_summary_choosing_models;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.aiworkshop.tasks.task_5_claim_summary_choosing_models.agent.SummaryConfig;
import org.junit.jupiter.api.Test;

/**
 * Checks that a choice was made, not which one.
 *
 * <p>There is no right answer to assert. Reading every document on a claim and writing one sentence
 * from facts already worked out are different jobs, so giving them the same model means the question
 * was not asked — but which way round they go is yours to argue, and the evaluation is where you find
 * out whether you were right.
 */
class SummaryConfigTest {

    @Test
    void bothJobsHaveBeenGivenAModel() {
        assertThat(SummaryConfig.decided())
                .describedAs("name a model for each job in SummaryConfig — both still say TODO")
                .isTrue();
    }

    @Test
    void theTwoJobsAreNotGivenTheSameModel() {
        assertThat(SummaryConfig.READING_EVERY_DOCUMENT)
                .describedAs("reading every document on a claim and writing one sentence from facts"
                        + " already worked out are not the same job — decide which model each needs")
                .isNotEqualTo(SummaryConfig.WRITING_THE_STATUS_LINE);
    }

    @Test
    void theModelsChosenHaveAPublishedPrice() {
        for (String model : new String[] {SummaryConfig.READING_EVERY_DOCUMENT, SummaryConfig.WRITING_THE_STATUS_LINE}) {
            assertThat(ModelPrices.known(model))
                    .describedAs("%s is not a model ModelPrices has a price for — check the spelling,"
                            + " or add it there if you meant to try something else", model)
                    .isTrue();
        }
    }
}
