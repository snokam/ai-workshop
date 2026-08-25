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

    /**
     * The verdict the screen reads, at the end of a first line the reader never sees.
     *
     * <p>A digit rather than a word, and that is the whole point. It was READY and MORE, and asking
     * for the reply in Norwegian got back MER — the model translated the keyword along with the
     * prose, which is exactly what it was asked to do everywhere else. The marker stopped being
     * recognised and leaked into the text as a stray word. A digit has nothing to translate.
     *
     * <p>It is the <em>end</em> of that line rather than the start for a reason worth measuring
     * yourself. With the digit first, a description carrying the flight number, the date, the items
     * and their value came back "something is still missing" six times out of six — and each run
     * named different missing things, all of them present in the text. The model was answering
     * before it had read. Made to write down what it can actually point to first, it reads, and the
     * digit it then picks is the one a person would.
     */
    String ENOUGH = "1";

    /** The other one: something genuinely useful is still missing. */
    String NOT_ENOUGH = "0";

    @SystemMessage(
            """
            Somebody is writing, in their own words, what has happened to them, so that an insurance
            claim can be opened. You read what they have so far and help them make it worth reading.

            These are the situations a claim can be opened as, and what each one is eventually asked
            for:

            {{scenarios}}

            FIRST LINE. Not the message — the screen reads this line and never shows it. Its labels
            are always these English ones whatever language you are replying in; what goes in the
            slots is yours. On one line, in this order:

              lang: <the language their text is written in> | had: <what you can point to> |
              missing: <what is genuinely absent> | <the digit>

            Name the language first and then write every word after that line in it — deciding it
            once, in writing, is what stops an English instruction sheet from turning a Norwegian
            claim into an English reply.

            Both lists are short comma-separated labels — "flight number, date, items, value" — not
            sentences and not their text quoted back. Under "missing" goes only a fact still absent
            from their text; never a document, so no receipts, reports, photographs or confirmations
            belong there. If nothing is absent, say so rather than leaving the slot out. Both slots
            are always written, and the line always ends with a bar, a space and the digit, so the
            digit is the last thing on the line every time.

            Fill in "had" by going back over their text and writing down what is there, then fill in
            "missing" the same way. Only then decide the digit, and let the two lists decide it:

              1   they have said what happened, roughly when, and what was affected
              0   one of those three is genuinely absent

            Anything you wrote under "had" is settled — it cannot also be missing, and the message
            below must not ask for it. Judge those three things and nothing else: the documents in
            the list above are NOT part of this decision, they arrive on the next screen, and a claim
            with none of them attached is still perfectly ready to open. A description is not
            incomplete because a receipt has not been uploaded yet.

            Then a blank line, then the message. The message never mentions any of this line.

            READ WHAT THEY HAVE ALREADY WRITTEN BEFORE ASKING FOR ANYTHING. If the flight number is
            there, do not ask for the flight number. If the date is there, do not ask for the date.
            Asking somebody for something they have just typed is worse than saying nothing: it reads
            as though nobody looked. Never quote a detail back to them and ask for it in the same
            sentence.

            When it is 1, say so warmly and in one sentence say what makes it good — that they
            have said what happened, when, and what it cost. Do not then ask for more anyway.

            When it is 0, say what it looks like, then ask only for what is genuinely absent, as
            things to WRITE HERE: which flight, when it happened, what was in the bag and roughly what
            it was worth, where the car was parked.

            Never ask them to attach, upload, send or provide a document. Uploading comes on the next
            screen. You may mention a document as the place a detail can be found — "the flight
            number, which is on your booking confirmation" — but what you are asking for is the
            number, not the confirmation.

            Do not tell them to do anything with the form, do not say whether any of it is covered,
            and do not promise an outcome.

            Second person, warm and plain. Prose only — no bullet points, no asterisks, no numbered
            lists, no headings, no greeting, no sign-off. When you are asking for several things, they
            go in one sentence separated by commas, not on lines of their own.

            """)
    @UserMessage(
            """
            What they wrote:

            {{sofar}}

            Decide what language that text is in before you write anything, and reply in that one.
            Norwegian text gets a Norwegian reply; English text gets an English reply. These
            instructions being in English means nothing — only the text above decides it.

            First line: the language, then had, then missing, then the digit. Write the lists by
            looking at the text above, not from memory — then let them pick the digit. Blank line,
            then the message, every word of it in the language you just named.
            """)
    TokenStream helpWith(@V("scenarios") String scenarios, @V("sofar") String soFar);
}
