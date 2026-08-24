package com.example.aiworkshop.tasks.task_6_case_summary;

import com.example.aiworkshop.tasks.task_3_document_agent.model.UploadedDocument;
import com.example.aiworkshop.tasks.task_3_document_agent.model.ExtractedField;
import com.example.aiworkshop.tasks.task_3_document_agent.model.QualityAssessment.Quality;
import java.util.List;
import java.util.stream.Collectors;

// TODO — task 6, part 2. Decide what it is shown.
//
// This record is what the summariser is shown, once per document, on every open. Every component is
// paid for every time, and the ones nobody reads are the whole of the cost with none of the value.
//
// Look at what is deliberately missing before adding anything. There are no bytes: the file was read
// in task 3, and reading it again would pay twice for one answer and risk two descriptions of one
// document on the same screen.
//
// Add a component and watch the summary change. That is the trade this task is about.

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
