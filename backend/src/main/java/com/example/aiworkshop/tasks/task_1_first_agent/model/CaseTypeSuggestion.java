package com.example.aiworkshop.tasks.task_1_first_agent.model;

import dev.langchain4j.model.output.structured.Description;

public record CaseTypeSuggestion(
        @Description("The case type that best fits what the person described. OTHER if none fit.")
                CaseType type,
        @Description("How sure you are of that type: HIGH, MEDIUM or LOW. LOW if you chose OTHER.")
                MatchConfidence confidence,
        @Description("One plain-language sentence explaining why this type fits what was described.")
                String rationale) {}
