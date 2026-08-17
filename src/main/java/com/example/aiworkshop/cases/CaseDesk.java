package com.example.aiworkshop.cases;

import com.example.aiworkshop.document.DocumentFiles;
import com.example.aiworkshop.document.DocumentReader;
import com.example.aiworkshop.document.DocumentStore;
import com.example.aiworkshop.document.UploadedDocument;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.service.Result;
import java.util.List;
import java.util.UUID;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * What the Case Handler's screen talks to.
 *
 * <p>Deliberately thin, and wide instead. The rules about what each status means live in
 * {@link Case#status} and the rules about what confirming a suggestion means live on {@link Proposal};
 * this class fetches, delegates and hands back. It holds every handler-side agent as a collaborator,
 * and is the only place any of them is called — including from {@link CaseChatTools}, which is why
 * that class is allowed to contain nothing.
 *
 * <p>Note which methods call a model and which do not. {@link #list} is pure lookup, so browsing the
 * case list is never gated on a model call per Case; {@link #open} makes both calls, once, for the
 * one Case the handler actually asked for; {@link #chat} makes one more, plus whatever the agent's
 * tools cost it. {@link #confirm} and {@link #decline} call none.
 *
 * <p>The Case Chat agent arrives {@link Lazy} to break a loop that is real rather than accidental:
 * the agent's tools call back into this class, so building it eagerly here would need it to exist
 * first. Nothing else about the wiring depends on it.
 */
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
        this.chatAgent = chatAgent;
        this.reader = reader;
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
                proposalsOn(caseId),
                chats.findByCaseId(caseId));
    }

    /**
     * One turn of the Case Chat. One model call, plus whatever the agent's tools cost it.
     *
     * <p>The Case Summary comes out of the same cache {@link #open} fills, so a handler who has
     * already opened the Case pays nothing extra for the chat to be grounded in it.
     *
     * <p>Which Proposals this turn raised is worked out by difference rather than reported by the
     * tools. A tool that had to report back would be a tool holding state, and the tools hold
     * nothing.
     */
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

    /** Everything the agent starts a turn knowing. Anything not here, it has to fetch. */
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

    /**
     * One Document, looked at properly: its own summary, everything the intake agent extracted, and
     * why the Quality Assessment landed where it did.
     *
     * <p>This is the other side of the index the agent starts with. Everything here is deliberately
     * absent from that index, so a Case Chat prompt does not carry every Extraction in the Case.
     *
     * <p>Rendered here rather than in the tool, and rendered rather than returned as a record: a
     * {@code @Tool} method returning anything but a {@code String} has its result turned into JSON,
     * and what an agent is handed is a decision worth making somewhere it can be read.
     */
    public String documentDetail(String caseId, String filename) {
        return DocumentInDetail.of(documentIn(caseId, filename)).toString();
    }

    /**
     * A second agent sent back to the original file with one question. The second model call a turn
     * can cost, and the only one that puts a Claimant's file in front of a model again.
     *
     * <p>The reader is handed the file and the question and nothing about the Case, on purpose. What
     * comes back is relayed to the Case Chat agent as the tool's result, whatever it says — including
     * that the file does not show what was asked.
     */
    public String readDocument(String caseId, String filename, String question) {
        UploadedDocument document = documentIn(caseId, filename);
        return reader.read(
                List.of(TextContent.from("Look at the attached file."), files.contentOf(document)), question);
    }

    /**
     * The agent suggesting a Review. Records a Proposal and nothing else — the Case is exactly where
     * it was until a Case Handler confirms it.
     *
     * <p>The Document is named by filename because that is what the agent was given; identifiers are
     * unspeakable. It is resolved to one here, at the moment of proposing, so that confirming later
     * cannot pick a different Document than the one the agent meant.
     */
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

    /**
     * The agent suggesting the Claimant be asked for something. Records a Proposal and nothing else.
     *
     * <p>The label is free text rather than one of the Required Documents on purpose: the most
     * useful thing to ask for is often the part of a document that did not arrive, which no
     * checklist entry names.
     */
    public ProposalCard proposeDocumentRequest(String caseId, String label, String reason) {
        cases.findById(caseId).orElseThrow(() -> new UnknownCaseException(caseId));
        return raise(new DocumentRequestProposal(
                UUID.randomUUID().toString(), caseId, label, reason, ProposalState.PROPOSED));
    }

    /**
     * A Case Handler saying yes. The one place a Proposal turns into a write.
     *
     * <p>The switch is over a sealed type, so adding a third form of Proposal stops compiling here
     * until someone decides what confirming it means. That is the point of the sealing.
     */
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

    /**
     * A Case Handler saying no. Nothing is performed, and the Proposal is kept rather than deleted —
     * a declined suggestion is fed back to the agent so it does not make the same one again.
     */
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

    /**
     * The one Document in this Case going by that filename, most recent upload first.
     *
     * <p>Nothing stops a Claimant uploading two files with the same name, so this is the same rule
     * {@link Case} uses to pick the Document that counts: the newest wins. Deliberately scoped to one
     * Case — the agent is bound to a Case by its memory identifier, and this is where that binding
     * stops it reaching another one.
     */
    private UploadedDocument documentIn(String caseId, String filename) {
        return documents.findByCaseId(caseId).stream()
                .filter(document -> document.filename().equals(filename))
                .findFirst()
                .orElseThrow(() -> new UnknownDocumentException(filename));
    }

    /**
     * The Case Summary, written once per set of Documents. The Documents themselves are the only
     * thing it depends on, so re-running the agent for a handler who opened the same Case twice
     * would be paying for the same paragraphs again.
     */
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
                theCase.type().label(),
                theCase.status(attached),
                theCase.requiredDocuments(),
                theCase.unmatchedRequiredDocuments(attached),
                requests.findByCaseId(theCase.id()));
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

    /** Thrown when a Confirm or a Decline names a Proposal that does not exist. Mapped to 404. */
    public static class UnknownProposalException extends RuntimeException {
        UnknownProposalException(String proposalId) {
            super("No such proposal: " + proposalId);
        }
    }

    /**
     * Thrown when a filename names no Document in this Case.
     *
     * <p>Reached by a model naming a file that is not there, so the message is written for one: it
     * goes back as the tool's result and is the agent's only chance to correct itself.
     */
    public static class UnknownDocumentException extends RuntimeException {
        UnknownDocumentException(String filename) {
            super("No document called '" + filename + "' is attached to this case.");
        }
    }
}
