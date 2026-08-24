package com.example.aiworkshop.tasks.task_1_first_agent.agent;

import com.example.aiworkshop.tasks.task_1_first_agent.model.CaseTypeSuggestion;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

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


    @SystemMessage(
            """
            TODO — task 1, part 2. Write the agent.

            You are writing the system message. The method signature below is already the contract:

              classify(@V("caseTypes") String caseTypes, @UserMessage String description)

            {{caseTypes}} renders the catalogue in — CaseType.catalog() builds it, one line per type with its
            name, label and description. The description is what the person typed, and it is the user turn.

            The prompt has to make it:
              1. choose exactly one type from the list it is shown, by name
              2. say how sure it is — HIGH, MEDIUM or LOW
              3. give one sentence of reasoning

            That is the shape of CaseTypeSuggestion, the record it returns. Read it: the @Description on each
            component is part of the prompt too.

            Two things are easy to miss. There is no case type for "something else", so when nothing fits it
            must name no type at all rather than force the closest one. And the rationale has two readers —
            name a type and a handler reads it, name none and the claimant does, because it is all they see.
            """)

    CaseTypeSuggestion classify(@V("caseTypes") String caseTypes, @UserMessage String description);
}
