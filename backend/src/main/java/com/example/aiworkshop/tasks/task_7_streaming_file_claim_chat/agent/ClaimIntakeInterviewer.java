package com.example.aiworkshop.tasks.task_7_streaming_file_claim_chat.agent;

import com.example.aiworkshop.tasks.task_7_streaming_file_claim_chat.model.InterviewTurn;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * The conversational intake agent: like the one-shot classifier of task 1, but allowed to ask before
 * it commits. It reads the conversation so far and returns a single {@link InterviewTurn} — either
 * the questions it still needs answered, or the scenario it has settled on.
 *
 * <p>This interface <em>is</em> the agent. LangChain4j builds the implementation from the system
 * message and the shape of {@link InterviewTurn}. The scenarios are not written into the prompt by
 * hand: {@code ClaimScenario.catalog()} is rendered in through {@code {{scenarios}}}, so the taxonomy
 * the agent reasons over is the same enum the Claim is opened from.
 *
 * <p>Stateless by design: the whole conversation is handed in as {@code {{transcript}}} each turn, so
 * the agent holds no memory between calls and the caller owns the history.
 */
public interface ClaimIntakeInterviewer {

    @SystemMessage(
            """
            You are the intake agent in a claim-handling system. Someone has written, in their own
            words, what they need help with. Your job is to open the right claim for them — but unlike
            a plain classifier, you may ask a few follow-up questions first when the answer would
            change which scenario applies.

            The scenarios you can open, grouped by the kind of insurance:

            {{scenarios}}

            Read the whole conversation so far and make one move:

            - NEEDS_INFO — you cannot yet tell which scenario fits, and a short answer would settle
              it. Give one to three plain questions, each asking exactly one thing. Ask only what
              changes which scenario applies: if two scenarios need different documents and you cannot
              yet tell them apart, that is what to ask about. Leave the scenario empty.

            - DECIDED — one scenario clearly fits. Return it, with the confidence you have: HIGH when
              the situation is plain, LOW when you are largely guessing.

            Be economical. Never ask about something the person has already told you, and never ask
            more than three questions in total across the whole conversation — if the transcript
            already shows you asked before, lean towards deciding now. If nothing fits even after
            asking, say so rather than forcing a poor match — there is no scenario for
            "something else", and opening the wrong kind of claim is worse than opening none.

            Address the claimant directly and plainly in the questions. Write the rationale as one
            factual sentence, in English, whatever language the conversation is written in.
            """)
    @UserMessage("The conversation so far:\n\n{{transcript}}")
    InterviewTurn next(@V("scenarios") String scenarios, @V("transcript") String transcript);
}
