package com.example.aiworkshop.cases.model;

import com.example.aiworkshop.document.model.MatchConfidence;
import dev.langchain4j.model.output.structured.Description;

public record CaseTypeSuggestion(
        @Description("The case type that best fits what the person described. OTHER if none fit.")
                CaseType type,
        @Description("How sure you are of that type: HIGH, MEDIUM or LOW. LOW if you chose OTHER.")
                MatchConfidence confidence,
        @Description("One plain-language sentence explaining why this type fits what was described.")
                String rationale) {}
