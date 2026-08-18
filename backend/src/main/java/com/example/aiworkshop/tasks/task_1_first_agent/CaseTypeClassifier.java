package com.example.aiworkshop.tasks.task_1_first_agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import com.example.aiworkshop.cases.CaseTypeSuggestion;

/**
 * The intake agent for a whole Case: the first thing that reads what a Claimant typed when they said
 * what they needed help with, and decides which {@link CaseType} to open for them.
 *
 * <p>This interface <em>is</em> the agent — LangChain4j builds the implementation from the system
 * message and the shape of {@link CaseTypeSuggestion}. The set of types is not written into the
 * prompt by hand; {@link CaseType#catalog()} is rendered in through the {@code {{caseTypes}}}
 * variable, so the one list the agent chooses from is the same enum the Case is created from.
 *
 * <p>The Claimant's own words are the {@link UserMessage}. Nothing else is: the description is
 * untrusted free text, and keeping the catalogue in the system message keeps that text from
 * competing with the instructions for the model's attention.
 */
public interface CaseTypeClassifier {
    /**
     * ── TASK FIRST AGENT ────────────────────────────────────────────────────────────────────────
     * Set to true once you have written the case type classifier below. While it is false the
     * application still runs: every screen that does not need this agent works as normal,
     * and the one that does explains which file to open.
     * ──────────────────────────────────────────────────────────────────────────────────
     */
    boolean IMPLEMENTED = false;


    @SystemMessage(
            """
            TODO — task 1.

            Write the system message for an agent that reads what someone typed when they said what
            they needed help with, and decides which kind of case to open for them.

            The list of types it may choose from is rendered in through {{caseTypes}}. It has to
            choose exactly one, say how sure it is, and give one plain sentence of reasoning — which
            is the shape of CaseTypeSuggestion, the record this returns.

            The solutions branch has the version this was written from.
            """)
    CaseTypeSuggestion classify(@V("caseTypes") String caseTypes, @UserMessage String description);
}
