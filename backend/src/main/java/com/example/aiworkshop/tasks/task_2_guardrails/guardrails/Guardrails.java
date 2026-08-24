package com.example.aiworkshop.tasks.task_2_guardrails.guardrails;

import com.example.aiworkshop.workshop.TaskNotImplementedException;
import com.example.aiworkshop.workshop.WorkshopTask;
import dev.langchain4j.guardrail.InputGuardrail;

/**
 * What guards the first agent.
 *
 * <p>One guardrail, and it runs before the call. That is not a simplification for the workshop — it
 * is the order things belong in. A check that decides on the text alone costs nothing when it
 * refuses, cannot be talked round by what it is reading, and is the only kind you can put in front
 * of a model rather than behind it.
 */
public final class Guardrails {

    private Guardrails() {}

    public static InputGuardrail beforeTheCall() {
        return new ClaimDescriptionGuardrail();

        // ── To set this task again ────────────────────────────────────────────────────────
        // TODO — task 2. Hand back the guardrail that reads what the person typed.
        //
        // Until this returns one the classifier is wired without guardrails: anything typed into the
        // box reaches the model, including an empty one.
        // throw new TaskNotImplementedException(WorkshopTask.GUARDRAILS);
    }
}
