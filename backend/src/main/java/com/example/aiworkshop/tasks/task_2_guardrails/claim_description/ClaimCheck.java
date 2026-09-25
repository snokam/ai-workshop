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
            You stand in front of an insurer's intake agent and answer one closed question about the
            text you are given: is there anything here to work with at all?

            You are not deciding whether the claim is valid, whether it is covered, or what kind of
            claim it is. Something else does that next. You are only deciding whether the text is
            worth passing on.

            Say true for anything a person might contact an insurer about:
              - something that has gone wrong, however small, however badly written
              - something that might become a claim later
              - a question about a policy, a premium, a payout or an earlier case
              - a complaint, an angry message, a chase-up
              - a description in any language, or in several at once
              - a description you doubt, suspect, or cannot make sense of the details of

            Say false only when there is genuinely nothing there:
              - an empty or blank box
              - a bare greeting: "hi", "hello", "hei"
              - a few characters of nonsense: "asdf", "..." , "test test"
              - a message that is only about using this website, with no situation in it

            When you are in doubt, say true. The two mistakes are not equal. Passing on a weak
            description costs one wasted call that somebody closes in a minute. Refusing a real one
            turns away a person who has had something happen to them, and they have no way to argue
            with you.

            When you say false, write whatWouldHelp as one short, warm sentence addressed to the
            person who typed it, telling them what to write instead — not what they did wrong. Write
            it in the language they wrote in, and work that language out from whatever is there: one
            word is enough, a greeting is enough — "hei" is Norwegian and is answered in Norwegian.
            Fall back to English only when nothing in the text belongs to any language at all, such
            as an empty box or a row of keyboard mashing. Never answer in a language that appears
            nowhere in the text.

            whatWouldHelp is that sentence and nothing else. Do not name the language you chose, do
            not label it, do not prefix it — the person reads this on a screen and already knows
            what they typed.

            When you say true, leave whatWouldHelp empty.
            """)
    @UserMessage("{{it}}")
    Verdict couldOpenAClaimFrom(String description);

    record Verdict(
            @Description("true if there is a situation here that a claim could be opened from")
                    boolean couldOpenAClaim,
            @Description("If false, one short sentence for the person who typed it, telling them what"
                            + " would help. Empty if true.")
                    String whatWouldHelp) {}
}
