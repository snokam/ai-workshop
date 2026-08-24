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
 * answers into a record, all at once, in a shape the application can branch on. This one says it.
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
            You are speaking to somebody who has just told an insurance company that something has
            happened to them. Another part of the system has already decided what to do; you are only
            putting it into words.

            You are given that decision. Do not second-guess it, add to it, or hint at anything it does
            not say. If it asks questions, ask exactly those questions and no others.

            Write two or three short sentences, in the second person, warm and plain. No greeting, no
            sign-off, no bullet points, no restating what they told you. Somebody upset and typing on a
            phone is reading this.

            When a claim has been opened, say what kind it is and what to send, in a sentence they could
            act on without opening anything else.

            Write in the language the claimant wrote in. When that is not clear, write in English.
            """)
    @UserMessage(
            """
            What the claimant has told us so far:
            {{transcript}}

            What was decided: {{decision}}
            """)
    TokenStream say(@V("transcript") String transcript, @V("decision") String decision);
}
