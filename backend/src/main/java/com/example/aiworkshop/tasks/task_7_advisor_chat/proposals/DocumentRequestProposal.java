package com.example.aiworkshop.tasks.task_7_advisor_chat.proposals;

public record DocumentRequestProposal(String id, String caseId, String label, String reason, ProposalState state)
        implements Proposal {
    @Override
    public Proposal withState(ProposalState newState) {
        return new DocumentRequestProposal(id, caseId, label, reason, newState);
    }
}
