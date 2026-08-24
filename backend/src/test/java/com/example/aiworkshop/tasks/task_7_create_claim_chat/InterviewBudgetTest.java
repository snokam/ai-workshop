package com.example.aiworkshop.tasks.task_7_create_claim_chat;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * The bound on a loop a model drives.
 *
 * <p>No model here, which is the point: whether the interview stops is decided by ordinary code and
 * can be asserted like any other rule. The prompt asking for two or three questions cannot be
 * asserted at all.
 */
class InterviewBudgetTest {

    private static final String TRANSCRIPT = "Description: something happened to my car";

    @Test
    void thereIsNothingToSayWhileThereIsBudgetLeft() {
        assertThat(InterviewBudget.withinBudget(TRANSCRIPT, 0)).isEqualTo(TRANSCRIPT);
        assertThat(InterviewBudget.withinBudget(TRANSCRIPT, InterviewBudget.ROUNDS - 1))
                .isEqualTo(TRANSCRIPT);
    }

    @Test
    void theLastRoundTellsTheAgentToDecide() {
        String bounded = InterviewBudget.withinBudget(TRANSCRIPT, InterviewBudget.ROUNDS);

        assertThat(bounded).startsWith(TRANSCRIPT).contains(InterviewBudget.LAST_ROUND);
    }

    /**
     * A claimant who somehow answered more rounds than the budget allows is still past it. Written
     * because {@code ==} is the tempting comparison and it is wrong: one round over and the interview
     * would go back to asking.
     */
    @Test
    void beingWellPastTheBudgetIsStillPastIt() {
        assertThat(InterviewBudget.withinBudget(TRANSCRIPT, InterviewBudget.ROUNDS + 3))
                .contains(InterviewBudget.LAST_ROUND);
    }
}
