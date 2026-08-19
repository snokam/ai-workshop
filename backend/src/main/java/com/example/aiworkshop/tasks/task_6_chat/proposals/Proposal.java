package com.example.aiworkshop.tasks.task_6_chat.proposals;

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
