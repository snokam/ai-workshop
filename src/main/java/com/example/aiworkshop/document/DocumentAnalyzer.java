package com.example.aiworkshop.document;

import dev.langchain4j.data.message.Content;
import dev.langchain4j.service.SystemMessage;
import java.util.List;

/**
 * The intake agent: the first thing that looks at a file after someone uploads it.
 *
 * <p>This interface <em>is</em> the agent. There is no implementation to write — LangChain4j builds
 * one at runtime from the system message below and the shape of {@link DocumentAnalysis}. The file
 * itself is passed as {@link Content} (a {@code PdfFileContent} or an {@code ImageContent}), so the
 * model reads the actual PDF or photo. Nothing here extracts text first; the quality assessment
 * would be impossible if it did, because a blurry scan and a crisp one produce the same text.
 *
 * <p>Note the missing {@code @UserMessage}: LangChain4j only forwards {@link Content} arguments when
 * there is no user-message template to render instead. The instructions therefore live in
 * {@link SystemMessage}, and the argument carries the file.
 */
public interface DocumentAnalyzer {

    @SystemMessage(
            """
            You are the intake agent in a case-handling system. Someone has just uploaded a file to
            their case, and you are the first to look at it.

            Do three things in one pass:

            1. CATEGORISE. Say what kind of document this is, as a short noun phrase — for example
               "invoice", "medical report", "proof of identity".

            2. EXTRACT. Pull out the handful of facts a case handler would care about, as name/value
               pairs. Choose the fields that suit this kind of document; there is no fixed schema.
               Name them using the document's own wording. Leave the list empty if the file is too
               poor to read reliably — inventing a value is far worse than returning nothing.

            3. ASSESS THE QUALITY of the file as an artefact, not of its contents. Is it legible? Is
               anything cut off, obscured or missing? Is it the whole document? Does it look like
               what it claims to be?

            The upload is always accepted, so never ask for a new one — your assessment is advice
            attached to the file, not a gate in front of it. Be concrete: "the total at the bottom of
            the receipt is cut off" is useful, "poor quality" is not.

            Write for the person who uploaded the file, in the language the document is written in.
            """)
    DocumentAnalysis analyse(List<Content> document);
}
