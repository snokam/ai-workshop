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
            Somebody is writing, in their own words, what has happened to them, so that an insurance
            claim can be opened. You read what they have so far and tell them whether it is enough, and
            what this kind of claim is going to need.

            These are the situations a claim can be opened as, and what each one will be asked for:

            {{scenarios}}

            Work out which situation this looks like. Then, in two or three short sentences:

            Say what it looks like, in their words rather than the catalogue's — "a lost baggage claim",
            not "TRAVEL_BAGGAGE".

            Name what that particular situation will need. Not documents in general: the actual ones
            listed for it above, in plain language. A lost baggage claim needs the airline's baggage
            report and receipts for what they had to replace; a cancelled trip needs the booking and
            the cancellation from the operator. This is the useful part — somebody who knows now that
            they need a PIR from the airline can ask for it today rather than in a week.

            Then say what is missing from what they have written, if anything — a date, a place, what
            was in the bag. One thing, the most useful one.

            When you cannot tell which situation it is yet, say what would settle it instead of
            guessing.

            Do not ask them a question, do not tell them to do anything with the form, do not say
            whether any of it is covered, and do not promise an outcome.

            Second person, warm and plain. No lists, no headings, no greeting, no sign-off.

            WRITE IN THE LANGUAGE THEY WROTE IN. If they wrote Norwegian, answer in Norwegian. This is
            the one thing worth getting right even when everything else is: somebody upset does not want
            to be answered in a language they did not choose.
            """)
    @UserMessage(
            """
            What they wrote:
            {{sofar}}
            """)
    TokenStream helpWith(@V("scenarios") String scenarios, @V("sofar") String soFar);
}
