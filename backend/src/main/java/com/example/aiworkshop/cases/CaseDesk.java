package com.example.aiworkshop.cases;

import com.example.aiworkshop.tasks.task_6_summary.CaseSummarizer;
import com.example.aiworkshop.tasks.task_6_summary.CaseStatusWriter;
import com.example.aiworkshop.tasks.task_5_chat.CaseChatAgent;
import com.example.aiworkshop.tasks.task_4_postprocessing.model.FraudScreening;
import com.example.aiworkshop.tasks.task_4_postprocessing.FraudScreener;
import com.example.aiworkshop.documents.store.DocumentStore;
import com.example.aiworkshop.documents.store.DocumentFiles;
import com.example.aiworkshop.documents.model.UploadedDocument;
import com.example.aiworkshop.tasks.task_5_chat.DocumentReader;
import com.example.aiworkshop.tasks.task_6_summary.CaseSummaryStore;
import com.example.aiworkshop.cases.store.CaseStore;
import com.example.aiworkshop.cases.proposals.ReviewProposal;
import com.example.aiworkshop.cases.proposals.ProposalStore;
import com.example.aiworkshop.cases.proposals.ProposalState;
import com.example.aiworkshop.cases.proposals.ProposalCard;
import com.example.aiworkshop.cases.proposals.Proposal;
import com.example.aiworkshop.cases.proposals.DocumentRequestStore;
import com.example.aiworkshop.cases.proposals.DocumentRequestProposal;
import com.example.aiworkshop.cases.proposals.DocumentRequest;
import com.example.aiworkshop.tasks.task_6_summary.DocumentForSummary;
import com.example.aiworkshop.cases.model.CaseOverview;
import com.example.aiworkshop.cases.model.CaseDetail;
import com.example.aiworkshop.cases.model.Case;
import com.example.aiworkshop.cases.chat.ToolCall;
import com.example.aiworkshop.tasks.task_5_chat.model.DocumentInDetail;
import com.example.aiworkshop.tasks.task_5_chat.model.DocumentForChat;
import com.example.aiworkshop.cases.chat.ChatTurn;
import com.example.aiworkshop.cases.chat.ChatAnswer;
import com.example.aiworkshop.cases.chat.CaseChatStore;
import com.example.aiworkshop.tasks.task_5_chat.model.CaseAtAGlance;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.service.Result;
import java.util.List;
import java.util.UUID;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class CaseDesk {
    private final CaseStore cases;
    private final DocumentStore documents;
    private final CaseSummaryStore summaries;
    private final ProposalStore proposals;
    private final DocumentRequestStore requests;
    private final CaseChatStore chats;
    private final DocumentFiles files;
    private final CaseSummarizer summarizer;
    private final CaseStatusWriter statusWriter;
    private final FraudScreener screener;
    private final CaseChatAgent chatAgent;
    private final DocumentReader reader;

    CaseDesk(
            CaseStore cases,
            DocumentStore documents,
            CaseSummaryStore summaries,
            ProposalStore proposals,
            DocumentRequestStore requests,
            CaseChatStore chats,
            DocumentFiles files,
            CaseSummarizer summarizer,
            CaseStatusWriter statusWriter,
            FraudScreener screener,
            @Lazy CaseChatAgent chatAgent,
            DocumentReader reader) {
        this.cases = cases;
        this.documents = documents;
        this.summaries = summaries;
        this.proposals = proposals;
        this.requests = requests;
        this.chats = chats;
        this.files = files;
        this.summarizer = summarizer;
        this.statusWriter = statusWriter;
        this.screener = screener;
        this.chatAgent = chatAgent;
        this.reader = reader;
    }

    public List<CaseOverview> list() {
        return cases.findAll().stream().map(this::overviewOf).toList();
    }

    public CaseDetail open(String caseId) {
        Case theCase = cases.findById(caseId).orElseThrow(() -> new UnknownCaseException(caseId));
        List<UploadedDocument> attached = documents.findByCaseId(caseId);
        CaseOverview overview = overviewOf(theCase);

        String caseType = theCase.type().label();
        String statusNote = statusWriter.write(
                caseType,
                overview.status(),
                overview.outstanding(),
                whyEachBlockedDocumentIsBlocked(theCase, attached));
        return new CaseDetail(
                overview,
                attached,
                idsOf(theCase.countingDocuments(attached)),
                idsOf(theCase.blockedDocuments(attached)),
                summaryOf(theCase, attached),
                statusNote,
                screeningsFound(attached),
                proposalsOn(caseId),
                chats.findByCaseId(caseId));
    }

    private List<FraudScreening> screeningsFound(List<UploadedDocument> attached) {
        return screener.findAllFor(idsOf(attached));
    }

    public ChatAnswer chat(String caseId, String question) {
        Case theCase = cases.findById(caseId).orElseThrow(() -> new UnknownCaseException(caseId));
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

    private CaseAtAGlance glanceAt(Case theCase) {
        List<UploadedDocument> attached = documents.findByCaseId(theCase.id());
        List<String> counting = idsOf(theCase.countingDocuments(attached));
        return new CaseAtAGlance(
                theCase.reference(),
                theCase.type().label(),
                theCase.status(attached),
                theCase.requiredDocuments(),
                theCase.unmatchedRequiredDocuments(attached),
                summaryOf(theCase, attached),
                attached.stream()
                        .map(document -> DocumentForChat.of(document, counting.contains(document.id())))
                        .toList(),
                proposalsOn(theCase.id()));
    }

    private List<String> idsOfProposalsOn(String caseId) {
        return proposals.findByCaseId(caseId).stream().map(Proposal::id).toList();
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
        cases.findById(caseId).orElseThrow(() -> new UnknownCaseException(caseId));
        return raise(new DocumentRequestProposal(
                UUID.randomUUID().toString(), caseId, label, reason, ProposalState.PROPOSED));
    }

    public ProposalCard confirm(String proposalId) {
        Proposal proposal = answerable(proposalId);
        if (!proposal.isOutstanding()) {
            return ProposalCard.of(proposal);
        }
        switch (proposal) {
            case ReviewProposal reviewProposal -> review(reviewProposal.documentId());
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

    private Proposal answerable(String proposalId) {
        return proposals.findById(proposalId).orElseThrow(() -> new UnknownProposalException(proposalId));
    }

    private ProposalCard raise(Proposal proposal) {
        proposals.save(proposal);
        return ProposalCard.of(proposal);
    }

    private List<ProposalCard> proposalsOn(String caseId) {
        return proposals.findByCaseId(caseId).stream().map(ProposalCard::of).toList();
    }

    private UploadedDocument documentIn(String caseId, String filename) {
        return documents.findByCaseId(caseId).stream()
                .filter(document -> document.filename().equals(filename))
                .findFirst()
                .orElseThrow(() -> new UnknownDocumentException(filename));
    }

    private String summaryOf(Case theCase, List<UploadedDocument> attached) {
        String caseId = theCase.id();
        List<String> writtenOver = idsOf(attached);
        return summaries.find(caseId, writtenOver).orElseGet(() -> {
            String summary = summarizer.summarise(
                    theCase.type().label(),
                    attached.stream().map(DocumentForSummary::of).toList());
            summaries.save(caseId, writtenOver, summary);
            return summary;
        });
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
        UnknownCaseException(String caseId) {
            super("No such case: " + caseId);
        }
    }

    public static class UnknownProposalException extends RuntimeException {
        UnknownProposalException(String proposalId) {
            super("No such proposal: " + proposalId);
        }
    }

    public static class UnknownDocumentException extends RuntimeException {
        UnknownDocumentException(String filename) {
            super("No document called '" + filename + "' is attached to this case.");
        }
    }
}
