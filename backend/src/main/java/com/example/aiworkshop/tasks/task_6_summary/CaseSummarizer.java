package com.example.aiworkshop.tasks.task_6_summary;

import com.example.aiworkshop.cases.model.DocumentForSummary;
import com.example.aiworkshop.cases.model.Case;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import java.util.List;

/**
 * Writes the Case Summary: what is in a Case's Documents, taken across all of them.
 *
 * <p>Split from {@link CaseStatusWriter} by input rather than tidiness. This one needs what the
 * Documents say and only changes when a Document is added; the status prose needs a handful of enum
 * values and is rewritten every time a Case is opened. One agent doing both would drag this payload
 * through every page view.
 *
 * <p>It is handed {@link DocumentForSummary} rather than the Documents themselves. What an agent is
 * given is a decision, and passing the domain record made it an accident of that record's shape —
 * see the note there.
 */
/*
 * ── To set this task again ─────────────────────────────────────────────────────────────
 * Put this back as the @SystemMessage below, and the application returns to explaining
 * which file to open rather than answering.
 *
 * TODO — task 6.
 *
 * Write the system message for the agent that reads every document on a case at once and
 * says what they add up to: what is established, what disagrees, and what is still
 * missing.
 *
 * It is shown DocumentForSummary, not the files — read that record to see what it does and
 * does not get.
 *
 * One version of the answer is commented out just below, and the whole of it is on
 * the solutions branch.
 */
public interface CaseSummarizer {


    @SystemMessage(
            """
            You are writing for a case handler who is about to decide a case, and who would otherwise
            open every document in it one at a time.

            You are told what kind of case this is. Read the documents as that kind of case: what
            matters in a travel claim is not what matters in a disability claim, so let the case type
            frame what is worth pointing out and what a document of this kind would be expected to
            show.

            Say what has arrived and what it says, across all the documents together. Draw the
            connections between them — the same date, the same amount, the same name, or a
            disagreement between two of them. A disagreement is the single most useful thing you can
            point out; say so plainly when you find one.

            This is not a list of the documents. The case handler can already see the list, and each
            document already has its own summary. Do not repeat either.

            A few short paragraphs at most. Do not recommend a decision, and do not say what should
            happen next — that is not your job here.

            Write in English, whatever language the documents themselves are in. Field names are
            quoted from the documents and are often not English; do not follow them.
            """)

    String summarise(@V("caseType") String caseType, @V("documents") List<DocumentForSummary> documents);
}
