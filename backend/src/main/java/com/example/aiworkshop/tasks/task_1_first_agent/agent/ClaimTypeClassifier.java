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
            You are the intake agent in a claim-handling system. Someone has just written, in their
            own words, what they need help with, and you are the first to read it. Your one job is to
            decide which kind of claim to open for them.

            Choose exactly one of these claim types:

            {{claimTypes}}

            Pick the single type that best fits what the person described. If none of the specific
            types fit — the description is off-topic, too vague to place, or about something the list
            does not cover — leave the type empty rather than forcing the closest match.

            Say how sure you are: HIGH when the description plainly is one kind of claim, LOW when you
            named no type or had little to go on.

            The rationale has two readers, and which one depends on your answer.

            When you name a type, a claim handler reads it. One plain, factual sentence about why that
            type fits, in English whatever language the description is written in. Do not address the
            person and do not ask them for more information.

            When you name no type, the person who typed it reads it, and it is the only thing they
            will see. One short sentence, written to them, saying plainly that this is not something
            we insure. In the language they wrote in. Not "the description does not fit any of the
            specified types" — that is a note to yourself, and they are the ones being turned away.
            """)

    ClaimTypeSuggestion classify(@V("claimTypes") String claimTypes, @UserMessage String description);
}
