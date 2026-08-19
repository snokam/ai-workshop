package com.example.aiworkshop.tasks.task_5_summary;

import com.example.aiworkshop.cases.model.CaseStatus;
import com.example.aiworkshop.cases.model.Case;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import java.util.List;

/**
 * Turns the derived Case Status into the sentence a Case Handler wants to read, and says what to do
 * next.
 *
 * <p>The honest boundary between what belongs in code and what belongs in a model, drawn on
 * purpose: the status itself is computed in {@link Case#status} and is testable and reproducible;
 * this agent only writes it up. It is handed derived facts and never a Document, which is what keeps
 * the call cheap enough to make on every page view.
 *
 * <p>The next move is prose rather than a structured action. An action enum invites the model to
 * propose things the screen cannot perform, and two prose fields tend to produce the same sentence
 * twice.
 */
public interface CaseStatusWriter {

    @SystemMessage(
            """
            You write the one-line situation report at the top of a case handler's screen.

            You are given facts that have already been worked out, not documents: the kind of case it
            is, its status, and what it is waiting on. Do not speculate about what is in the case
            beyond what you are told, and do not invent a status — the status you are given is the
            status.

            Two short sentences: where the case stands, and the next move. Name the kind of case so
            the handler knows at a glance what they are looking at. Address the case handler directly.
            The next move must be something a person can actually do — chase a claimant for a named
            document, review a named file, decide the case. If the case is waiting on the claimant,
            say so, so the handler knows it is not theirs to act on.

            No preamble, no restating the case reference back. Write in English.
            """)
    @UserMessage(
            """
            Case type: {{caseType}}
            Status: {{status}}
            Still waiting for: {{outstanding}}
            Too poor to work with, and not yet reviewed: {{blocked}}
            """)
    String write(
            @V("caseType") String caseType,
            @V("status") CaseStatus status,
            @V("outstanding") List<String> outstandingRequiredDocuments,
            @V("blocked") List<String> blockedDocuments);
}
