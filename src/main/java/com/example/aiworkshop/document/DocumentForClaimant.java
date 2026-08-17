package com.example.aiworkshop.document;

import java.time.Instant;
import java.util.List;

/**
 * One Document as the person who uploaded it sees it.
 *
 * <p>Everything the agent worked out about their file, minus the one thing written for somebody
 * else: whether the Document tried to give the agent orders. A Claimant who is told which trick was
 * spotted has been handed a list of the ones that were not, and an honest Claimant — the
 * overwhelming majority — gains nothing from a sentence about manipulation they never attempted.
 *
 * <p>Same reasoning as {@code DocumentForSummary}, applied to a screen rather than an agent: what an
 * audience is given is a decision, so it is written down as a type instead of being left to whoever
 * next edits a controller. The fraud Screening never appears here either, and cannot — it is not
 * held on {@link UploadedDocument} at all.
 *
 * @param analysis the agent's findings, less the manipulation report
 */
public record DocumentForClaimant(
        String id,
        String caseId,
        String filename,
        String contentType,
        long sizeBytes,
        Instant uploadedAt,
        AnalysisForClaimant analysis,
        boolean reviewed) {

    public static DocumentForClaimant of(UploadedDocument document) {
        DocumentAnalysis analysis = document.analysis();
        return new DocumentForClaimant(
                document.id(),
                document.caseId(),
                document.filename(),
                document.contentType(),
                document.sizeBytes(),
                document.uploadedAt(),
                new AnalysisForClaimant(
                        analysis.category(),
                        analysis.summary(),
                        analysis.fields(),
                        analysis.matchedRequiredDocument(),
                        analysis.matchConfidence(),
                        analysis.quality()),
                document.reviewed());
    }

    public static List<DocumentForClaimant> of(List<UploadedDocument> documents) {
        return documents.stream().map(DocumentForClaimant::of).toList();
    }

    /**
     * {@link DocumentAnalysis} with {@link ManipulationAttempt} left out — and written out
     * component by component rather than delegating, so that adding a component to the analysis is a
     * decision about who sees it rather than something that happens by default.
     */
    public record AnalysisForClaimant(
            String category,
            String summary,
            List<ExtractedField> fields,
            String matchedRequiredDocument,
            MatchConfidence matchConfidence,
            QualityAssessment quality) {}
}
