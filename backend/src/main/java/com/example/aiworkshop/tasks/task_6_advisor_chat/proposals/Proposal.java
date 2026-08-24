package com.example.aiworkshop.tasks.task_6_advisor_chat.proposals;

public sealed interface Proposal permits ReviewProposal, DocumentRequestProposal {
    String id();

    String claimId();

    String reason();

    ProposalState state();

    Proposal withState(ProposalState state);

    default boolean isOutstanding() {
        return state() == ProposalState.PROPOSED;
    }
}
