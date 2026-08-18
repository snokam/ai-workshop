package com.example.aiworkshop.tasks.task_3_guardrails;

import com.example.aiworkshop.workshop.WorkshopTask;
import com.example.aiworkshop.workshop.TaskNotImplementedException;
import com.example.aiworkshop.tasks.task_3_guardrails.guardrails.UploadedFileGuardrail;
import com.example.aiworkshop.tasks.task_3_guardrails.guardrails.AnalysisGuardrail;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.OutputGuardrail;

public final class Guardrails {


    public static final String INTAKE_INSTRUCTION = "Analyse the attached file.";

    private Guardrails() {}

    public static InputGuardrail beforeTheCall() {
        // TODO — task 3. Hand back the guardrail that checks the message before it is sent.
        //
        // Until this returns one the intake agent is wired without guardrails: uploads still work,
        // and nothing is checked. Write UploadedFileGuardrail, then return it here.
        throw new TaskNotImplementedException(WorkshopTask.GUARDRAILS);

        // return new UploadedFileGuardrail();
    }

    public static OutputGuardrail afterTheCall() {
        // TODO — task 3. Hand back the guardrail that checks the reply before it is parsed.
        // return new AnalysisGuardrail();
        throw new TaskNotImplementedException(WorkshopTask.GUARDRAILS);
    }
}
