package com.example.aiworkshop.tasks.task_6_advisor_chat;

import com.example.aiworkshop.tasks.task_6_advisor_chat.agent.ClaimChatAgent;
import com.example.aiworkshop.tasks.task_6_advisor_chat.agent.DocumentReader;
import com.example.aiworkshop.tasks.task_1_first_agent.ClaimDesk;
import com.example.aiworkshop.tasks.task_6_advisor_chat.store.ClaimChatStore;
import com.example.aiworkshop.tasks.task_6_advisor_chat.model.ChatAnswer;
import com.example.aiworkshop.tasks.task_6_advisor_chat.model.ChatTurn;
import com.example.aiworkshop.tasks.task_6_advisor_chat.model.ToolCall;
import com.example.aiworkshop.tasks.task_1_first_agent.model.Claim;
import com.example.aiworkshop.tasks.task_6_advisor_chat.proposals.DocumentRequest;
import com.example.aiworkshop.tasks.task_6_advisor_chat.proposals.DocumentRequestProposal;
import com.example.aiworkshop.tasks.task_6_advisor_chat.store.DocumentRequestStore;
import com.example.aiworkshop.tasks.task_6_advisor_chat.proposals.Proposal;
import com.example.aiworkshop.tasks.task_6_advisor_chat.proposals.ProposalCard;
import com.example.aiworkshop.tasks.task_6_advisor_chat.proposals.ProposalState;
import com.example.aiworkshop.tasks.task_6_advisor_chat.store.ProposalStore;
import com.example.aiworkshop.tasks.task_6_advisor_chat.proposals.ReviewProposal;
import com.example.aiworkshop.tasks.task_1_first_agent.store.ClaimStore;
import com.example.aiworkshop.tasks.task_3_document_agent.model.UploadedDocument;
import com.example.aiworkshop.tasks.task_3_document_agent.store.DocumentFiles;
import com.example.aiworkshop.tasks.task_3_document_agent.progress.ClaimDocuments;
import com.example.aiworkshop.tasks.task_3_document_agent.progress.DocumentReview;
import com.example.aiworkshop.tasks.task_3_document_agent.store.DocumentStore;
import com.example.aiworkshop.tasks.task_5_claim_summary.SummaryDesk;
import com.example.aiworkshop.tasks.task_6_advisor_chat.model.ClaimAtAGlance;
import com.example.aiworkshop.tasks.task_6_advisor_chat.model.DocumentForChat;
import com.example.aiworkshop.tasks.task_6_advisor_chat.model.DocumentInDetail;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.service.Result;
import java.util.List;
import java.util.UUID;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * Everything task 6 offers: one conversation about one claim, and the proposals it puts up.
 *
 * <p>The tools talk to this and nothing else, which is why none of them contains any logic — an
 * agent that could reach past this door would end up with a private version of the claim.
 *
 * <p>It reads task 5's summary rather than the documents. That is what the opening context is: the
 * agent starts knowing what the documents say together, and uses its tools for the detail.
 */
@Service
public class ChatDesk {

    private final ClaimStore claims;
    private final DocumentStore documents;
    private final DocumentFiles files;
    private final ProposalStore proposals;
    private final DocumentRequestStore requests;
    private final ClaimChatStore chats;
    private final ClaimChatAgent chatAgent;
    private final DocumentReader reader;
    private final SummaryDesk summaries;
    private final ClaimDesk desk;
    private final DocumentReview review;

    public ChatDesk(
            ClaimStore claims,
            DocumentStore documents,
            DocumentFiles files,
            ProposalStore proposals,
            DocumentRequestStore requests,
            ClaimChatStore chats,
            // The tools call this desk and this desk calls the agent that owns them, so one of the
            // two has to be resolved late. Spring cannot break the circle on its own.
            @Lazy ClaimChatAgent chatAgent,
            DocumentReader reader,
            SummaryDesk summaries,
            ClaimDesk desk,
            DocumentReview review) {
        this.claims = claims;
        this.documents = documents;
        this.files = files;
        this.proposals = proposals;
        this.requests = requests;
        this.chats = chats;
        this.chatAgent = chatAgent;
        this.reader = reader;
        this.summaries = summaries;
        this.desk = desk;
        this.review = review;
    }

