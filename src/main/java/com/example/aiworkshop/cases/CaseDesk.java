package com.example.aiworkshop.cases;

import com.example.aiworkshop.document.DocumentStore;
import com.example.aiworkshop.document.UploadedDocument;
import com.example.aiworkshop.tasks.task_2_postprocessing.FraudScreener;
import com.example.aiworkshop.tasks.task_2_postprocessing.model.FraudScreening;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * What the Case Handler's screen talks to.
 *
 * <p>Deliberately thin. The rules about what each status means live in {@link Case#status}, where
 * they are a pure function; this class fetches, delegates and hands back. It holds the two
 * handler-side agents as collaborators, and is the only place either of them is called.
 *
 * <p>Note which methods call a model and which do not. {@link #list} is pure lookup, so browsing the
 * case list is never gated on a model call per Case; {@link #open} makes both calls, once, for the
 * one Case the handler actually asked for.
 */
@Service
public class CaseDesk {

    private final CaseStore cases;
    private final DocumentStore documents;
    private final CaseSummaryStore summaries;
    private final CaseSummarizer summarizer;
    private final CaseStatusWriter statusWriter;
    private final FraudScreener screener;

    CaseDesk(
            CaseStore cases,
            DocumentStore documents,
            CaseSummaryStore summaries,
            CaseSummarizer summarizer,
            CaseStatusWriter statusWriter,
            FraudScreener screener) {
        this.cases = cases;
        this.documents = documents;
        this.summaries = summaries;
        this.summarizer = summarizer;
        this.statusWriter = statusWriter;
        this.screener = screener;
    }

    /** Every Case with its derived status. No model calls — this is a list the handler skims. */
    public List<CaseOverview> list() {
        return cases.findAll().stream().map(this::overviewOf).toList();
    }

    /**
     * One Case, with both agents run over it. Two model calls, made once, for the Case the handler
     * actually opened.
     */
    public CaseDetail open(String caseId) {
        Case theCase = cases.findById(caseId).orElseThrow(() -> new UnknownCaseException(caseId));
        List<UploadedDocument> attached = documents.findByCaseId(caseId);
        CaseOverview overview = overviewOf(theCase);

        String statusNote = statusWriter.write(
                overview.status(), overview.outstanding(), whyEachBlockedDocumentIsBlocked(theCase, attached));
        return new CaseDetail(
                overview,
                attached,
                idsOf(theCase.countingDocuments(attached)),
                idsOf(theCase.blockedDocuments(attached)),
                summaryOf(caseId, attached),
                statusNote,
                screeningsFound(attached));
    }

    private List<FraudScreening> screeningsFound(List<UploadedDocument> attached) {
        return screener.findAllFor(idsOf(attached));
    }

    /**
     * The Case Summary, written once per set of Documents. The Documents themselves are the only
     * thing it depends on, so re-running the agent for a handler who opened the same Case twice
     * would be paying for the same paragraphs again.
     */
    private String summaryOf(String caseId, List<UploadedDocument> attached) {
        List<String> writtenOver = idsOf(attached);
        return summaries.find(caseId, writtenOver).orElseGet(() -> {
            String summary = summarizer.summarise(
                    attached.stream().map(DocumentForSummary::of).toList());
            summaries.save(caseId, writtenOver, summary);
            return summary;
        });
    }

    /**
     * A Case Handler's confirmation that a Document is good enough to work with despite its Quality
     * Assessment. Recorded on the Document, so the Case's status follows on its own.
     */
    public void review(String documentId) {
        documents.findById(documentId)
                .map(UploadedDocument::markReviewed)
                .ifPresent(documents::save);
    }

    private CaseOverview overviewOf(Case theCase) {
        List<UploadedDocument> attached = documents.findByCaseId(theCase.id());
        return new CaseOverview(
                theCase.id(),
                theCase.reference(),
                theCase.status(attached),
                theCase.requiredDocuments(),
                theCase.unmatchedRequiredDocuments(attached));
    }

    /** The screen already has the Documents; what it is missing is which of them a rule picked out. */
    private static List<String> idsOf(List<UploadedDocument> documents) {
        return documents.stream().map(UploadedDocument::id).toList();
    }

    /**
     * Filename and the agent's own sentence about the file — a derived fact about the Document as an
     * artefact, not a word of what it says. That distinction is the whole reason this agent is cheap.
     */
    private List<String> whyEachBlockedDocumentIsBlocked(Case theCase, List<UploadedDocument> attached) {
        return theCase.blockedDocuments(attached).stream()
                .map(document -> document.filename() + ": " + document.analysis().quality().reason())
                .toList();
    }

    /** Thrown when the handler screen asks for a Case that does not exist. Mapped to 404. */
    public static class UnknownCaseException extends RuntimeException {
        UnknownCaseException(String caseId) {
            super("No such case: " + caseId);
        }
    }
}
