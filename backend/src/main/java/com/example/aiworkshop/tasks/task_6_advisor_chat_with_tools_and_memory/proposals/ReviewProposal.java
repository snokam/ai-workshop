package com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools_and_memory.proposals;

public record ReviewProposal(
        String id, String claimId, String documentId, String filename, String reason, ProposalState state)
        implements Proposal {
    @Override
    public Proposal withState(ProposalState newState) {
        return new ReviewProposal(id, claimId, documentId, filename, reason, newState);
    }
}
