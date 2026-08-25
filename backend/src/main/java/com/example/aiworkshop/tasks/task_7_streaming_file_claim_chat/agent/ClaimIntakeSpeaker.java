package com.example.aiworkshop.tasks.task_7_streaming_file_claim_chat.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * The voice of the interview: what the claimant actually reads, arriving a word at a time.
 *
 * <p>There are two agents in this task and only one of them is ever seen. {@link
 * ClaimIntakeInterviewer} decides — which scenario, or which questions are still missing — and it
 * answers into a record, all at once, in a shape the application can branch on. This one talks to the
 * person while that happens.
 *
 * <p><b>They run at the same time, and that is the point.</b> This one is not told what was decided
 * and does not wait for it: the screen starts both at once, so the two seconds the decision takes are
 * two seconds of something being written to the claimant rather than two seconds of a spinner. An
 * earlier version ran this one second, with the decision in hand — it was smooth, it was accurate, and
 * it was worthless, because it arrived after the questions were already on screen and said the same
 * thing.
 *
 * <p><b>Why they cannot be the same call.</b> A method returning {@code InterviewTurn} has no answer
 * until the last token has arrived, because half a JSON object is not an object. A method returning
 * {@link TokenStream} never has a whole answer to give back — it hands you pieces as they come. One
 * call cannot do both, and the choice is not a preference: it is whether the reply is for a program
 * or for a person.
 *
 * <p>So the flow costs two calls per turn. That is the price of the claimant seeing something happen
 * while a model thinks, and it is worth knowing you are paying it rather than discovering it on a
 * bill. The decision call is the small one — a handful of tokens into a fixed schema. This one writes
 * prose and is read by a human at reading speed, which is exactly the call worth streaming.
 */
public interface ClaimIntakeSpeaker {

    @SystemMessage(
            """
            Somebody has just told an insurance company that something has happened to them, and is
            waiting while it is worked out what to do about it. You are what they read during that wait.

            Say back what you understood, in one or two short sentences, and then that you are checking
            what will be needed. Nothing else.

            Do not ask them anything — questions are being decided elsewhere and will arrive under what
            you write. Do not say whether anything is covered, do not promise an outcome, do not
            speculate about what kind of claim it is. If you cannot tell what happened, say that you are
            looking at what they have sent rather than guessing at it.

            Second person, warm and plain. No greeting, no sign-off. Somebody upset and typing on a phone
            is reading this.

            Write in the language they wrote in. When that is not clear, write in English.
            """)
    @UserMessage(
            """
            What they have told us so far:
            {{transcript}}
            """)
    TokenStream say(@V("transcript") String transcript);
}
