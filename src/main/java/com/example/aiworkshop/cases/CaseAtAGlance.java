package com.example.aiworkshop.cases;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The Case as the Case Chat agent is handed it, before it asks for anything.
 *
 * <p>Everything a Case Handler can see without scrolling, and nothing a tool exists to fetch. The
 * Case Summary is in here because it is already written and already cached — reusing it costs
 * nothing and keeps the chat's answers consistent with the prose the handler has just read above it.
 *
 * <p>Every Proposal is included, whatever became of it. Outstanding and declined ones stop the agent
 * repeating a suggestion; confirmed ones stop it suggesting something that has already been done.
 */
public record CaseAtAGlance(
        String reference,
        CaseStatus status,
        List<String> requiredDocuments,
        List<String> outstanding,
        String summary,
        List<DocumentForChat> documents,
        List<ProposalCard> proposals) {

    /**
     * Load-bearing: this <em>is</em> the Case as rendered into the Case Chat system message. Pinned
     * by {@code CaseAtAGlanceTest}.
     */
    @Override
    public String toString() {
        return """
                Case %s
                Status: %s
                Required documents: %s
                Still outstanding: %s

                Documents attached:
                %s

                What the documents say, taken together:
                %s

                Suggestions you have already made on this case:
                %s"""
                .formatted(
                        reference,
                        status,
                        labels(requiredDocuments),
                        labels(outstanding),
                        indented(documents),
                        summary,
                        indented(proposals));
    }

    private static String labels(List<String> values) {
        return values.isEmpty() ? "none" : String.join(", ", values);
    }

    private static String indented(List<?> lines) {
        return lines.isEmpty()
                ? "  none"
                : lines.stream().map(line -> "  " + line).collect(Collectors.joining("\n"));
    }
}
