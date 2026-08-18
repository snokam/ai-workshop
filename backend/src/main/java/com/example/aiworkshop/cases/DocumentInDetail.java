package com.example.aiworkshop.cases;

import com.example.aiworkshop.document.ExtractedField;
import com.example.aiworkshop.document.QualityAssessment.Quality;
import com.example.aiworkshop.document.UploadedDocument;
import java.util.List;
import java.util.stream.Collectors;

/**
 * One Document as the detail tool returns it: everything {@link DocumentForChat} left out.
 *
 * <p>These two types are the two halves of the same decision. The index says a Document exists and
 * roughly what it is; this says what it contains and why it was judged the way it was. Keeping them
 * apart is what stops every Case Chat prompt carrying every Extraction in the Case.
 *
 * <p>Still not the file. A question this cannot answer is what the reader agent is for.
 */
public record DocumentInDetail(
        String filename,
        String category,
        String summary,
        List<ExtractedField> fields,
        Quality quality,
        String qualityReason,
        List<String> qualityIssues,
        boolean reviewed) {

    public static DocumentInDetail of(UploadedDocument document) {
        return new DocumentInDetail(
                document.filename(),
                document.analysis().category(),
                document.analysis().summary(),
                document.analysis().fields(),
                document.analysis().quality().verdict(),
                document.analysis().quality().reason(),
                document.analysis().quality().issues(),
                document.reviewed());
    }

    /**
     * Load-bearing: this <em>is</em> what comes back to the Case Chat agent when it fetches a
     * Document, not a debugging aid. Pinned by {@code DocumentInDetailTest}.
     */
    @Override
    public String toString() {
        return """
                %s — %s
                What it says: %s
                Extracted: %s
                Quality: %s — %s
                Issues: %s
                Reviewed by a case handler: %s"""
                .formatted(
                        filename,
                        category,
                        summary,
                        renderedFields(),
                        quality,
                        qualityReason,
                        qualityIssues.isEmpty() ? "none" : String.join("; ", qualityIssues),
                        reviewed ? "yes" : "no");
    }

    private String renderedFields() {
        return fields.isEmpty()
                ? "nothing could be read off this document"
                : fields.stream()
                        .map(field -> field.name() + ": " + field.value())
                        .collect(Collectors.joining(" | "));
    }
}
