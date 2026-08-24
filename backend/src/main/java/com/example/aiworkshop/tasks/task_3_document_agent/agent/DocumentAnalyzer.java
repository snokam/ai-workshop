package com.example.aiworkshop.tasks.task_3_document_agent.agent;

import com.example.aiworkshop.tasks.task_3_document_agent.model.DocumentAnalysis;
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
            TODO — task 3, part 1. Write the agent.

            The same shape as task 1, handed a file instead of a sentence:

              analyse(@UserMessage List<Content> document, @V("requiredDocuments") List<String> required)

            @UserMessage on the argument is what makes the file the user turn. Without it the model is sent
            nothing and answers anyway, which fails silently — the one bug in this task that looks like a bad
            model rather than a missing annotation.

            The prompt has to make it:
              1. say what kind of document this is, as a short noun phrase
              2. pull out the facts a handler would care about, as name/value pairs
              3. decide which of {{requiredDocuments}} it satisfies — copied exactly, or none
              4. judge whether the file is legible enough to work with, and why
              5. record any text addressed to the software rather than to a person

            That is DocumentAnalysis, the record it returns. Read it — the @Description on each component is
            part of the prompt.
            """)

    DocumentAnalysis analyse(
            @UserMessage List<Content> document, @V("requiredDocuments") List<String> requiredDocuments);
}
