package com.example.aiworkshop.fraud;

import java.util.List;

/**
 * One way of looking at an uploaded file for signs it is not what it appears to be.
 *
 * <p>Every implementation is a Spring bean, and {@link FraudScreener} runs all of them it is given.
 * Adding a check is therefore adding a class — the seam this package exists to make obvious. Three
 * ship with it: the file has been seen online, the file has been seen here before, and the file's
 * own metadata says something about where it came from. A fourth reads what the intake agent
 * already noticed.
 *
 * <p>Two rules an implementation must hold to:
 *
 * <ul>
 *   <li><b>Never refuse.</b> A check returns findings; it cannot stop an upload. An upload is always
 *       accepted, and a check that could reject one would put a heuristic between a Claimant and
 *       their own Case.
 *   <li><b>Never throw.</b> {@link FraudScreener} catches anyway, because a screening failure must
 *       never lose someone's document — but a check that swallows its own trouble can say so in an
 *       Indicator instead of leaving a handler to assume it ran clean.
 * </ul>
 */
public interface FraudCheck {

    /** What this check noticed. An empty list is the ordinary answer for an ordinary document. */
    List<FraudIndicator> screen(ScreenedFile file);

    /** Named in logs, so a handler asking "did the online check run?" has somewhere to look. */
    default String name() {
        return getClass().getSimpleName();
    }
}
