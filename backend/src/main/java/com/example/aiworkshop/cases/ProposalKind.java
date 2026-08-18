package com.example.aiworkshop.cases;

/** Which of the two writes a Proposal stands for. One value per permitted form of {@link Proposal}. */
public enum ProposalKind {
    /** Record a Review against a named Document. */
    REVIEW,
    /** Ask the Claimant for a named document. */
    DOCUMENT_REQUEST
}
