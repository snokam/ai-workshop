package com.example.aiworkshop.tasks.task_2_guardrails.claim_description;

import dev.langchain4j.model.output.structured.Description;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * A model asked one question about the text, before the model that has to act on it.
 *
 * <p>This is an agent guarding an agent, and the shape is worth noticing. The classifier is asked
 * something open — which of these claim types fits? — and it will answer whatever it is given, because
 * that is what it was built to do. Ask it about "hi" and it will pick something, or refuse and say
 * why — either way you have paid for a call to find out there was nothing there.
 *
 * <p>This one is asked something closed, with one job and no catalogue to choose from: is there
 * anything here to open a claim from? A narrow question is a cheaper question, and it is much harder
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
            TODO — task 2, part 1. Write the check.

            You are writing the system message for a second agent, asked one closed question in front of the
            first: is there anything here to open a claim from?

              couldOpenAClaimFrom(String description) returns Verdict(boolean couldOpenAClaim, String whatWouldHelp)

            Say yes to anything a person might contact an insurer about — a question about a policy, a
            complaint, something that has gone wrong, something that might become a claim. It does not have to
            be valid or covered. Deciding what kind of claim it is comes next and is not this agent's job.

            Say no only when there is nothing to work with: an empty box, a greeting, a few characters of
            nonsense.

              - When in doubt, say yes. Refusing an unusual claim is far worse than opening a claim somebody
                closes: the second wastes a minute, the first turns a person away.
              - whatWouldHelp is shown to the person, so write it to them, in their language — and in English
                when the text is too short or garbled to have one. An early version answered "asdf" in Spanish.
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
