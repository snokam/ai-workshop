package com.example.aiworkshop.cases;

/**
 * The agent suggesting that the Claimant be asked for a document.
 *
 * <p>The label is plain text rather than one of the Case's Required Documents. A Document Request is
 * a question, and a question that had to be chosen from the Required Documents could not ask for the
 * second page of something that has already arrived.
 *
 * @param label what to ask the Claimant for, in words the Claimant will understand
 */
public record DocumentRequestProposal(String id, String caseId, String label, String reason, ProposalState state)
        implements Proposal {

    @Override
    public Proposal withState(ProposalState newState) {
        return new DocumentRequestProposal(id, caseId, label, reason, newState);
    }
}
