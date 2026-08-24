package com.example.aiworkshop.tasks.task_2_guardrails.guardrails;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailRequest;
import dev.langchain4j.guardrail.InputGuardrailResult;
import java.util.List;
import java.util.Locale;

/**
 * Refuses text that nobody could open a case from, before the model is paid to read it.
 *
 * <p>The classifier will answer whatever it is given. Send it "hi" and it returns OTHER with LOW
 * confidence and a polite sentence of reasoning, which costs a model call and produces a case that
 * a handler now has to close. Send it an empty box and it does the same. The agent is not wrong —
 * nobody told it that some inputs are not worth an answer.
 *
 * <p>That is what an input guardrail is for. It runs before the call, it decides on the text alone,
 * and it cannot be argued with by the text it is reading, because it is not reading for meaning. Two
 * rules, and both are about substance rather than subject:
 *
 * <ul>
 *   <li>there has to be enough of it — a handful of characters cannot describe anything;
 *   <li>it has to be more than a greeting — "hei" and "hello there" are how people open a message,
 *       not how they describe what happened.
 * </ul>
 *
 * <p>Notice what is deliberately not here: any judgement about whether the situation is insurable.
 * That is the classifier's job, and it has a whole case type for it. A guardrail that tried to decide
 * "is this really a claim" would be a second, worse classifier standing in front of the first.
 */
public class ClaimDescriptionGuardrail implements InputGuardrail {

    /** Shorter than this and there is nothing to classify. */
    static final int ENOUGH_TO_GO_ON = 15;

    /** Openings that are how a message starts rather than what it says. */
    static final List<String> JUST_A_GREETING =
            List.of("hi", "hei", "hallo", "hello", "hey", "good morning", "god dag", "test");

    @Override
    public InputGuardrailResult validate(UserMessage message) {
        String description = message.singleText() == null
                ? ""
                : message.singleText().trim();

        if (description.length() < ENOUGH_TO_GO_ON) {
            return fatal("Tell us a little more about what happened — a sentence is enough.");
        }
        if (JUST_A_GREETING.contains(description.toLowerCase(Locale.ROOT).replaceAll("[.!?]+$", ""))) {
            return fatal("Tell us what happened rather than saying hello, and we will open the right case.");
        }
        return success();

        // ── To set this task again ────────────────────────────────────────────────────────
        // TODO — task 2. Refuse text nobody could open a case from.
        //
        // message.singleText() is what the person typed. Return fatal(...) with something they can
        // act on when there is not enough of it, or when it is only a greeting. Return success()
        // otherwise.
        //
        // Whatever you write here runs before the model does, which is the point: an input guardrail
        // is the only check in this workshop that costs nothing when it refuses.
        //
        // return success();
    }

    @Override
    public InputGuardrailResult validate(InputGuardrailRequest request) {
        return validate(request.userMessage());
    }
}
