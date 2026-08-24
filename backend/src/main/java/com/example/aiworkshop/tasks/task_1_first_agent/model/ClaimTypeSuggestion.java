package com.example.aiworkshop.tasks.task_1_first_agent.model;

import dev.langchain4j.model.output.structured.Description;

public record ClaimTypeSuggestion(
        @Description("The claim type that best fits what the person described. Null if none of them"
                        + " fit — do not force the closest one.")
                ClaimType type,
        @Description("How sure you are of that type: HIGH, MEDIUM or LOW. LOW if you named no type.")
                MatchConfidence confidence,
        @Description("One plain-language sentence explaining why this type fits what was described.")
                String rationale) {}
