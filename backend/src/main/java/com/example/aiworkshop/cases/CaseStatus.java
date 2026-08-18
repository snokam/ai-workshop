package com.example.aiworkshop.cases;

/**
 * Where a Case stands. Always derived from the Documents attached to it, never stored — a stored
 * status is a second source of truth that drifts from the Documents it claims to describe.
 */
public enum CaseStatus {
    /** At least one Required Document has nothing matching it yet. Not the handler's to act on. */
    AWAITING_DOCUMENTS,
    /** Everything required has arrived, but one of them is too poor to work with. A human decides. */
    NEEDS_REVIEW,
    /** Everything required has arrived and is usable. */
    READY_FOR_DECISION
}
