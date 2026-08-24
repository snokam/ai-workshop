package com.example.aiworkshop.tasks.task_7_create_claim_chat;

import com.example.aiworkshop.workshop.TaskNotImplementedException;
import com.example.aiworkshop.workshop.WorkshopTask;

/**
 * How many times we are willing to ask, and what happens when we have asked enough.
 *
 * <p>Every agent before this one answered a single call. This one drives a loop: it asks, a person
 * answers, it reads the whole transcript and decides whether to ask again. Nothing in the loop
 * stops it — the interviewer's prompt says to keep to two or three questions, and a prompt is a
 * request. A model can ask a fourth, and a fifth, and the claimant who is being interrogated closes
 * the tab. That is not a prompt bug to tune away. A loop needs a bound, and a bound belongs in code.
 *
 * <p>The interesting part is not the counter. It is what you do when it runs out, and there is no
 * obvious answer:
 *
 * <ul>
 *   <li><b>Refuse</b>, and hand the claim to a person. Safe, and it throws away everything the
 *       claimant has already typed.
 *   <li><b>Open it anyway</b> on the agent's best guess. But on a turn where the agent asked for
 *       more there is no guess — the scenario comes back null, and inventing one here is exactly the
 *       thing the enum in {@code InterviewTurn} exists to prevent.
 *   <li><b>Change the question.</b> Tell the agent this is its last turn and it must decide with
 *       what it has. It still chooses the scenario, so nothing is invented, and the confidence it
 *       returns is honest about how much it was guessing.
 * </ul>
 *
 * <p>The third is what this class does, and it is worth noticing why it works: when you cannot
 * constrain what a model does, you can still constrain what it is shown. The bound is enforced by
 * rewriting the input, not by arguing with the output.
 */
public final class InterviewBudget {

    private InterviewBudget() {}

    /**
     * Rounds of follow-up questions a claimant is put through before the agent is made to decide.
     *
     * <p>Each round is one screen of questions and one set of answers, so this is a number about a
     * person's patience rather than about the model. Two is a form. Five is an interrogation.
     */
    public static final int ROUNDS = 2;

    /** Appended to the transcript on the final round. The agent reads it as part of the conversation. */
    public static final String LAST_ROUND =
            """

            This is the final round. Decide now, with what you have — ask nothing further. \
            Choose the scenario that fits best and say how sure you are; LOW is an honest answer \
            here and a wrong scenario opened confidently is not.""";

    /**
     * The transcript the agent is shown this turn, bounded.
     *
     * @param transcript the conversation so far
     * @param roundsAnswered how many rounds of questions the claimant has already answered
     */
    public static String withinBudget(String transcript, int roundsAnswered) {
        // TODO — task 7, part 2. Stop the interview asking forever.
        //
        // Return the transcript the agent is shown:
        //
        //   - while there is budget left, unchanged
        //   - on the last round, with LAST_ROUND appended
        //
        // "The last round" is when roundsAnswered has reached ROUNDS: the claimant has answered twice,
        // so this turn is the one that has to produce a claim.
        //
        // Two lines. The thinking is in the class comment above — read it before writing them, because
        // the code is obvious and the choice it encodes is not.
        //
        // Then try it. Open the AI chat screen and be deliberately vague: "something happened to my
        // car". Answer each round as unhelpfully as you can and watch where it stops. Then set ROUNDS
        // to 5 and do it again, and decide which of the two you would put in front of a real claimant.

        throw new TaskNotImplementedException(WorkshopTask.CREATE_CLAIM_CHAT);
    }
}
