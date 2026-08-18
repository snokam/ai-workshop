package com.example.aiworkshop.cases;

public sealed interface Proposal permits ReviewProposal, DocumentRequestProposal {
    String id();

    String caseId();

    String reason();

    ProposalState state();

    Proposal withState(ProposalState state);

    default boolean isOutstanding() {
        return state() == ProposalState.PROPOSED;
    }
}
