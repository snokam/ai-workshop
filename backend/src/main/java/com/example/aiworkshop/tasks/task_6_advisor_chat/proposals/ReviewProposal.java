package com.example.aiworkshop.tasks.task_6_advisor_chat.proposals;

public record ReviewProposal(
        String id, String caseId, String documentId, String filename, String reason, ProposalState state)
        implements Proposal {
    @Override
    public Proposal withState(ProposalState newState) {
        return new ReviewProposal(id, caseId, documentId, filename, reason, newState);
    }
}
