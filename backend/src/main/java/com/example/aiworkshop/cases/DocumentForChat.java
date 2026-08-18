package com.example.aiworkshop.cases;

import com.example.aiworkshop.document.QualityAssessment.Quality;
import com.example.aiworkshop.document.UploadedDocument;

/**
 * One Document as the Case Chat agent first sees it: one line, enough to be talked about by name.
 *
 * <p>The dividing line the whole feature is built on. The agent is handed an index of the Case and
 * looks closer on demand, so this deliberately carries no Extraction values and none of the Quality
 * Assessment's prose — those are what the detail tool exists to fetch, and an index that already
 * contained them would make the tool pointless and the prompt enormous.
 *
 * <p>What it does carry beyond the spec's four facts is whether a Case Handler has already Reviewed
 * the Document. Without it the agent reads {@code POOR} off a Document that has already been cleared
 * and suggests a Review that has already happened.
 *
 * @param matchedRequiredDocument what the intake agent said this file satisfies, or {@code null}
 * @param counting whether this is the Document the Case Status was actually derived from — a matched
 *     Document that is not counting has been superseded by a later upload of the same thing
 */
public record DocumentForChat(
        String filename,
        String category,
        String matchedRequiredDocument,
        boolean counting,
        Quality quality,
        boolean reviewed) {

    public static DocumentForChat of(UploadedDocument document, boolean counting) {
        return new DocumentForChat(
                document.filename(),
                document.analysis().category(),
                document.analysis().matchedRequiredDocument(),
                counting,
                document.analysis().quality().verdict(),
                document.reviewed());
    }

    /**
     * Load-bearing: this <em>is</em> one line of the Case Chat prompt, not a debugging aid. Pinned by
     * {@code DocumentForChatTest}.
     */
    @Override
    public String toString() {
        return "%s — %s — %s — quality %s".formatted(filename, category, standing(), renderedQuality());
    }

    private String standing() {
        if (matchedRequiredDocument == null) {
            return "counts as nothing this case requires";
        }
        return counting
                ? "counts as \"%s\"".formatted(matchedRequiredDocument)
                : "superseded by a later \"%s\"".formatted(matchedRequiredDocument);
    }

    private String renderedQuality() {
        return reviewed ? quality + ", already reviewed by a case handler" : quality.toString();
    }
}
