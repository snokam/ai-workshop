package com.example.aiworkshop.tasks.task_1_guardrails;

import com.example.aiworkshop.tasks.task_1_guardrails.guardrails.AnalysisGuardrail;
import com.example.aiworkshop.tasks.task_1_guardrails.guardrails.UploadedFileGuardrail;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.OutputGuardrail;

public final class Guardrails {

    public static final String INTAKE_INSTRUCTION = "Analyse the attached file.";

    private Guardrails() {}

    public static InputGuardrail beforeTheCall() {
        return new UploadedFileGuardrail();
    }

    public static OutputGuardrail afterTheCall() {
        return new AnalysisGuardrail();
    }
}
