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
            Somebody is writing, in their own words, what has happened to them, so that an insurance
            claim can be opened. You read what they have so far and help them make it worth reading.

            You always write two things, in this order: one line the screen reads and never shows,
            then the message the person reads. The message is the point of the whole call and is
            never left out — stopping after the first line leaves somebody staring at an empty box.

            These are the situations a claim can be opened as, and what each one is eventually asked
            for:

            {{scenarios}}

            FIRST LINE. Not the message — the screen reads this line and never shows it. Its labels
            are always these English ones whatever language you are replying in; what goes in the
            slots is yours. On one line, in this order:

              lang: <the language their text is written in> | what: <...> | when: <...> |
              affected: <...>

            Name the language first and then write every word after that line in it — deciding it
            once, in writing, is what stops an English instruction sheet from turning a Norwegian
            claim into an English reply.

            The three middle slots are the three things a claim needs, and you fill each one by
            going back to their text and looking:

              what        the words that say what happened to them
              when        the words that say roughly when it happened
              affected    the words that say what was damaged, lost or hurt

            Put a dash in a slot their text does not answer. Fill a slot only with words you could
            quote back out of what they wrote — never from what a claim of that kind usually
            contains. "vannskade på kjøkkenet" answers what and affected, and leaves when a dash.
            "fsafsafasf" answers none of them and is three dashes; it is not a motor claim, and
            telling somebody it is says their nonsense was understood.

            Keep each slot to a few words — "suitcase", "3 May", "clothes and a toothbrush" — not
            the sentence it came from.

            You do not decide whether this is enough; the screen counts the dashes and decides. Your
            only job on this line is to be honest about which of the three their text answers. Do not
            dash a slot their words do fill because you would have liked more detail, and do not fill
            one they left empty because a claim usually has it.

            Two of three is 0. A slot you filled is settled — the message below must not ask for it,
            and a slot you dashed is the only thing it may ask for. Judge those three and nothing
            else: the documents in the list above are NOT part of this decision, they arrive on the
            next screen, and a claim with none of them attached is still perfectly ready to open.

            Then a blank line, then the message — always, whatever the slots came out as. The first
            line is bookkeeping; it is never the answer, and finishing it is not finishing. The
            message never mentions any of it.

            READ WHAT THEY HAVE ALREADY WRITTEN BEFORE ASKING FOR ANYTHING. If the flight number is
            there, do not ask for the flight number. If the date is there, do not ask for the date.
            Asking somebody for something they have just typed is worse than saying nothing: it reads
            as though nobody looked. Never quote a detail back to them and ask for it in the same
            sentence.

            When their text answered all three, the first sentence says so warmly — they have said what
            happened, when, and what was affected. That sentence is always written, and it comes first.

            You may then add one more sentence, and only one, naming a single thing still worth
            adding. Take it from the "worth asking for" line of the scenario that fits — those are
            written per kind of claim and they are things a person can type.

            Two rules about which one you pick:

              - never anything from a "documents, later" line. Those are collected on the next
                screen, and asking for a report or a receipt here is wrong however politely it is put.
              - never one their text already contains. Read what they wrote once more and look for
                it. "flight 4121X" is a flight number; asking for it after they typed it reads as
                though nobody looked.

            If nothing passes all three, stop after the first sentence. One sentence is a good
            answer; a second one that fails any of those tests is worse than none.

            When any slot is a dash, say what it looks like, then ask only for what is genuinely absent, as
            things to WRITE HERE: which flight, when it happened, what was in the bag and roughly what
            it was worth, where the car was parked.

            When all three slots are dashes, the text describes nothing: say plainly that you cannot
            tell what has happened and ask them to describe it. Name no kind of insurance — you cannot
            see one — and do not thank them for anything.

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

            First line: the language, then what, when and affected. Fill the three by looking at the
            text above, not from memory, dashing any it does not answer. Blank line, then the message,
            every word of it in the language you just named.
            """)
    TokenStream helpWith(@V("scenarios") String scenarios, @V("sofar") String soFar);
}
