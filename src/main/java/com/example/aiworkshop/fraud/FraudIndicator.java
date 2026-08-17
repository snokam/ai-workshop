package com.example.aiworkshop.fraud;

import java.util.List;

/**
 * One thing a {@link FraudCheck} noticed about an uploaded file.
 *
 * <p>An Indicator is an observation, never a conclusion. "This exact image is published on four
 * websites" is something a Case Handler can act on; "this claim is fraudulent" is not something any
 * check here is entitled to say, and nothing in this package says it. The handler decides.
 *
 * <p>Read alongside {@code QualityAssessment}, which asks a different question. Quality asks whether
 * the file can be worked with at all. Screening asks whether the file is what it appears to be —
 * and, unlike Quality, it is never shown to the Claimant.
 *
 * @param kind which check found this, so the screen can group and the handler can learn to weigh
 *     each one
 * @param weight how much attention it deserves on its own, before a handler sees the rest
 * @param detail one plain sentence stating what was found. Written for a Case Handler, who is the
 *     only person who will ever read it
 * @param evidence the specifics behind the sentence — URLs, a capture date, the id of an earlier
 *     Document. What a handler needs in order to disagree with the check
 */
public record FraudIndicator(Kind kind, Weight weight, String detail, List<String> evidence) {

    public static FraudIndicator of(Kind kind, Weight weight, String detail, List<String> evidence) {
        return new FraudIndicator(kind, weight, detail, List.copyOf(evidence));
    }

    /**
     * What was noticed. Named after the observation rather than the conclusion it might support:
     * {@code SEEN_ONLINE}, not {@code STOCK_PHOTO}, because a photo can be online for reasons that
     * have nothing to do with the person who uploaded it.
     */
    public enum Kind {
        /** The image, or a crop of it, is published somewhere on the web. */
        SEEN_ONLINE,
        /** These exact bytes have been uploaded before — to this Case or another one. */
        ALREADY_UPLOADED,
        /** The image carries the fingerprint of editing software. */
        EDITED_IN_SOFTWARE,
        /** A photo with none of the metadata a camera writes. */
        NO_CAMERA_ORIGIN,
        /** The capture date the file carries does not sit where it should relative to the upload. */
        DATE_OUT_OF_PLACE,
        /** The document contained text aimed at the agent reading it rather than at a human. */
        ADDRESSED_THE_AGENT
    }

    /**
     * Three values for the same reason {@code Quality} has three: with two, every faint signal reads
     * as an accusation. Most honest uploads will produce a {@link #NOTE} or two, and a screen that
     * cannot show them quietly is a screen handlers stop reading.
     */
    public enum Weight {
        /** True, and worth having on the record. Common on entirely honest uploads. */
        NOTE,
        /** Unusual enough that a handler should look at the file themselves. */
        CONCERN,
        /** Hard to explain innocently. Still not a verdict — it is the handler who decides. */
        STRONG
    }
}
