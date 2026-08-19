package com.example.aiworkshop.tasks.task_6_chat.proposals;

public record ProposalCard(String id, ProposalKind kind, String subject, String reason, ProposalState state) {
    public static ProposalCard of(Proposal proposal) {
        return switch (proposal) {
            case ReviewProposal review ->
                new ProposalCard(review.id(), ProposalKind.REVIEW, review.filename(), review.reason(), review.state());
            case DocumentRequestProposal request ->
                new ProposalCard(
                        request.id(),
                        ProposalKind.DOCUMENT_REQUEST,
                        request.label(),
                        request.reason(),
                        request.state());
        };
    }

    @Override
    public String toString() {
        return "%s %s [%s] — %s".formatted(kind, subject, state, reason);
    }
}
