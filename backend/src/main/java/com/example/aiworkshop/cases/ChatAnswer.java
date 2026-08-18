package com.example.aiworkshop.cases;

import java.util.List;

/**
 * What one turn of a Case Chat hands back.
 *
 * <p>The Proposals are every Proposal on the Case, not only the ones this turn raised. A turn can
 * change the state of nothing, but the screen redraws the whole conversation, and one live list is
 * simpler to be right about than a patch.
 */
public record ChatAnswer(ChatTurn turn, List<ProposalCard> proposals) {}
