package com.example.aiworkshop.tasks.task_2_document_agent;

import dev.langchain4j.data.message.Content;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import java.util.List;
import com.example.aiworkshop.document.DocumentAnalysis;

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
    /**
     * ── TASK DOCUMENT AGENT ────────────────────────────────────────────────────────────────────────
     * Set to true once you have written the document intake agent below. While it is false the
     * application still runs: every screen that does not need this agent works as normal,
     * and the one that does explains which file to open.
     * ──────────────────────────────────────────────────────────────────────────────────
     */
    boolean IMPLEMENTED = false;


    @SystemMessage(
            """
            TODO — task 2.

            Write the system message for an agent that is handed an uploaded file and has to
            describe it: what kind of document it is, the handful of facts worth pulling off it,
            which of the case's required documents it satisfies, whether the file is legible enough
            to work with, and whether anything in it is addressed to you rather than to a reader.

            The Case's required documents arrive through {{requiredDocuments}}. What you return is
            DocumentAnalysis, and that record is the whole of the output schema.

            The solutions branch has the version this was written from.
            """)
    DocumentAnalysis analyse(
            @UserMessage List<Content> document, @V("requiredDocuments") List<String> requiredDocuments);
}
