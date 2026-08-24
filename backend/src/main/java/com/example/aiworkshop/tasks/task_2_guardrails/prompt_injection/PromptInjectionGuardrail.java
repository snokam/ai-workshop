package com.example.aiworkshop.tasks.task_2_guardrails.prompt_injection;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailRequest;
import dev.langchain4j.guardrail.InputGuardrailResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Refuses text that is giving the system instructions, and refuses it without saying why.
 *
 * <p>The judgement belongs to {@link InjectionCheck}. What this class decides is what happens next,
 * and one decision in it is worth the whole task.
 *
 * <p><b>The refusal is a constant.</b> {@code ClaimDescriptionGuardrail} shows the person the
 * sentence the model wrote, because that person is stuck and a specific hint helps them. Here the
 * opposite holds. Whoever wrote an injection is iterating: they send something, read what comes
 * back, and adjust. Every word of detail in the refusal is a free measurement — "your text was
 * flagged as trying to set a field" tells them exactly which phrasing to drop next time. So the
 * message is fixed, written here in Java, identical for every refusal, and it explains nothing.
 *
 * <p>The detail is not thrown away, it is sent where the attacker cannot read it: the log. That
 * split — vague to the sender, specific to the operator — is the general shape for any check whose
 * subject is an adversary rather than a confused user.
 *
 * <p>The cost of that choice is real: a person wrongly refused here is told nothing that helps them.
 * Which is a reason to keep the check biased towards letting things through, and a reason to read
 * the log.
 */
public class PromptInjectionGuardrail implements InputGuardrail {

    /**
     * What the person is told, every time, whatever was found. Deliberately not written by a model
     * and deliberately uninformative — see the class comment.
     */
    public static final String REFUSAL = "We could not use that text. Please describe what happened in your own words.";

    private static final Logger log = LoggerFactory.getLogger(PromptInjectionGuardrail.class);

    private final InjectionCheck check;

    public PromptInjectionGuardrail(InjectionCheck check) {
        this.check = check;
    }

    @Override
    public InputGuardrailResult validate(UserMessage message) {
        // TODO — task 2, part 4. Refuse without explaining.
        //
        // Steps:
        //
        //   1. message.singleText() is what the person typed
        //   2. InjectionCheck.Verdict verdict = check.looksLikeAnInstruction(...)
        //   3. if it does not address the system, return success()
        //   4. otherwise log.warn(...) with verdict.whatItAskedFor(), and return fatal(REFUSAL)
        //
        // fatal(...) and success() come from InputGuardrail, which this class implements.
        //
        // Return REFUSAL itself, not verdict.whatItAskedFor(). Passing the check's description
        // through to the screen is the tempting version and it is the wrong one: it turns every
        // refusal into feedback for whoever is probing, and they are the only person who reads it
        // carefully. The detail goes to the log instead.

        return success();
    }

    @Override
    public InputGuardrailResult validate(InputGuardrailRequest request) {
        return validate(request.userMessage());
    }
}
