package com.example.aiworkshop.tasks.task_6_case_summary;

import com.example.aiworkshop.tasks.task_6_case_summary.agent.CaseStatusWriter;
import com.example.aiworkshop.tasks.task_6_case_summary.agent.CaseSummarizer;
import com.example.aiworkshop.tasks.task_6_case_summary.store.CaseSummaryStore;
import com.example.aiworkshop.tasks.task_1_first_agent.model.Case;
import com.example.aiworkshop.tasks.task_1_first_agent.model.CaseStatus;
import com.example.aiworkshop.tasks.task_5_fraud_detection.model.FraudScreening;
import com.example.aiworkshop.tasks.task_3_document_agent.model.UploadedDocument;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Everything task 5 offers the rest of the application: what a case's documents say together, and
 * the short situation report beside it.
 *
 * <p>Both agents live behind this one door, which is what keeps the cost in one place. The summary
 * is cached against the documents it was written over — add one and it is recomputed, open the case
 * twice and it is not — and that decision is invisible to every caller, which is the point.
 */
@Service
public class SummaryDesk {

    private final CaseSummaryStore summaries;
    private final CaseSummarizer summarizer;
    private final CaseStatusWriter statusWriter;

    public SummaryDesk(CaseSummaryStore summaries, CaseSummarizer summarizer, CaseStatusWriter statusWriter) {
        this.summaries = summaries;
        this.summarizer = summarizer;
        this.statusWriter = statusWriter;
    }

    /** What the documents say together. Read from the cache unless they have changed since. */
    public String summaryOf(Case theCase, List<UploadedDocument> attached) {
        String caseId = theCase.id();
        List<String> writtenOver = attached.stream().map(UploadedDocument::id).toList();
        return summaries.find(caseId, writtenOver).orElseGet(() -> {
            String summary = summarizer.summarise(
                    theCase.type().label(),
                    attached.stream().map(DocumentForSummary::of).toList());
            summaries.save(caseId, writtenOver, summary);
            return summary;
        });
    }

    /** The cheap agent beside the expensive one: derived facts in, one short report out. */
    /**
     * The line at the top of the handler's screen.
     *
     * <p>The screenings are here because without them this sentence once read "ready for decision"
     * on a case whose only document had been flagged as sent twice. A status note that recommends
     * deciding, while something sits unlooked-at underneath it, is worse than no status note: it is
     * the one part of the screen a handler is entitled to read instead of the rest.
     *
     * <p>Passed as sentences rather than as the screening records themselves, so the agent is given
     * facts already worked out — the same way it is given the status and the outstanding list, and
     * for the same reason.
     */
    public String statusNote(
            String caseType,
            CaseStatus status,
            List<String> outstanding,
            List<String> blockedReasons,
            List<FraudScreening> screenings) {
        return statusWriter.write(caseType, status, outstanding, blockedReasons, whatWasFlagged(screenings));
    }

    private static List<String> whatWasFlagged(List<FraudScreening> screenings) {
        return screenings.stream()
                .flatMap(screening -> screening.indicators().stream()
                        .map(indicator -> "%s (%s): %s".formatted(indicator.kind(), indicator.weight(), indicator.detail())))
                .toList();
    }
}
