package com.example.aiworkshop.tasks.task_5_chat;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import com.example.aiworkshop.cases.CaseAtAGlance;

/**
 * The Case Chat agent: a conversation about one Case, with tools, that can suggest but never write.
 *
 * <p>Three things about the signature are load-bearing.
 *
 * <p>{@link MemoryId} is the Case identifier. It keys the conversation, and it is also what every
 * tool receives as its {@code @ToolMemoryId} — so the Case a tool acts on is the Case the handler
 * has open, never one the model named. There is no tool argument for a Case anywhere.
 *
 * <p>The return type is {@link Result} rather than {@code String} because {@code Result} is what
 * carries {@link Result#toolExecutions()}. The tool calls are shown to the Case Handler under the
 * answer, so a looked-up fact can be told from a guess; a bare {@code String} would throw them away.
 *
 * <p>{@link CaseAtAGlance} arrives as a template variable rendered into the system message rather
 * than as part of the question, so the Case is context and the handler's words are the turn.
 *
 * <p>Per ADR 0002 the English rule is stated here rather than inherited from anywhere:
 * {@code AiServices.create} builds an agent from its interface alone.
 */
public interface CaseChatAgent {
    /**
     * ── TASK CHAT ────────────────────────────────────────────────────────────────────────
     * Set to true once you have written the case chat agent below. While it is false the
     * application still runs: every screen that does not need this agent works as normal,
     * and the one that does explains which file to open.
     * ──────────────────────────────────────────────────────────────────────────────────
     */
    boolean IMPLEMENTED = false;


    @SystemMessage(
            """
            TODO — task 5.

            Write the system message for the agent a case handler talks to. Unlike the others this
            one has tools and a memory, so it can look things up mid-answer and remember what was
            already asked.

            It answers about one case, it may propose asking the claimant for something, and it
            never writes anything itself.

            The solutions branch has the version this was written from.
            """)
    Result<String> answer(
            @MemoryId String caseId, @UserMessage String question, @V("case") CaseAtAGlance theCase);
}
