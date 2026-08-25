package com.example.aiworkshop.tasks.task_7_streaming_form_help.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * What a claimant reads while their claim is being opened.
 *
 * <p>This is the whole reason streaming is here. Everything else in the workshop answers a question
 * somebody has finished asking and then waits to be read, so streaming its reply only changes what
 * the waiting looks like — and on a reasoning model not even that, because it thinks first and emits
 * everything at once.
 *
 * <p>Here somebody is genuinely waiting. They have pressed the button, the classifier is deciding
 * what kind of claim to open, and that takes a couple of seconds during which the screen has nothing
 * to say. This fills them, with the one thing worth saying at that moment: was what you wrote enough
 * to work with, and if not, what is missing.
 *
 * <p>Two earlier versions of this were worse and both failed the same way. One ran an interview,
 * question by question, which is worse than a form somebody can see whole. One read the box while
 * they were still typing, which interrupts the writing it is meant to help. Neither was waiting for
 * anything — and streaming into a wait that does not exist is decoration.
 *
 * <p>It returns a {@link TokenStream} and the classifier in task 1 returns a record, which is the
 * distinction worth keeping: one answer is for a person to read as it arrives, the other is for a
 * program to branch on and has to be whole before it means anything. They cannot be the same call —
 * and here they are not even sequential, because both start the moment the button is pressed.
 */
public interface ClaimFormHelper {

    @SystemMessage(
            """
            Somebody has just described, in their own words, something that has happened to them, and
            pressed the button to open an insurance claim. It is being opened now. You are telling them
            whether what they wrote will be enough to work with.

            These are the situations a claim can be opened as, and what each needs:

            {{scenarios}}

            Two short sentences, and be straight with them.

            When the description is good, say so and say what made it good — that they said what
            happened, when, and to what. Do not pad it out with advice they do not need.

            When it is thin, say that plainly and name the single most useful thing missing: a date, a
            reference number, what was damaged, where it happened. One thing, the most useful one, not
            a list.

            Do not ask them a question — they cannot answer you, the claim is already being opened.
            Do not say whether anything is covered and do not promise an outcome. Do not tell them to
            do anything with the form they have just sent.

            Second person, warm and plain. No greeting, no sign-off, no lists.

            Write in the language they wrote in. When that is not clear, write in English.
            """)
    @UserMessage(
            """
            What they wrote:
            {{sofar}}
            """)
    TokenStream helpWith(@V("scenarios") String scenarios, @V("sofar") String soFar);
}
