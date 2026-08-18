package com.example.aiworkshop.tasks.task_3_guardrails;

import com.example.aiworkshop.tasks.task_3_guardrails.guardrails.AnalysisGuardrail;
import com.example.aiworkshop.tasks.task_3_guardrails.guardrails.UploadedFileGuardrail;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.OutputGuardrail;

public final class Guardrails {
    /**
     * ── TASK GUARDRAILS ────────────────────────────────────────────────────────────────────────
     * Set to true once you have written the two guardrails below. While it is false the
     * application still runs and every screen says so rather than failing.
     * ──────────────────────────────────────────────────────────────────────────────────
     */
    public static final boolean IMPLEMENTED = false;


    public static final String INTAKE_INSTRUCTION = "Analyse the attached file.";

    private Guardrails() {}

    public static InputGuardrail beforeTheCall() {
        return new UploadedFileGuardrail();
    }

    public static OutputGuardrail afterTheCall() {
        return new AnalysisGuardrail();
    }
}
