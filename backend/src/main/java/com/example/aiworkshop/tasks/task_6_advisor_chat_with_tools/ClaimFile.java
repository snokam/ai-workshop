package com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools;

import com.example.aiworkshop.tasks.task_1_first_agent.ClaimDesk;
import com.example.aiworkshop.tasks.task_1_first_agent.model.Claim;
import com.example.aiworkshop.tasks.task_1_first_agent.model.ClaimOverview;
import com.example.aiworkshop.tasks.task_3_document_agent.progress.ClaimDocuments;
import com.example.aiworkshop.tasks.task_3_document_agent.model.UploadedDocument;
import com.example.aiworkshop.tasks.task_3_document_agent.store.DocumentStore;
import com.example.aiworkshop.tasks.task_5_claim_summary.SummaryDesk;
import com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools.model.ClaimDetail;
import com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools.model.ChatTurn;
import com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools.proposals.ProposalCard;
import com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools.store.DocumentRequestStore;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * One claim, opened: everything the tasks before it know about it, on one screen.
 *
 * <p>This is the only class that touches all of them — the claim from task 1, the uploads from task
 * 3, the summary from task 5, the conversation and the outstanding requests from task 6 — and it
 * lives here because here is the last of them. A composition belongs with the newest thing it
 * composes, or it forces an earlier task to know about a later one.
 */
@Service
public class ClaimFile {
    private final ClaimDesk claims;
    private final DocumentStore documents;
    private final DocumentRequestStore requests;
    private final SummaryDesk summaries;

    public ClaimFile(
            ClaimDesk claims,
            DocumentStore documents,
            DocumentRequestStore requests,
            SummaryDesk summaries) {
        this.claims = claims;
        this.documents = documents;
        this.requests = requests;
        this.summaries = summaries;
    }

    /**
     * The conversation and the proposals are passed in rather than fetched, so that the shape of
     * this method says plainly what a claim screen is made of.
     */
    public ClaimDetail open(String claimId, List<ProposalCard> proposals, List<ChatTurn> conversation) {
        Claim theClaim = claims.require(claimId);
        List<UploadedDocument> attached = documents.findByClaimId(claimId);
        ClaimOverview overview = claims.overviewOf(theClaim);

        return new ClaimDetail(
                overview,
                attached,
                idsOf(ClaimDocuments.countingDocuments(theClaim, attached)),
                idsOf(ClaimDocuments.blockedDocuments(theClaim, attached)),
                summaries.summaryOf(theClaim, attached),
                summaries.statusNote(
                        theClaim.type().label(),
                        overview.status(),
                        overview.outstanding(),
                        whyEachBlockedDocumentIsBlocked(theClaim, attached)),
                requests.findByClaimId(claimId),
                proposals,
                conversation);
    }

    private static List<String> idsOf(List<UploadedDocument> documents) {
        return documents.stream().map(UploadedDocument::id).toList();
    }

    private List<String> whyEachBlockedDocumentIsBlocked(Claim theClaim, List<UploadedDocument> attached) {
        return ClaimDocuments.blockedDocuments(theClaim, attached).stream()
                .map(document -> document.filename() + ": " + document.analysis().quality().reason())
                .toList();
    }
}
