package com.example.aiworkshop.cases;

import com.example.aiworkshop.document.MatchConfidence;
import java.util.List;

/**
 * What the claimant screen gets back after describing what they need help with: the Case that was
 * opened, and the agent's account of why this type.
 *
 * <p>The type is returned here as a label rather than stored on the {@link Case} — a Case created
 * from a type still holds only its Required Documents, so the checklist stays the single source of
 * truth and nothing downstream has to learn about types. The label and rationale are for the screen
 * to show once, at the moment of creation.
 *
 * @param id the new Case identifier the screen then uploads against
 * @param reference the human-readable name the Case was given
 * @param typeLabel the chosen {@link CaseType}'s label, e.g. "Travel insurance claim"
 * @param confidence how sure the classifier was of that type
 * @param rationale one sentence explaining the choice, shown to the Claimant
 * @param requiredDocuments the checklist the Case was created with
 * @param status the Case's status at creation — {@code AWAITING_DOCUMENTS} whenever it needs anything
 */
public record CreatedCase(
        String id,
        String reference,
        String typeLabel,
        MatchConfidence confidence,
        String rationale,
        List<String> requiredDocuments,
        CaseStatus status) {}
