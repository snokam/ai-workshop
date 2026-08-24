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
        // TODO — task 2, part 3. Hand it back.
        //
        // Return new ClaimDescriptionGuardrail(check).
        //
        // GuardrailConfig calls this and publishes the result as a bean. Task 1's agent takes whichever
        // InputGuardrails exist and knows nothing about who wrote them, which is why task 1 can be finished
        // and run before this task is started.
        //
        // Until this returns one, anything typed into the box reaches the model, including an empty one.

        throw new TaskNotImplementedException(WorkshopTask.GUARDRAILS);
    }
}
