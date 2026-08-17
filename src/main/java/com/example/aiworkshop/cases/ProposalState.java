package com.example.aiworkshop.cases;

/**
 * Where a Proposal stands.
 *
 * <p>A Proposal only ever leaves {@link #PROPOSED} because a Case Handler clicked. Nothing an agent
 * does moves it, which is the whole of what makes a Proposal safe to raise.
 */
public enum ProposalState {
    /** Suggested by the agent, waiting on a Case Handler. Nothing has happened. */
    PROPOSED,
    /** A Case Handler said yes, and the write it stood for has been performed. */
    CONFIRMED,
    /** A Case Handler said no. Kept, and fed back to the agent so it does not suggest it again. */
    DECLINED
}
