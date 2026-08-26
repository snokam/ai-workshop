package com.example.aiworkshop.tasks.task_1_first_agent.agent;

import com.example.aiworkshop.tasks.task_1_first_agent.model.ClaimTypeSuggestion;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * The intake agent for a whole Claim: the first thing that reads what a Claimant typed when they said
 * what they needed help with, and decides which {@link ClaimType} to open for them.
 *
 * <p>This interface <em>is</em> the agent — LangChain4j builds the implementation from the system
 * message and the shape of {@link ClaimTypeSuggestion}. The set of types is not written into the
 * prompt by hand; {@link ClaimType#catalog()} is rendered in through the {@code {{claimTypes}}}
 * variable, so the one list the agent chooses from is the same enum the Claim is created from.
 *
 * <p>The Claimant's own words are the {@link UserMessage}. Nothing else is: the description is
 * untrusted free text, and keeping the catalogue in the system message keeps that text from
 * competing with the instructions for the model's attention.
 *
 * <h2>An example</h2>
 *
 * <em>"someone drove into my parked car outside the office"</em> comes back as:
 *
 * <pre>{@code
 * new ClaimTypeSuggestion(ClaimType.MOTOR, MatchConfidence.HIGH,
 *         "The description involves damage to a motor vehicle.")
 * }</pre>
 *
 * <p>{@code ClaimIntake} opens a motor claim from that. When nothing fits — <em>"my crops failed
 * after a drought"</em> — the type is {@code null} and the rationale is all the person sees.
 */
public interface ClaimTypeClassifier {


    @SystemMessage(
            """
            TODO — task 1, part 2. Write the agent.

            You are writing the system message. The method signature below is already the contract:

              classify(@V("claimTypes") String claimTypes, @UserMessage String description)

            {{claimTypes}} renders the catalogue in — ClaimType.catalog() builds it, one line per type with its
            name, label and description. The description is what the person typed, and it is the user turn.

            A system message is only text with the variable dropped into it. The smallest one that runs:

              You sort insurance claims. The types you can choose from are:

              {{claimTypes}}

              Pick the one that fits what the person describes, and say how sure you are.

            Start from something like that and it will answer — and it will also force a match on a
            description none of the types cover, because "pick the one that fits" never said it could
            decline. That is the gap the rest of this list closes.

            Yours has to make it:
              1. choose exactly one type from the list it is shown, by name
              2. say how sure it is — HIGH, MEDIUM or LOW
              3. give one sentence of reasoning

            That is the shape of ClaimTypeSuggestion, the record it returns. Read it: the @Description on each
            component is part of the prompt too.

            Two things are easy to miss. There is no claim type for "something else", so when nothing fits it
            must name no type at all rather than force the closest one. And the rationale has two readers —
            name a type and a handler reads it, name none and the claimant does, because it is all they see.
            """)

    ClaimTypeSuggestion classify(@V("claimTypes") String claimTypes, @UserMessage String description);
}
