package com.example.aiworkshop.tasks.task_6_advisor_chat.agent;

import com.example.aiworkshop.tasks.task_6_advisor_chat.model.ClaimAtAGlance;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * The Claim Chat agent: a conversation about one Claim, with tools, that can suggest but never write.
 *
 * <p>Three things about the signature are load-bearing.
 *
 * <p>{@link MemoryId} is the Claim identifier. It keys the conversation, and it is also what every
 * tool receives as its {@code @ToolMemoryId} — so the Claim a tool acts on is the Claim the handler
 * has open, never one the model named. There is no tool argument for a Claim anywhere.
 *
 * <p>The return type is {@link Result} rather than {@code String} because {@code Result} is what
 * carries {@link Result#toolExecutions()}. The tool calls are shown to the Claim Handler under the
 * answer, so a looked-up fact can be told from a guess; a bare {@code String} would throw them away.
 *
 * <p>{@link ClaimAtAGlance} arrives as a template variable rendered into the system message rather
 * than as part of the question, so the Claim is context and the handler's words are the turn.
 *
 * <p>Per ADR 0002 the English rule is stated here rather than inherited from anywhere:
 * {@code AiServices.create} builds an agent from its interface alone.
 */
public interface ClaimChatAgent {


    @SystemMessage(
            """
            TODO — task 7, part 1. Write the agent.

            Tools, a memory per claim, and a Result so tool calls survive the round trip:

              chat(@MemoryId String claimId, @V("claim") ClaimAtAGlance atAGlance, @UserMessage String question)

            @MemoryId is what keeps one conversation per claim rather than one for the whole application.
            ClaimAtAGlance is what it starts with — read it, and notice what is not in it.

            The prompt has to make it:
              1. answer from what it was given when that is enough
              2. reach for a tool when it is not, rather than guessing
              3. propose rather than act — it never writes to a claim, it suggests and waits for a handler
            """)

    Result<String> answer(
            @MemoryId String claimId, @UserMessage String question, @V("claim") ClaimAtAGlance theClaim);
}
