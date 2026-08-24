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

    // TODO — task 6, part 1. Write the two descriptions marked below.
    //
    // A @Tool description is a prompt, not documentation. It is the only thing the model reads when it
    // decides whether to call this method, so it has to answer one question for a reader who has the
    // claim summary in front of them: when would I need this instead of what I already have?
    //
    // The two above are written for you. Notice what they do beyond saying what the tool returns:
    //
    //   documentDetail   says when to reach for it ("whenever a question turns on what a document
    //                    actually says") and what it will not do ("it does not open the file")
    //   proposeReview    says outright that it performs nothing, because a model that thinks it has
    //                    acted will tell the handler it has
    //
    // For this one: it is the expensive tool. A second agent opens the actual file. Say when that is
    // worth it and when the cheaper one will do, or it will be called for everything.
    @Tool("TODO — say what this does, when to use it instead of documentDetail, and what it costs.")
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

    // The second one to write. It reaches a person, eventually — but not by being called. Say what
    // actually happens when the model calls it, or it will report to the handler that it has already
    // asked the claimant.
    @Tool("TODO — say what this does, and be exact about what it does not do.")
    ProposalCard proposeDocumentRequest(
            @ToolMemoryId String claimId,
            @P("What to ask the claimant for, in plain language they will understand.") String label,
            @P("Why it is needed, in one sentence, also written for the claimant.") String reason) {
        return desk.proposeDocumentRequest(claimId, label, reason);
    }
}
