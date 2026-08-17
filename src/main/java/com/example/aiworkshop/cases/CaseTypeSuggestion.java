package com.example.aiworkshop.cases;

import com.example.aiworkshop.document.MatchConfidence;
import dev.langchain4j.model.output.structured.Description;

/**
 * What the {@link CaseTypeClassifier} returns after reading a Claimant's description of what they
 * need help with.
 *
 * <p>This record is the output schema. LangChain4j derives the JSON contract from it and constrains
 * {@link #type} to the {@link CaseType} constants, so the agent cannot invent a type that has no
 * checklist behind it. {@link MatchConfidence} is reused from the intake side rather than a second
 * three-value scale being introduced — the same coarse HIGH/MEDIUM/LOW, and the same reason for it.
 *
 * @param type the case type the description best fits, or {@link CaseType#OTHER} when none does
 * @param confidence how sure the agent is of that choice; LOW when it fell back to OTHER
 * @param rationale one plain sentence a Claimant could read explaining the choice
 */
public record CaseTypeSuggestion(
        @Description("The case type that best fits what the person described. OTHER if none fit.")
                CaseType type,
        @Description("How sure you are of that type: HIGH, MEDIUM or LOW. LOW if you chose OTHER.")
                MatchConfidence confidence,
        @Description("One plain-language sentence explaining why this type fits what was described.")
                String rationale) {}
