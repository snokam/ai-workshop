package com.example.aiworkshop.tasks.task_7_create_claim_chat.agent;

import com.example.aiworkshop.tasks.task_7_create_claim_chat.model.InterviewTurn;
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
            TODO — task 8, part 1. Write the interviewer.

            An interview instead of a form. It reads a transcript and either asks for what is missing or
            decides.

              next(@V("scenarios") String scenarios, @UserMessage String transcript)

            It returns an InterviewTurn: a Decision of NEEDS_INFO or DECIDED, the questions to ask, the
            scenario it settled on, a confidence and a rationale.

            The hard part is when to stop asking. Too eager and it opens the wrong claim; too cautious and it
            interrogates somebody who has already said enough. Two or three questions is usually the whole
            budget before a person gives up.

            There is no scenario for "something else", so when nothing fits it must say so rather than force a
            poor match.
            """)
    @UserMessage("The conversation so far:\n\n{{transcript}}")
    InterviewTurn next(@V("scenarios") String scenarios, @V("transcript") String transcript);
}
