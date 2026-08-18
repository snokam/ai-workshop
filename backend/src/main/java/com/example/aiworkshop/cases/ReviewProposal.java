package com.example.aiworkshop.cases;

/**
 * The agent suggesting that a Document is workable despite its Quality Assessment.
 *
 * <p>Confirming one runs the same Review path the Document's own button runs, so there is one
 * behaviour and not two.
 *
 * @param documentId the Document a Review would be recorded against, resolved when the Proposal was
 *     raised — the agent names a Document by filename and never sees an identifier
 * @param filename the name that was resolved, kept so the card can say which Document this is about
 */
public record ReviewProposal(
        String id, String caseId, String documentId, String filename, String reason, ProposalState state)
        implements Proposal {

    @Override
    public Proposal withState(ProposalState newState) {
        return new ReviewProposal(id, caseId, documentId, filename, reason, newState);
    }
}
