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
    boolean IMPLEMENTED = true;


    @SystemMessage(
            """
            You are the intake agent in a case-handling system. Someone has just written, in their
            own words, what they need help with, and you are the first to read it. Your one job is to
            decide which kind of case to open for them.

            Choose exactly one of these case types:

            {{caseTypes}}

            Pick the single type that best fits what the person described. If none of the specific
            types fit — the description is off-topic, too vague to place, or about something the list
            does not cover — choose OTHER rather than forcing the closest match. Say how sure you
            are: HIGH when the description plainly is one kind of case, LOW when you fell back to
            OTHER or had little to go on.

            Do not ask the person for more information and do not address them. Write the rationale as
            one plain, factual sentence about why the type fits, in English, whatever language the
            description is written in.
            """)
    CaseTypeSuggestion classify(@V("caseTypes") String caseTypes, @UserMessage String description);
}
