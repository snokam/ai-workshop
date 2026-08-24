package com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools.proposals;

public record DocumentRequestProposal(String id, String claimId, String label, String reason, ProposalState state)
        implements Proposal {
    @Override
    public Proposal withState(ProposalState newState) {
        return new DocumentRequestProposal(id, claimId, label, reason, newState);
    }
}
