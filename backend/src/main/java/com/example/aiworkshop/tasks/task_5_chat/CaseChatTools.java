package com.example.aiworkshop.tasks.task_5_chat;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import org.springframework.stereotype.Component;
import com.example.aiworkshop.cases.CaseDesk;
import com.example.aiworkshop.cases.ProposalCard;

/**
 * What the Case Chat agent can reach for. Four methods, and not one line of logic.
 *
 * <p>That is a hard constraint rather than a style: anything written here is reachable only by
 * driving a model, and therefore cannot be tested. Every method hands straight to {@link CaseDesk},
 * where the same behaviour is exercised by {@code CaseChatTest} without a model in sight. If a
 * change to this file is tempting, the change belongs on the other side of one of these calls.
 *
 * <p>No method takes a Case. The Case arrives as {@link ToolMemoryId} — the identifier the
 * conversation is keyed by — so the Case a tool acts on is the Case the handler has open, and the
 * model has no way to name a different one.
 *
 * <p>{@code returnBehavior} is left at its default on the two proposing tools. A Proposal must not
 * halt the tool-calling loop: the agent has to be able to propose two things, or say a word about
 * the one it just proposed.
 */
@Component
public class CaseChatTools {

    private final CaseDesk desk;

    CaseChatTools(CaseDesk desk) {
        this.desk = desk;
    }

    @Tool(
            """
            Look one document up properly: its own summary, every fact the intake agent extracted \
            from it, why its quality was judged the way it was, and whether a case handler has \
            already reviewed it. Use this whenever a question turns on what a document actually \
            says. It does not open the file.""")
    String documentDetail(
            @ToolMemoryId String caseId,
            @P("The document's filename, exactly as it appears in the case index.") String filename) {
        return desk.documentDetail(caseId, filename);
    }

    @Tool(
            """
            Have a second agent look at the original file and answer one question about it. Use this \
            when the extracted facts do not contain what was asked, when the file was judged poor, \
            or when the case handler asks you to look again. Slower than the other tools, because a \
            model reads the file.""")
    String readDocument(
            @ToolMemoryId String caseId,
            @P("The document's filename, exactly as it appears in the case index.") String filename,
            @P("The single question to answer from the file. Be specific about what to look for.")
                    String question) {
        return desk.readDocument(caseId, filename, question);
    }

    @Tool(
            """
            Suggest that a case handler review a document — that it is workable despite a poor \
            quality assessment. This performs nothing. It puts a card in front of the handler, and \
            only their click records the review.""")
    ProposalCard proposeReview(
            @ToolMemoryId String caseId,
            @P("The document's filename, exactly as it appears in the case index.") String filename,
            @P("Why the document is workable anyway, in one sentence a case handler can judge.")
                    String reason) {
        return desk.proposeReview(caseId, filename, reason);
    }

    @Tool(
            """
            Suggest asking the claimant for a document. This performs nothing and reaches nobody. \
            It puts a card in front of the case handler, and only their click sends it.""")
    ProposalCard proposeDocumentRequest(
            @ToolMemoryId String caseId,
            @P("What to ask the claimant for, in plain language they will understand.") String label,
            @P("Why it is needed, in one sentence, also written for the claimant.") String reason) {
        return desk.proposeDocumentRequest(caseId, label, reason);
    }
}
