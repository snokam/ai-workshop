package com.example.aiworkshop.cases.chat;

import com.example.aiworkshop.documents.model.UploadedDocument;
import com.example.aiworkshop.documents.model.QualityAssessment.Quality;

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
