package com.example.aiworkshop.tasks.task_5_summary;

import com.example.aiworkshop.tasks.task_6_chat.model.DocumentInDetail;
import com.example.aiworkshop.documents.model.QualityAssessment;
import com.example.aiworkshop.documents.model.UploadedDocument;
import com.example.aiworkshop.tasks.task_2_document_agent.model.ExtractedField;
import com.example.aiworkshop.documents.model.QualityAssessment.Quality;
import java.util.List;
import java.util.stream.Collectors;

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

    // ── To set this task again ────────────────────────────────────────────────────────
    // TODO — task 6, part 2. Decide what the expensive agent is shown.
    //
    // Delete the components of this record and write them again from what the summariser needs. It
    // is handed one of these per document, in one prompt, on every screen load — so every field is
    // paid for once per document per open, and the ones nobody reads are the whole of the cost with
    // none of the value.
    //
    // Look at what is deliberately missing before you add it back. There are no bytes here: the
    // file was read in task 2 and reading it again would pay twice for one answer and risk two
    // descriptions of one document on the same screen. Compare it with DocumentInDetail, which the
    // chat's tool fetches for a single document when someone actually asks.
    //
    // Then add a component and watch the summary change. That is the whole trade: this is the one
    // agent whose prompt grows with the case.
}
