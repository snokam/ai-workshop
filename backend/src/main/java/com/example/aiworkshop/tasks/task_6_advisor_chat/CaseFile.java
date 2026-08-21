package com.example.aiworkshop.tasks.task_6_advisor_chat;

import com.example.aiworkshop.tasks.task_1_first_agent.CaseDesk;
import com.example.aiworkshop.tasks.task_1_first_agent.model.Case;
import com.example.aiworkshop.tasks.task_1_first_agent.model.CaseOverview;
import com.example.aiworkshop.tasks.task_2_document_agent.CaseDocuments;
import com.example.aiworkshop.tasks.task_2_document_agent.model.UploadedDocument;
import com.example.aiworkshop.tasks.task_2_document_agent.store.DocumentStore;
import com.example.aiworkshop.tasks.task_4_fraud_detection.FraudScreener;
import com.example.aiworkshop.tasks.task_5_case_summary.SummaryDesk;
import com.example.aiworkshop.tasks.task_6_advisor_chat.model.CaseDetail;
import com.example.aiworkshop.tasks.task_6_advisor_chat.model.ChatTurn;
import com.example.aiworkshop.tasks.task_6_advisor_chat.proposals.ProposalCard;
import com.example.aiworkshop.tasks.task_6_advisor_chat.store.DocumentRequestStore;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * One case, opened: everything six tasks know about it, on one screen.
 *
 * <p>This is the only class that touches all of them — the case from task 1, the uploads from task
 * 2, the screenings from task 4, the summary from task 5, the conversation and the outstanding
 * requests from task 6 — and it lives here because here is the last of them. A composition belongs
 * with the newest thing it composes, or it forces an earlier task to know about a later one.
 */
@Service
public class CaseFile {
    private final CaseDesk cases;
    private final DocumentStore documents;
    private final DocumentRequestStore requests;
    private final SummaryDesk summaries;
    private final FraudScreener screener;

    public CaseFile(
            CaseDesk cases,
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

    /**
     * The conversation and the proposals are passed in rather than fetched, so that the shape of
     * this method says plainly what a case screen is made of.
     */
    public CaseDetail open(String caseId, List<ProposalCard> proposals, List<ChatTurn> conversation) {
        Case theCase = cases.require(caseId);
        List<UploadedDocument> attached = documents.findByCaseId(caseId);
        CaseOverview overview = cases.overviewOf(theCase);

        return new CaseDetail(
                overview,
                attached,
                idsOf(CaseDocuments.countingDocuments(theCase, attached)),
                idsOf(CaseDocuments.blockedDocuments(theCase, attached)),
                summaries.summaryOf(theCase, attached),
                summaries.statusNote(
                        theCase.type().label(),
                        overview.status(),
                        overview.outstanding(),
                        whyEachBlockedDocumentIsBlocked(theCase, attached)),
                screener.findAllFor(idsOf(attached)),
                requests.findByCaseId(caseId),
                proposals,
                conversation);
    }

    private static List<String> idsOf(List<UploadedDocument> documents) {
        return documents.stream().map(UploadedDocument::id).toList();
    }

    private List<String> whyEachBlockedDocumentIsBlocked(Case theCase, List<UploadedDocument> attached) {
        return CaseDocuments.blockedDocuments(theCase, attached).stream()
                .map(document -> document.filename() + ": " + document.analysis().quality().reason())
                .toList();
    }
}
