package com.example.aiworkshop.cases.model;

import com.example.aiworkshop.documents.model.UploadedDocument;
import com.example.aiworkshop.documents.model.ExtractedField;
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
}
