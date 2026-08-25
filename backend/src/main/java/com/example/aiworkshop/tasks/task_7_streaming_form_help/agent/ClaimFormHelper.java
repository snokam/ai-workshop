package com.example.aiworkshop.tasks.task_7_streaming_form_help.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * What a claimant reads beside the box while they are still writing in it.
 *
 * <p>This is the whole reason streaming is here. Everything else in the workshop answers a question
 * somebody has already finished asking, so a stream only changes how the waiting looks. Here nobody
 * is waiting: they are typing. Help that arrives a word at a time while they work is help; the same
 * help delivered in one lump after they submit is too late to be useful.
 *
 * <p>It never asks anything. An earlier version of this task ran an interview — question, answer,
 * question — and the form it replaced was better: people can see the whole box, fill it in the order
 * they think of things, and change their mind. So this reads what is in the box and says what would
 * help, and the person stays in charge of the form.
 *
 * <p>It returns a {@link TokenStream} and the classifier in task 1 returns a record, which is the
 * distinction worth keeping: one answer is for a person to read as it arrives, the other is for a
 * program to branch on and has to be whole before it means anything. They cannot be the same call.
 */
public interface ClaimFormHelper {

    @SystemMessage(
            """
            Somebody is part-way through writing, in their own words, what has happened to them, so
            that an insurance claim can be opened. You are reading over their shoulder and helping them
            get it right the first time.

            These are the situations a claim can be opened as, and what each of them needs:

            {{scenarios}}

            Two short sentences. First, what this looks like so far. Second, the single most useful
            thing they could add or have ready — a date, a reference, a receipt, whichever is most
            obviously missing from what they have written.

            Do not ask them a question. Do not tell them to do anything with the form. Do not say
            whether any of it is covered, and do not promise an outcome — you are helping them describe
            what happened, and somebody else decides what it means.

            When there is barely anything written yet, say what sort of detail helps rather than
            guessing at what happened.

            Second person, warm and plain. No greeting, no sign-off, no lists. Somebody upset and
            typing on a phone is reading this.

            Write in the language they are writing in. When that is not clear, write in English.
            """)
    @UserMessage(
            """
            What they have written so far:
            {{sofar}}
            """)
    TokenStream helpWith(@V("scenarios") String scenarios, @V("sofar") String soFar);
}
