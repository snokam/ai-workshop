package com.example.aiworkshop.tasks.task_5_claim_summary_choosing_models;

import com.example.aiworkshop.tasks.task_5_claim_summary_choosing_models.agent.ClaimStatusWriter;
import com.example.aiworkshop.tasks.task_5_claim_summary_choosing_models.agent.ClaimSummarizer;
import com.example.aiworkshop.tasks.task_5_claim_summary_choosing_models.store.ClaimSummaryStore;
import com.example.aiworkshop.tasks.task_1_first_agent.model.Claim;
import com.example.aiworkshop.tasks.task_1_first_agent.model.ClaimStatus;
import com.example.aiworkshop.tasks.task_3_document_agent.model.UploadedDocument;
import java.util.List;
import dev.langchain4j.service.Result;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Everything task 5 offers the rest of the application: what a claim's documents say together, and
 * the short situation report beside it.
 *
 * <p>Both agents live behind this one door, which is what keeps the cost in one place. The summary
 * is cached against the documents it was written over — add one and it is recomputed, open the claim
 * twice and it is not — and that decision is invisible to every caller, which is the point.
 */
@Service
public class SummaryDesk {

    private static final Logger log = LoggerFactory.getLogger(SummaryDesk.class);

    private final ClaimSummaryStore summaries;
    private final ClaimSummarizer summarizer;
    private final ClaimStatusWriter statusWriter;

    public SummaryDesk(ClaimSummaryStore summaries, ClaimSummarizer summarizer, ClaimStatusWriter statusWriter) {
        this.summaries = summaries;
        this.summarizer = summarizer;
        this.statusWriter = statusWriter;
    }

    /** What the documents say together. Read from the cache unless they have changed since. */
    public String summaryOf(Claim theClaim, List<UploadedDocument> attached) {
        String claimId = theClaim.id();
        List<String> writtenOver = attached.stream().map(UploadedDocument::id).toList();
        return summaries.find(claimId, writtenOver).orElseGet(() -> {
            String summary = costOf(
                    "summary",
                    () -> summarizer.summarise(
                            theClaim.type().label(),
                            attached.stream().map(DocumentForSummary::of).toList()));
            summaries.save(claimId, writtenOver, summary);
            return summary;
        });
    }

    /**
     * The line at the top of the handler's screen: the cheap agent beside the expensive one, derived
     * facts in and one short report out.
     *
     * <p>Everything it is given has been worked out already — the status, what the claim is still
     * waiting on, why anything is blocked. The agent is asked to write a sentence, not to decide
     * what is true, which is the difference between a status note you can trust and one you have to
     * check.
     */
    public String statusNote(
            String claimId,
            String claimType,
            ClaimStatus status,
            List<String> outstanding,
            List<String> blockedReasons) {
        List<String> derivedFrom = List.of(claimType, status.name(), outstanding.toString(), blockedReasons.toString());

        return summaries.findStatusNote(claimId, derivedFrom).orElseGet(() -> {
            String note = costOf("status line", () -> statusWriter.write(claimType, status, outstanding, blockedReasons));
            summaries.saveStatusNote(claimId, derivedFrom, note);
            return note;
        });
    }

    /**
     * Runs one call and says what it cost.
     *
     * <p>Both agents return {@link Result}, which carries the token usage beside the answer. That is
     * the only reason the choice in {@code SummaryConfig.modelFor} can be made on evidence rather
     * than on a feeling about which job sounds harder.
     *
     * <p>Read the totals rather than adding the parts: input plus output does not make the total on a
     * reasoning model, and the difference is thinking — spent, billed, and invisible in both.
     */
    private static String costOf(String what, Supplier<Result<String>> call) {
        long start = System.nanoTime();
        Result<String> result = call.get();
        log.info(
                "{} took {}ms and {} tokens ({})",
                what,
                (System.nanoTime() - start) / 1_000_000,
                result.tokenUsage() == null ? "?" : result.tokenUsage().totalTokenCount(),
                result.tokenUsage());
        return result.content();
    }
}
