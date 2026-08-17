package com.example.aiworkshop.cases;

/**
 * Something a Case Handler has asked the Claimant for.
 *
 * <p>A distinct thing from the {@link DocumentRequestProposal} that produced it. A Proposal is what
 * the agent suggested; a Document Request is what exists in the world, and it exists only because a
 * Case Handler clicked Confirm.
 *
 * <p>Deliberately not one of the Case's Required Documents. That list is what Case Status is derived
 * from (ADR 0001), and a Document Request that appended to it would let a question walk a Case
 * backwards out of {@code READY_FOR_DECISION}. A Claimant sees both, side by side, and the checklist
 * is still the checklist.
 *
 * @param label what the Claimant is being asked for, in words they will understand
 * @param reason why it is needed, so the ask does not arrive bare
 */
public record DocumentRequest(String id, String caseId, String label, String reason) {}
