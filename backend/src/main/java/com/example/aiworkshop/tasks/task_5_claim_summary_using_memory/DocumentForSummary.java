package com.example.aiworkshop.tasks.task_5_claim_summary_using_memory;

import com.example.aiworkshop.tasks.task_3_document_agent.model.UploadedDocument;
import com.example.aiworkshop.tasks.task_3_document_agent.model.ExtractedField;
import com.example.aiworkshop.tasks.task_3_document_agent.model.QualityAssessment.Quality;
import java.util.List;
import java.util.stream.Collectors;

/**
 * What the summariser is shown, once per document, on every open.
 *
 * <p>Every component here is paid for every time, and the ones nobody reads are the whole of the
 * cost with none of the value. Worth noticing what is deliberately missing: there are no bytes. The
 * file was read in task 3, and reading it again would pay twice for one answer and risk two
 * descriptions of one document on the same screen.
 */
public record DocumentForSummary(
        String filename, String category, String summary, List<ExtractedField> fields, Quality quality) {
    public static DocumentForSummary of(UploadedDocument document) {
        return new DocumentForSummary(
                document.filename(),
                document.analysis().category(),
                document.analysis().summary(),
                document.analysis().fields(),
                document.analysis().quality().verdict());
    }

    @Override
    public String toString() {
        return "%s — %s (%s)%n  %s%n  %s".formatted(filename, category, quality, summary, renderedFields());
    }

    private String renderedFields() {
        return fields.stream()
                .map(field -> field.name() + ": " + field.value())
                .collect(Collectors.joining(" | "));
    }

}
