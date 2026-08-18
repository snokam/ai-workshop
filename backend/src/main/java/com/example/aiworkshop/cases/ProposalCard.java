package com.example.aiworkshop.cases;

/**
 * One Proposal, flattened: what the conversation shows, and what the agent is reminded of.
 *
 * <p>{@link Proposal} is sealed and its two forms hold different components, which is right for the
 * domain and wrong for both readers of it. A screen wants one shape it can render a card from, and a
 * system message wants one line it can list. Flattening happens once, here, in the same pattern
 * switch that will stop compiling if a third form of Proposal is added.
 *
 * @param subject what the Proposal is about — a Document's filename, or the label to ask a Claimant
 *     for
 */
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

    /**
     * Load-bearing: this <em>is</em> the line the Case Chat agent reads back about its own earlier
     * suggestions, not a debugging aid. Pinned by {@code CaseAtAGlanceTest}.
     *
     * <p>The identifier is deliberately absent. The agent never confirms a Proposal and has no use
     * for the handle a Case Handler's click travels on.
     */
    @Override
    public String toString() {
        return "%s %s [%s] — %s".formatted(kind, subject, state, reason);
    }
}
