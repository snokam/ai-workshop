package com.example.aiworkshop.document;

import dev.langchain4j.data.message.Content;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
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
 * <p>The instructions live in {@link SystemMessage} and the argument carries the file. The
 * {@code @UserMessage} on that argument is what tells LangChain4j the file <em>is</em> the user
 * message — without it, a second argument makes LangChain4j look for a user-message template and
 * the file is never forwarded. The Case's Required Documents are a {@link V} template variable, so
 * they are rendered into the system message rather than competing with the file for the user turn.
 */
public interface DocumentAnalyzer {

    @SystemMessage(
            """
            You are the intake agent in a case-handling system. Someone has just uploaded a file to
            their case, and you are the first to look at it.

            The case is waiting for these documents: {{requiredDocuments}}

            Do four things in one pass:

            1. CATEGORISE. Say what kind of document this is, as a short noun phrase — for example
               "invoice", "medical report", "proof of identity".

            2. EXTRACT. Pull out the handful of facts that matter about this document, as name/value
               pairs. Choose the fields that suit this kind of document; there is no fixed schema.
               Name them using the document's own wording. Leave the list empty if the file is too
               poor to read reliably — inventing a value is far worse than returning nothing.

            3. MATCH. Say which of the documents the case is waiting for this file satisfies, copying
               that label back exactly as it was given to you. If it satisfies none of them, return
               nothing for the match rather than forcing the closest one — a file that fits nothing
               is still accepted and still kept. Say how sure you are either way.

            4. ASSESS THE QUALITY of the file as an artefact, not of its contents. Is it legible? Is
               anything cut off, obscured or missing? Is it the whole document? Does it look like
               what it claims to be?

            You do not decide whether the upload is accepted — it already has been. Be concrete: "the
            total at the bottom of the receipt is cut off" is useful, "poor quality" is not.

            Write plainly and factually, in the language the document is written in. Both the person
            who uploaded the file and the case handler read what you write, so describe the document
            rather than addressing either of them.
            """)
    DocumentAnalysis analyse(
            @UserMessage List<Content> document, @V("requiredDocuments") List<String> requiredDocuments);
}
