package com.example.aiworkshop.tasks.task_6_chat.model;

import com.example.aiworkshop.tasks.task_2_document_agent.model.UploadedDocument;
import com.example.aiworkshop.tasks.task_2_document_agent.model.ExtractedField;
import com.example.aiworkshop.tasks.task_2_document_agent.model.QualityAssessment.Quality;
import java.util.List;
import java.util.stream.Collectors;

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
