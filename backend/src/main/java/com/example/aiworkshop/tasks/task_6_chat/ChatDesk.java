package com.example.aiworkshop.tasks.task_6_chat;

import com.example.aiworkshop.tasks.task_6_chat.agent.CaseChatAgent;
import com.example.aiworkshop.tasks.task_6_chat.agent.DocumentReader;
import com.example.aiworkshop.tasks.task_1_first_agent.CaseDesk;
import com.example.aiworkshop.tasks.task_6_chat.store.CaseChatStore;
import com.example.aiworkshop.tasks.task_6_chat.model.ChatAnswer;
import com.example.aiworkshop.tasks.task_6_chat.model.ChatTurn;
import com.example.aiworkshop.tasks.task_6_chat.model.ToolCall;
import com.example.aiworkshop.tasks.task_1_first_agent.model.Case;
import com.example.aiworkshop.tasks.task_6_chat.proposals.DocumentRequest;
import com.example.aiworkshop.tasks.task_6_chat.proposals.DocumentRequestProposal;
import com.example.aiworkshop.tasks.task_6_chat.store.DocumentRequestStore;
import com.example.aiworkshop.tasks.task_6_chat.proposals.Proposal;
import com.example.aiworkshop.tasks.task_6_chat.proposals.ProposalCard;
import com.example.aiworkshop.tasks.task_6_chat.proposals.ProposalState;
import com.example.aiworkshop.tasks.task_6_chat.store.ProposalStore;
import com.example.aiworkshop.tasks.task_6_chat.proposals.ReviewProposal;
import com.example.aiworkshop.tasks.task_1_first_agent.store.CaseStore;
import com.example.aiworkshop.tasks.task_2_document_agent.model.UploadedDocument;
import com.example.aiworkshop.tasks.task_2_document_agent.store.DocumentFiles;
import com.example.aiworkshop.tasks.task_2_document_agent.CaseDocuments;
import com.example.aiworkshop.tasks.task_2_document_agent.DocumentReview;
import com.example.aiworkshop.tasks.task_2_document_agent.store.DocumentStore;
import com.example.aiworkshop.tasks.task_5_summary.SummaryDesk;
import com.example.aiworkshop.tasks.task_6_chat.model.CaseAtAGlance;
import com.example.aiworkshop.tasks.task_6_chat.model.DocumentForChat;
import com.example.aiworkshop.tasks.task_6_chat.model.DocumentInDetail;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.service.Result;
import java.util.List;
import java.util.UUID;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * Everything task 6 offers: one conversation about one case, and the proposals it puts up.
 *
 * <p>The tools talk to this and nothing else, which is why none of them contains any logic — an
 * agent that could reach past this door would end up with a private version of the case.
 *
 * <p>It reads task 5's summary rather than the documents. That is what the opening context is: the
 * agent starts knowing what the documents say together, and uses its tools for the detail.
 */
@Service
public class ChatDesk {

    private final CaseStore cases;
    private final DocumentStore documents;
    private final DocumentFiles files;
    private final ProposalStore proposals;
    private final DocumentRequestStore requests;
    private final CaseChatStore chats;
    private final CaseChatAgent chatAgent;
    private final DocumentReader reader;
    private final SummaryDesk summaries;
    private final CaseDesk desk;
    private final DocumentReview review;

    public ChatDesk(
            CaseStore cases,
            DocumentStore documents,
            DocumentFiles files,
            ProposalStore proposals,
            DocumentRequestStore requests,
            CaseChatStore chats,
            // The tools call this desk and this desk calls the agent that owns them, so one of the
            // two has to be resolved late. Spring cannot break the circle on its own.
            @Lazy CaseChatAgent chatAgent,
            DocumentReader reader,
            SummaryDesk summaries,
            CaseDesk desk,
            DocumentReview review) {
        this.cases = cases;
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

    public ChatAnswer chat(String caseId, String question) {
        Case theCase = caseOrFail(caseId);
        List<String> before = idsOfProposalsOn(caseId);

        Result<String> answered = chatAgent.answer(caseId, question, glanceAt(theCase));

        List<String> raised = idsOfProposalsOn(caseId).stream()
                .filter(id -> !before.contains(id))
                .toList();
        ChatTurn turn = new ChatTurn(
                question,
                answered.content(),
                answered.toolExecutions().stream().map(ToolCall::of).toList(),
                raised);
        chats.append(caseId, turn);
        return new ChatAnswer(turn, proposalsOn(caseId));
    }

    public List<ChatTurn> turnsOn(String caseId) {
        return chats.findByCaseId(caseId);
    }

    public List<ProposalCard> proposalsOn(String caseId) {
        return proposals.findByCaseId(caseId).stream().map(ProposalCard::of).toList();
    }

    public String documentDetail(String caseId, String filename) {
        return DocumentInDetail.of(documentIn(caseId, filename)).toString();
    }

    public String readDocument(String caseId, String filename, String question) {
        UploadedDocument document = documentIn(caseId, filename);
        return reader.read(
                List.of(TextContent.from("Look at the attached file."), files.contentOf(document)), question);
    }

    public ProposalCard proposeReview(String caseId, String filename, String reason) {
        UploadedDocument document = documentIn(caseId, filename);
        return raise(new ReviewProposal(
                UUID.randomUUID().toString(),
                caseId,
                document.id(),
                document.filename(),
                reason,
                ProposalState.PROPOSED));
    }

    public ProposalCard proposeDocumentRequest(String caseId, String label, String reason) {
        caseOrFail(caseId);
        return raise(new DocumentRequestProposal(
                UUID.randomUUID().toString(), caseId, label, reason, ProposalState.PROPOSED));
    }

    public List<DocumentRequest> requestsOn(String caseId) {
        return requests.findByCaseId(caseId);
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
                        requestProposal.caseId(),
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

    private CaseAtAGlance glanceAt(Case theCase) {
        List<UploadedDocument> attached = documents.findByCaseId(theCase.id());
        List<String> counting =
                CaseDocuments.countingDocuments(theCase, attached).stream().map(UploadedDocument::id).toList();
        return new CaseAtAGlance(
                theCase.reference(),
                theCase.type().label(),
                CaseDocuments.statusOf(theCase, attached),
                theCase.requiredDocuments(),
                CaseDocuments.unmatchedRequiredDocuments(theCase, attached),
                summaries.summaryOf(theCase, attached),
                attached.stream()
                        .map(document -> DocumentForChat.of(document, counting.contains(document.id())))
                        .toList(),
                proposalsOn(theCase.id()));
    }

    private Case caseOrFail(String caseId) {
        return cases.findById(caseId).orElseThrow(() -> new CaseDesk.UnknownCaseException(caseId));
    }

    private List<String> idsOfProposalsOn(String caseId) {
        return proposals.findByCaseId(caseId).stream().map(Proposal::id).toList();
    }

    private Proposal answerable(String proposalId) {
        return proposals.findById(proposalId).orElseThrow(() -> new CaseDesk.UnknownProposalException(proposalId));
    }

    private ProposalCard raise(Proposal proposal) {
        proposals.save(proposal);
        return ProposalCard.of(proposal);
    }

    private UploadedDocument documentIn(String caseId, String filename) {
        return documents.findByCaseId(caseId).stream()
                .filter(document -> document.filename().equals(filename))
                .findFirst()
                .orElseThrow(() -> new CaseDesk.UnknownDocumentException(filename));
    }
}
