package com.example.aiworkshop.cases;

import com.example.aiworkshop.document.ExtractedField;
import com.example.aiworkshop.document.QualityAssessment.Quality;
import com.example.aiworkshop.document.UploadedDocument;
import java.util.List;
import java.util.stream.Collectors;

/**
 * One Document as the {@link CaseSummarizer} sees it — what it says, and nothing about the file it
 * arrived as.
 *
 * <p>This type exists because the agent used to be handed {@link UploadedDocument} directly, and
 * LangChain4j renders a template variable by calling {@code toString()} on it. The prompt was
 * therefore a record dump — ids, MIME type, byte count — that nobody had chosen and that changed
 * silently whenever a component was added to the record. What an agent is given is a decision, so it
 * is written down here.
 *
 * <p>The quality verdict comes across as the bare enum on purpose. It tells the summarizer how far to
 * trust a Document when drawing connections between them, without handing over
 * {@link QualityAssessment#reason} — prose the handler can already read on the Document itself, and
 * which the agent will otherwise paraphrase back into the Case Summary.
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

    /**
     * Load-bearing: this <em>is</em> the text rendered into the Case Summary prompt, not a debugging
     * aid. Pinned by {@code DocumentForSummaryTest}.
     */
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
