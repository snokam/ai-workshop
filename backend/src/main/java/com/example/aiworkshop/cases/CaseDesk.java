package com.example.aiworkshop.cases;

import com.example.aiworkshop.cases.proposals.DocumentRequestStore;
import com.example.aiworkshop.tasks.task_5_summary.SummaryDesk;
import com.example.aiworkshop.tasks.task_4_postprocessing.model.FraudScreening;
import com.example.aiworkshop.tasks.task_4_postprocessing.FraudScreener;
import com.example.aiworkshop.documents.store.DocumentStore;
import com.example.aiworkshop.documents.model.UploadedDocument;
import com.example.aiworkshop.cases.store.CaseStore;
import com.example.aiworkshop.cases.proposals.ProposalCard;
import com.example.aiworkshop.cases.model.CaseOverview;
import com.example.aiworkshop.cases.model.CaseDetail;
import com.example.aiworkshop.cases.model.Case;
import com.example.aiworkshop.cases.chat.ChatTurn;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CaseDesk {
    private final CaseStore cases;
    private final DocumentStore documents;
    private final DocumentRequestStore requests;
    private final SummaryDesk summaries;
    private final FraudScreener screener;

    public CaseDesk(
            CaseStore cases,
            DocumentStore documents,
            DocumentRequestStore requests,
            SummaryDesk summaries,
            FraudScreener screener) {
        this.cases = cases;
        this.documents = documents;
        this.requests = requests;
        this.summaries = summaries;
        this.screener = screener;
    }

    public List<CaseOverview> list() {
        return cases.findAll().stream().map(this::overviewOf).toList();
    }

    /**
     * One case, as the handler's screen shows it.
     *
     * <p>The conversation is passed in rather than fetched. This is what a case is; what has been
     * said about it belongs to task 6, and a desk that reached for it would have to know about a
     * task written after this one — which is the rule the layout keeps.
     */
    public CaseDetail open(String caseId, List<ProposalCard> proposals, List<ChatTurn> conversation) {
        Case theCase = cases.findById(caseId).orElseThrow(() -> new UnknownCaseException(caseId));
        List<UploadedDocument> attached = documents.findByCaseId(caseId);
        CaseOverview overview = overviewOf(theCase);

        return new CaseDetail(
                overview,
                attached,
                idsOf(theCase.countingDocuments(attached)),
                idsOf(theCase.blockedDocuments(attached)),
                summaries.summaryOf(theCase, attached),
                summaries.statusNote(
                        theCase.type().label(),
                        overview.status(),
                        overview.outstanding(),
                        whyEachBlockedDocumentIsBlocked(theCase, attached)),
                screeningsFound(attached),
                proposals,
                conversation);
    }

    private List<FraudScreening> screeningsFound(List<UploadedDocument> attached) {
        return screener.findAllFor(idsOf(attached));
    }

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
                theCase.type().label(),
                theCase.status(attached),
                theCase.requiredDocuments(),
                theCase.unmatchedRequiredDocuments(attached),
                requests.findByCaseId(theCase.id()));
    }

    private static List<String> idsOf(List<UploadedDocument> documents) {
        return documents.stream().map(UploadedDocument::id).toList();
    }

    private List<String> whyEachBlockedDocumentIsBlocked(Case theCase, List<UploadedDocument> attached) {
        return theCase.blockedDocuments(attached).stream()
                .map(document -> document.filename() + ": " + document.analysis().quality().reason())
                .toList();
    }

    public static class UnknownCaseException extends RuntimeException {
        public UnknownCaseException(String caseId) {
            super("No such case: " + caseId);
        }
    }

    public static class UnknownProposalException extends RuntimeException {
        public UnknownProposalException(String proposalId) {
            super("No such proposal: " + proposalId);
        }
    }

    public static class UnknownDocumentException extends RuntimeException {
        public UnknownDocumentException(String filename) {
            super("No document called '" + filename + "' is attached to this case.");
        }
    }
}
