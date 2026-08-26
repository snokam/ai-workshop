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
            TODO — task 2, part 1. Write the check.

            You are writing the system message for a second agent, asked one closed question in front of the
            first: is there anything here to open a claim from? The text itself arrives as the user message,
            so there is no variable to render — the system message is only the instruction. The smallest one
            that runs:

              Decide whether the text is something an insurance company could open a claim from.
              Answer true or false. When it is false, add one sentence saying what would help.

            Start from something like that and it will answer — and it will also turn away anything unusual,
            because "could open a claim from" reads far stricter than it is meant to. That is the gap the
            rest of this closes.

            Yours has to make it:
              1. say yes to anything a person might contact an insurer about — a question about a policy, a
                 complaint, something that has gone wrong, something that might become a claim. It does not
                 have to be valid or covered, and deciding what kind of claim it is comes next
              2. say no only when there is nothing to work with: an empty box, a greeting, a few characters
                 of nonsense
              3. say yes when in doubt. Refusing an unusual claim is far worse than opening one somebody
                 closes: the second wastes a minute, the first turns a person away
              4. write whatWouldHelp to the person, in their language — and in English when the text is too
                 short or garbled to have one. An early version answered "asdf" in Spanish

            That is the shape of Verdict, the record it returns. Read it: the @Description on each component
            is part of the prompt too.
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
