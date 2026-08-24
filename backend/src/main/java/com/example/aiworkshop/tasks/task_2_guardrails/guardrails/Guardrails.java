package com.example.aiworkshop.tasks.task_2_guardrails.guardrails;

import com.example.aiworkshop.workshop.TaskNotImplementedException;
import com.example.aiworkshop.workshop.WorkshopTask;
import dev.langchain4j.guardrail.InputGuardrail;

/**
 * What guards the first agent.
 *
 * <p>One guardrail, and it runs before the call. Not a simplification for the workshop — it is the
 * order things belong in. A check in front of a model can refuse before anything is spent, and is
 * much harder to talk round than one standing behind it, because it decides on the request rather
 * than on an answer the request has already shaped.
 */
public final class Guardrails {

    private Guardrails() {}

    public static InputGuardrail beforeTheCall(ClaimCheck check) {
        return new ClaimDescriptionGuardrail(check);

        // ── To set this task again ────────────────────────────────────────────────────────
        // TODO — task 2. Hand back the guardrail that reads what the person typed.
        //
        // Until this returns one the classifier is wired without guardrails: anything typed into the
        // box reaches the model, including an empty one.
        // throw new TaskNotImplementedException(WorkshopTask.GUARDRAILS);
    }
}
