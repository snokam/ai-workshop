package com.example.aiworkshop.tasks.task_2_guardrails.guardrails;

import dev.langchain4j.model.output.structured.Description;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * A model asked one question about the text, before the model that has to act on it.
 *
 * <p>This is an agent guarding an agent, and the shape is worth noticing. The classifier is asked
 * something open — which of these case types fits? — and it will answer whatever it is given, because
 * that is what it was built to do. Ask it about "hi" and it will pick something, or refuse and say
 * why — either way you have paid for a call to find out there was nothing there.
 *
 * <p>This one is asked something closed, with one job and no catalogue to choose from: is there
 * anything here to open a case from? A narrow question is a cheaper question, and it is much harder
 * to talk a model round when there is only one thing to say.
 *
 * <p>The cost is real and worth saying plainly: this guardrail is not free. A check that counts
 * characters costs nothing when it refuses; this one costs a call to save a call. It is worth it
 * because the call it saves is the more expensive of the two — and because the thing it is judging
 * is a matter of meaning, which nothing cheaper can judge at all.
 */
public interface ClaimCheck {

    @SystemMessage(
            """
            You stand in front of an insurance company's case intake. Someone has typed something into
            a box, and your one job is to decide whether there is anything in it to open a case from.

            Say yes to anything that describes a situation a person might contact an insurer about.
            It does not have to be a valid claim, or covered, or even clearly insurance — a question
            about a policy, a complaint, something that has gone wrong, a situation that might turn
            into a claim later. Deciding what kind of case it is comes next and is not your job.

            Say no only when there is nothing to work with: an empty box, a greeting, a test, a few
            characters of nonsense, or something with no situation in it at all.

            When in doubt, say yes. Refusing someone with an unusual claim is far worse than opening a
            case somebody has to close: the second wastes a minute, the first turns a person away.

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
            @Description("true if there is a situation here that a case could be opened from")
                    boolean couldOpenACase,
            @Description("If false, one short sentence for the person who typed it, telling them what"
                            + " would help. Empty if true.")
                    String whatWouldHelp) {}
}
