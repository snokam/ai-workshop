package com.example.aiworkshop.cases;

import com.example.aiworkshop.document.UploadedDocument;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import java.util.List;

/**
 * Writes the Case Summary: what is in a Case's Documents, taken across all of them.
 *
 * <p>Split from {@link CaseStatusWriter} by input rather than tidiness. This one needs the Documents
 * themselves and only changes when a Document is added; the status prose needs a handful of enum
 * values and is rewritten every time a Case is opened. One agent doing both would drag this payload
 * through every page view.
 */
public interface CaseSummarizer {

    @SystemMessage(
            """
            You are writing for a case handler who is about to decide a case, and who would otherwise
            open every document in it one at a time.

            Say what has arrived and what it says, across all the documents together. Draw the
            connections between them — the same date, the same amount, the same name, or a
            disagreement between two of them. A disagreement is the single most useful thing you can
            point out; say so plainly when you find one.

            This is not a list of the documents. The case handler can already see the list, and each
            document already has its own summary. Do not repeat either.

            A few short paragraphs at most. Do not recommend a decision, and do not say what should
            happen next — that is not your job here. Write in the language the documents are written
            in.
            """)
    @UserMessage("The documents attached to this case:\n\n{{documents}}")
    String summarise(@V("documents") List<UploadedDocument> documents);
}
