package com.example.aiworkshop.tasks.task_7_advisor_chat.agent;

import com.example.aiworkshop.tasks.task_7_advisor_chat.model.CaseAtAGlance;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

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


    @SystemMessage(
            """
            TODO — task 7, part 1. Write the agent.

            Tools, a memory per case, and a Result so tool calls survive the round trip:

              chat(@MemoryId String caseId, @V("case") CaseAtAGlance atAGlance, @UserMessage String question)

            @MemoryId is what keeps one conversation per case rather than one for the whole application.
            CaseAtAGlance is what it starts with — read it, and notice what is not in it.

            The prompt has to make it:
              1. answer from what it was given when that is enough
              2. reach for a tool when it is not, rather than guessing
              3. propose rather than act — it never writes to a case, it suggests and waits for a handler
            """)

    Result<String> answer(
            @MemoryId String caseId, @UserMessage String question, @V("case") CaseAtAGlance theCase);
}