    public ChatAnswer chat(String claimId, String question) {
        Claim theClaim = caseOrFail(claimId);
        List<String> before = idsOfProposalsOn(claimId);

        Result<String> answered = chatAgent.answer(claimId, question, glanceAt(theClaim));

        List<String> raised = idsOfProposalsOn(claimId).stream()
                .filter(id -> !before.contains(id))
                .toList();
        ChatTurn turn = new ChatTurn(
                question,
                answered.content(),
                answered.toolExecutions().stream().map(ToolCall::of).toList(),
                raised);
        chats.append(claimId, turn);
        return new ChatAnswer(turn, proposalsOn(claimId));
    }

    public List<ChatTurn> turnsOn(String claimId) {
        return chats.findByCaseId(claimId);
    }

    public List<ProposalCard> proposalsOn(String claimId) {
        return proposals.findByCaseId(claimId).stream().map(ProposalCard::of).toList();
    }

    public String documentDetail(String claimId, String filename) {
        return DocumentInDetail.of(documentIn(claimId, filename)).toString();
    }

    public String readDocument(String claimId, String filename, String question) {
        UploadedDocument document = documentIn(claimId, filename);
        return reader.read(
                List.of(TextContent.from("Look at the attached file."), files.contentOf(document)), question);
    }

    public ProposalCard proposeReview(String claimId, String filename, String reason) {
        UploadedDocument document = documentIn(claimId, filename);
        return raise(new ReviewProposal(
                UUID.randomUUID().toString(),
                claimId,
                document.id(),
                document.filename(),
                reason,
                ProposalState.PROPOSED));
    }

    public ProposalCard proposeDocumentRequest(String claimId, String label, String reason) {
        caseOrFail(claimId);
        return raise(new DocumentRequestProposal(
                UUID.randomUUID().toString(), claimId, label, reason, ProposalState.PROPOSED));
    }

    public List<DocumentRequest> requestsOn(String claimId) {
        return requests.findByCaseId(claimId);
    }

    public ProposalCard confirm(String proposalId) {
        Proposal proposal = answerable(proposalId);
        if (!proposal.isOutstanding()) {
            return ProposalCard.of(proposal);
        }
        switch (proposal) {
            case ReviewProposal reviewProposal -> review.markReviewed(reviewProposal.documentId());
            case DocumentRequestProposal requestProposal ->
                requests.save(new DocumentRequest(
                        UUID.randomUUID().toString(),
                        requestProposal.claimId(),
                        requestProposal.label(),
                        requestProposal.reason()));
        }
        return raise(proposal.withState(ProposalState.CONFIRMED));
    }

    public ProposalCard decline(String proposalId) {
        Proposal proposal = answerable(proposalId);
        return proposal.isOutstanding()
                ? raise(proposal.withState(ProposalState.DECLINED))
                : ProposalCard.of(proposal);
    }

    private ClaimAtAGlance glanceAt(Claim theClaim) {
        List<UploadedDocument> attached = documents.findByCaseId(theClaim.id());
        List<String> counting =
                ClaimDocuments.countingDocuments(theClaim, attached).stream().map(UploadedDocument::id).toList();
        return new ClaimAtAGlance(
                theClaim.reference(),
                theClaim.type().label(),
                ClaimDocuments.statusOf(theClaim, attached),
                theClaim.requiredDocuments(),
                ClaimDocuments.unmatchedRequiredDocuments(theClaim, attached),
                summaries.summaryOf(theClaim, attached),
                attached.stream()
                        .map(document -> DocumentForChat.of(document, counting.contains(document.id())))
                        .toList(),
                proposalsOn(theClaim.id()));
    }

    private Claim caseOrFail(String claimId) {
        return claims.findById(claimId).orElseThrow(() -> new ClaimDesk.UnknownClaimException(claimId));
    }

    private List<String> idsOfProposalsOn(String claimId) {
        return proposals.findByCaseId(claimId).stream().map(Proposal::id).toList();
    }

    private Proposal answerable(String proposalId) {
        return proposals.findById(proposalId).orElseThrow(() -> new ClaimDesk.UnknownProposalException(proposalId));
    }

    private ProposalCard raise(Proposal proposal) {
        proposals.save(proposal);
        return ProposalCard.of(proposal);
    }

    private UploadedDocument documentIn(String claimId, String filename) {
        return documents.findByCaseId(claimId).stream()
                .filter(document -> document.filename().equals(filename))
                .findFirst()
                .orElseThrow(() -> new ClaimDesk.UnknownDocumentException(filename));
    }
}
