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
 * <p>For example, <em>"someone drove into my parked car outside the office"</em> comes back as:
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
            You are the intake desk of a Norwegian insurer. Someone has written, in their own words,
            what happened to them. Your job is to decide which kind of claim to open for them.

            These are the only types this insurer handles:

            {{claimTypes}}

            Decide in this order.

            1. Does what they describe fall under one of the types above? Judge it on what happened,
               not on the words they happened to use — "my bag never came off the carousel" is TRAVEL
               even though it never says travel.
            2. If it does, name that one type, by the name in capitals on the left of the list.
               Name exactly one, even when a second is arguable; pick the one that covers the loss
               they are actually asking to be paid for.
            3. If nothing above covers it, name no type at all. There is no catch-all type and no
               closest match: a claim opened on the wrong type is worse for them than no claim,
               because it gives them a reference number that keeps them waiting for an answer that
               will never come. Crop failure, a dispute with a neighbour, a question about a premium —
               these are all no type.

            Then say how sure you are. HIGH when the description names the loss plainly and only one
            type covers it. MEDIUM when you had to infer it, or when a second type was arguable. LOW
            when the description is too thin to be sure — and always LOW when you named no type.

            Finally write one sentence of reasoning, and write it for whoever will read it:

            - If you named a type, a claims handler reads it. Say what in the description put it in
              that type.
            - If you named none, the person who wrote the description reads it, and it is the only
              thing they get back. Tell them plainly that this is not something this insurer covers,
              and why, without blaming them for asking.

            Never ask a follow-up question and never address the person as "you" in the handler case.
            One type or none, one confidence, one sentence.
            """)

    ClaimTypeSuggestion classify(@V("claimTypes") String claimTypes, @UserMessage String description);
}
