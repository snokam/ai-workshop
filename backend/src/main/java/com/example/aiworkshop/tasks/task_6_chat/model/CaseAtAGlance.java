package com.example.aiworkshop.tasks.task_6_chat.model;

import com.example.aiworkshop.cases.model.Case;
import com.example.aiworkshop.cases.proposals.ProposalCard;
import com.example.aiworkshop.cases.model.CaseStatus;
import java.util.List;
import java.util.stream.Collectors;

public record CaseAtAGlance(
        String reference,
        String typeLabel,
        CaseStatus status,
        List<String> requiredDocuments,
        List<String> outstanding,
        String summary,
        List<DocumentForChat> documents,
        List<ProposalCard> proposals) {
    @Override
    public String toString() {
        return """
                Case %s — %s
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
                        typeLabel,
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
