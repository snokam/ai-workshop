package com.example.aiworkshop.cases;

/**
 * Something the Case Chat agent has suggested a Case Handler do. Never something it has done.
 *
 * <p>Sealed on purpose. Confirming a Proposal switches over the permitted forms by pattern, so a
 * third kind of write cannot be added without the compiler pointing at the place that has to decide
 * what confirming it means. That is the safety property this type exists for: an agent can raise a
 * Proposal, and there is exactly one place where a Proposal turns into a write.
 */
public sealed interface Proposal permits ReviewProposal, DocumentRequestProposal {

    String id();

    String caseId();

    /** Why the agent thinks this is worth doing, in its own words. Shown on the card. */
    String reason();

    ProposalState state();

    Proposal withState(ProposalState state);
}
