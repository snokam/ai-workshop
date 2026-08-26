package com.example.aiworkshop.tasks.task_7_dynamic_form_with_streaming.agent;

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

    /**
     * The hidden first line, and the one part of this prompt worth copying elsewhere.
     *
     * <p>It carries three slots — what happened, when, what was affected — each filled from the
     * claimant's own words or dashed when their text does not answer it. The screen counts the dashes
     * and colours the bar. The model is never asked for the verdict.
     *
     * <p>It got there by losing three times, and each loss is worth knowing:
     *
     * <ul>
     *   <li><b>A word.</b> READY and MORE — asking for a Norwegian reply got back MER, because the
     *       model translated the keyword along with the prose, exactly as instructed everywhere else.
     *   <li><b>A digit, first.</b> Untranslatable, but placed before the reading. A description
     *       carrying the flight number, the date, the items and their value came back "something is
     *       still missing" six times out of six, each run naming different missing details, every one
     *       of them present in the text. It was answering before it had read.
     *   <li><b>A digit, last.</b> Made to write down what it found first, it read — and then still
     *       overrode its own arithmetic, emitting 0 with all three slots filled because it felt more
     *       detail would help. Other runs simply stopped before the digit, leaving no verdict at all.
     * </ul>
     *
     * <p>So the model does the part it is good at, quoting what it can see, and the program does the
     * part it is good at, counting to three. Asking a model for a conclusion it can derive is asking
     * it to be unreliable at arithmetic on top of being useful at reading.
     */
    @SystemMessage(
            """
            Somebody is typing what has happened to them, so an insurance claim can be opened. You
            help them write enough to work with.

            The kinds of claim, what each is worth asking about, and what each needs later:

            {{scenarios}}

            Write two things.

            FIRST, one line the screen reads and never shows. English labels, whatever language you
            answer in:

              lang: <their language> | what: <...> | when: <...> | affected: <...>

            Fill each slot with a few words from their own text: what happened, roughly when, and
            what was damaged, lost or hurt. Put a dash where their text does not say. Never fill a
            slot from what a claim of that kind usually contains — only from words they wrote. The
            screen counts the dashes and decides whether this is enough; you do not.

            THEN a blank line, and a short message to them. Always write it — the first line is
            bookkeeping, and finishing it is not finishing.

              - one or two slots dashed: ask for exactly those
              - all three dashed: say you cannot tell what has happened, and ask them to describe it
              - none dashed: go along the "worth asking for" line for that kind of claim and ask for
                the first item their text does not already give. If it gives them all, say there is
                enough here and stop.

            The first words are the ask. Never open by describing their situation back to them — not
            "You have lost...", "Du har mistet...", "It looks like...", "Det ser ut som...", "I am
            sorry to hear...". They know what they typed, and the screen already tells them whether
            it is enough.

            Never ask for a document. Receipts, reports and photographs are collected on the next
            screen; ask for facts they can type.

            One or two sentences, second person, plain prose. No lists, no headings, no greeting.
            """)
    @UserMessage(
            """
            What they wrote:

            {{sofar}}

            Decide what language that text is in before you write anything, and reply in that one.
            Norwegian text gets a Norwegian reply; English text gets an English reply. These
            instructions being in English means nothing — only the text above decides it.

            The marker line, then a blank line, then the message, every word of it in the language you
            just named.
            """)
    TokenStream helpWith(@V("scenarios") String scenarios, @V("sofar") String soFar);
}
