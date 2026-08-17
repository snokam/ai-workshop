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

    /**
     * Whether this Proposal is still a Case Handler's to answer.
     *
     * <p>A Proposal is answered once. Two clicks on Confirm is the ordinary way a second answer
     * arrives, and a second answer to a document request would ask the Claimant twice for the same
     * thing; reaching Confirm after Decline would perform a write a handler has already refused.
     */
    default boolean isOutstanding() {
        return state() == ProposalState.PROPOSED;
    }
}
