package com.example.aiworkshop.document;

/**
 * How sure the intake agent is that a Document satisfies the Required Document it named.
 *
 * <p>Three coarse values rather than a number: a model's self-reported confidence is weakly
 * calibrated, and 0.82 invites a threshold that the number cannot actually support. Shown to a Case
 * Handler so they know where to look first; deliberately absent from the Case Status derivation
 * (ADR 0001).
 */
public enum MatchConfidence {
    /** The document plainly is the thing the Case asked for. */
    HIGH,
    /** It fits, but a handler glancing at it would not be wasting their time. */
    MEDIUM,
    /** A guess. Worth a handler checking before the Case is decided. */
    LOW
}
