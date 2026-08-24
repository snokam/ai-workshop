package com.example.aiworkshop.tasks.task_7_create_case_chat.model;

import com.example.aiworkshop.tasks.task_1_first_agent.model.MatchConfidence;
import dev.langchain4j.model.output.structured.Description;
import java.util.List;

/**
 * One move by the intake interviewer (task 7): either a request for more information, or a decision.
 * This record is the agent's output schema — LangChain4j derives the JSON contract from it, so the
 * shape here is what lets a single agent do two different things in one call.
 *
 * <p>There is no true union in the schema, so the two outcomes share one record and {@link #decision}
 * says which fields matter: {@link Decision#NEEDS_INFO} fills {@link #questions} and leaves {@link
 * #scenario} null; {@link Decision#DECIDED} fills {@link #scenario} and leaves {@link #questions}
 * empty. Constraining {@link #scenario} to the {@link CaseScenario} enum is what stops the agent
 * inventing a scenario with no checklist behind it.
 *
 * @param decision whether the agent is asking or committing
 * @param questions the follow-up questions to put to the claimant; empty unless asking
 * @param scenario the scenario the case is opened as; null unless deciding
 * @param confidence how sure the agent is — HIGH when the situation is plain, LOW when it guessed
 * @param rationale one plain sentence: what was decided, or what is still missing
 */
public record InterviewTurn(
        @Description("NEEDS_INFO to ask the claimant more; DECIDED once one scenario clearly fits.")
                Decision decision,
        @Description("One to three short, plain questions to ask the claimant. Empty unless NEEDS_INFO.")
                List<String> questions,
        @Description("The scenario the description best fits. Null unless DECIDED.") CaseScenario scenario,
        @Description("How sure you are of the decision: HIGH, MEDIUM or LOW.") MatchConfidence confidence,
        @Description("One plain sentence: what was decided, or what is still missing.") String rationale) {

    /** Whether this turn asks the claimant for more, or commits to a scenario. */
    public enum Decision {
        NEEDS_INFO,
        DECIDED
    }
}
