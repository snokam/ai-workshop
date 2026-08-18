package com.example.aiworkshop.tasks.task_1_guardrails.model;

import dev.langchain4j.model.output.structured.Description;

public record ManipulationAttempt(
        @Description("What the text tried to make you do, in one sentence. Null if the document contains nothing"
                        + " of the kind.")
                String attemptedInstruction,
        @Description("The words themselves, quoted from the document.") String quote) {}
