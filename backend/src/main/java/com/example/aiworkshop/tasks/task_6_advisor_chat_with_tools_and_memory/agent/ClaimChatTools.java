package com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools_and_memory.agent;

import com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools_and_memory.ChatDesk;
import com.example.aiworkshop.tasks.task_6_advisor_chat_with_tools_and_memory.proposals.ProposalCard;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import org.springframework.stereotype.Component;

/**
 * What the Claim Chat agent can reach for. Four methods, and not one line of logic.
 *
 * <p>That is a hard constraint rather than a style: anything written here is reachable only by
 * driving a model, and therefore cannot be tested. Every method hands straight to {@link ClaimDesk},
 * where the same behaviour is exercised by {@code ClaimChatTest} without a model in sight. If a
 * change to this file is tempting, the change belongs on the other side of one of these calls.
 *
 * <p>No method takes a Claim. The Claim arrives as {@link ToolMemoryId} — the identifier the
 * conversation is keyed by — so the Claim a tool acts on is the Claim the handler has open, and the
 * model has no way to name a different one.
 *
 * <p>{@code returnBehavior} is left at its default on the two proposing tools. A Proposal must not
 * halt the tool-calling loop: the agent has to be able to propose two things, or say a word about
 * the one it just proposed.
 */
@Component
public class ClaimChatTools {

    private final ChatDesk desk;

    ClaimChatTools(ChatDesk desk) {
        this.desk = desk;
    }

    @Tool(
            """
            Look one document up properly: its own summary, every fact the intake agent extracted \
            from it, why its quality was judged the way it was, and whether a claim handler has \
            already reviewed it. Use this whenever a question turns on what a document actually \
            says. It does not open the file.""")
    String documentDetail(
            @ToolMemoryId String claimId,
            @P("The document's filename, exactly as it appears in the claim index.") String filename) {
        return desk.documentDetail(claimId, filename);
    }

    @Tool(
            """
            Open the actual file — the PDF or the photograph — and answer one specific question \
            from what is on the page. This is the expensive tool: it sends the whole document to a \
            second model and takes several seconds, so do not reach for it by default. Use \
            documentDetail first. Only come here when documentDetail has already been consulted and \
            does not carry the answer: a figure, date, name, clause or serial number nobody \
            extracted, something in the small print, or a question about what the image itself \
            shows. Ask one narrow question per call, not "tell me about this document".""")
    String readDocument(
            @ToolMemoryId String claimId,
            @P("The document's filename, exactly as it appears in the claim index.") String filename,
            @P("The single question to answer from the file. Be specific about what to look for.")
                    String question) {
        return desk.readDocument(claimId, filename, question);
    }



    @Tool(
            """
            Suggest that a claim handler review a document — that it is workable despite a poor \
            quality assessment. This performs nothing. It puts a card in front of the handler, and \
            only their click records the review.""")
    ProposalCard proposeReview(
            @ToolMemoryId String claimId,
            @P("The document's filename, exactly as it appears in the claim index.") String filename,
            @P("Why the document is workable anyway, in one sentence a claim handler can judge.")
                    String reason) {
        return desk.proposeReview(claimId, filename, reason);
    }

    @Tool(
            """
            Suggest asking the claimant for something the claim is missing. This sends nothing and \
            contacts nobody. It puts a card in front of the claim handler with your wording on it, \
            and the request only leaves the building if they click it. So do not tell the handler \
            the claimant has been asked, or that anything is on its way — say you have put a \
            suggested request in front of them. Use it when a document that is needed is absent or \
            unusable and the only way forward is for the claimant to send something new.""")
    ProposalCard proposeDocumentRequest(
            @ToolMemoryId String claimId,
            @P("What to ask the claimant for, in plain language they will understand.") String label,
            @P("Why it is needed, in one sentence, also written for the claimant.") String reason) {
        return desk.proposeDocumentRequest(claimId, label, reason);
    }
}
