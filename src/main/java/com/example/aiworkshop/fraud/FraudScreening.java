package com.example.aiworkshop.fraud;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

/**
 * What the checks found on one Document, taken together.
 *
 * <p>Deliberately a separate record from {@code UploadedDocument} rather than a component of it.
 * The Claimant's screen is served the Document; if a Screening hung off it, keeping the two apart
 * would be a matter of remembering to, on every endpoint, forever. Here it is structural: the
 * Screening lives in its own store and reaches the API only through {@code CaseDetail}, which only
 * the handler screen asks for.
 *
 * @param documentId the Document these findings are about
 * @param indicators what the checks noticed, heaviest first. Empty is the normal case and means the
 *     checks ran and found nothing — which is not the same as their not having run
 * @param screenedAt when the checks ran, which is upload time. A Screening is never recomputed: the
 *     file it was made from is not kept, and a check that quietly changed its mind about a Document
 *     a handler has already read would be worse than no check
 */
public record FraudScreening(String documentId, List<FraudIndicator> indicators, Instant screenedAt) {

    public boolean foundSomething() {
        return !indicators.isEmpty();
    }

    /** The heaviest thing found, for a screen that shows one badge before the handler opens anything. */
    public FraudIndicator.Weight heaviest() {
        return indicators.stream()
                .map(FraudIndicator::weight)
                .max(Comparator.naturalOrder())
                .orElse(null);
    }
}
