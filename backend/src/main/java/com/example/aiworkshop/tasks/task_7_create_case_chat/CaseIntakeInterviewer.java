package com.example.aiworkshop.tasks.task_7_create_case_chat;

import com.example.aiworkshop.cases.model.Case;
import com.example.aiworkshop.tasks.task_7_create_case_chat.model.CaseScenario;
import com.example.aiworkshop.tasks.task_7_create_case_chat.model.InterviewTurn;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * The conversational intake agent: like the one-shot classifier of task 1, but allowed to ask before
 * it commits. It reads the conversation so far and returns a single {@link InterviewTurn} — either
 * the questions it still needs answered, or the scenario it has settled on.
 *
 * <p>This interface <em>is</em> the agent. LangChain4j builds the implementation from the system
 * message and the shape of {@link InterviewTurn}. The scenarios are not written into the prompt by
 * hand: {@code CaseScenario.catalog()} is rendered in through {@code {{scenarios}}}, so the taxonomy
 * the agent reasons over is the same enum the Case is opened from.
 *
 * <p>Stateless by design: the whole conversation is handed in as {@code {{transcript}}} each turn, so
 * the agent holds no memory between calls and the caller owns the history.
 */
/*
 * ── To set this task again ─────────────────────────────────────────────────────────────
 * Put this back as the @SystemMessage below, and the application returns to explaining
 * which file to open rather than asking the claimant anything.
 *
 * TODO — task 7.
 *
 * Write the system message for a conversational intake agent. Unlike the one-shot classifier
 * of task 1, this one may ask the claimant a few follow-up questions before it commits, when
 * the answer would change which scenario applies.
 *
 * The scenarios it may choose from are rendered in through {{scenarios}}. Each turn it makes
 * one move: ask one to three plain questions (NEEDS_INFO), or settle on exactly one
 * CaseScenario (DECIDED) — which is the shape of InterviewTurn, the record this returns.
 *
 * One version of the answer is commented out just below, and the whole of it is on
 * the solutions branch.
 */
public interface CaseIntakeInterviewer {

    @SystemMessage(
            """
            You are the intake agent in a case-handling system. Someone has written, in their own
            words, what they need help with. Your job is to open the right case for them — but unlike
            a plain classifier, you may ask a few follow-up questions first when the answer would
            change which scenario applies.

            The scenarios you can open, grouped by the kind of insurance:

            {{scenarios}}

            Read the whole conversation so far and make one move:

            - NEEDS_INFO — you cannot yet tell which scenario fits, and a short answer would settle
              it. Give one to three plain questions, each asking exactly one thing. Ask only what
              changes which scenario applies: if two scenarios need different documents and you cannot
              yet tell them apart, that is what to ask about. Leave the scenario empty.

            - DECIDED — one scenario clearly fits. Return it, with the confidence you have: HIGH when
              the situation is plain, LOW when you are largely guessing.

            Be economical. Never ask about something the person has already told you, and never ask
            more than three questions in total across the whole conversation — if the transcript
            already shows you asked before, lean towards deciding now. If nothing fits even after
            asking, decide on OTHER rather than forcing a poor match.

            Address the claimant directly and plainly in the questions. Write the rationale as one
            factual sentence, in English, whatever language the conversation is written in.
            """)
    @UserMessage("The conversation so far:\n\n{{transcript}}")
    InterviewTurn next(@V("scenarios") String scenarios, @V("transcript") String transcript);
}
