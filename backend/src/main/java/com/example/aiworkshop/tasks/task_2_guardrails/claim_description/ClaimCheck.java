package com.example.aiworkshop.tasks.task_2_guardrails.claim_description;

import dev.langchain4j.model.output.structured.Description;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * Asks one yes-or-no question about the text before the classifier ever sees it: is there anything
 * here to open a claim from?
 *
 * <p>The point is to keep the classifier doing what it is supposed to do. Handed junk, a model still
 * answers, and it answers confidently — and that answer becomes a claim with a reference number on
 * it. Stopping it here is what lets everything after this point assume the text it was given was
 * worth reading.
 *
 * <p>For example:
 *
 * <pre>{@code
 * "my kitchen flooded last night" -> new Verdict(true, "")
 * "hi"                            -> new Verdict(false, "Please tell us more about why you are contacting us.")
 * }</pre>
 *
 * <p>The first goes on to the classifier and comes back a home contents claim. The second never
 * reaches it, and that sentence is what the person is shown instead.
 */
public interface ClaimCheck {

    @SystemMessage(
            """
            You stand in front of an insurance company's claim intake. Someone has typed something into
            a box, and your one job is to decide whether there is anything in it to open a claim from.

            Say yes to anything that describes a situation a person might contact an insurer about.
            It does not have to be a valid claim, or covered, or even clearly insurance — a question
            about a policy, a complaint, something that has gone wrong, a situation that might turn
            into a claim later. Deciding what kind of claim it is comes next and is not your job.

            Say no only when there is nothing to work with: an empty box, a greeting, a test, a few
            characters of nonsense, or something with no situation in it at all.

            When in doubt, say yes. Refusing someone with an unusual claim is far worse than opening a
            claim somebody has to close: the second wastes a minute, the first turns a person away.

            When you say no, write one short sentence to the person who typed it, addressed to them,
            telling them what would help. No apology, no explanation of your reasoning, nothing about
            being an automated check.

            Write it in the language they wrote in. When that is not clear — and it often will not be,
            because the text you are refusing is usually too short or too garbled to have a language
            at all — write in English. Do not guess at a language from a handful of characters:
            answering "asdf asdf" in Spanish is worse than answering it in English.
            """)
    @UserMessage("{{it}}")
    Verdict couldOpenACaseFrom(String description);

    record Verdict(
            @Description("true if there is a situation here that a claim could be opened from")
                    boolean couldOpenACase,
            @Description("If false, one short sentence for the person who typed it, telling them what"
                            + " would help. Empty if true.")
                    String whatWouldHelp) {}
}
