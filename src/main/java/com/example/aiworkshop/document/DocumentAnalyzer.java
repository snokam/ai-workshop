package com.example.aiworkshop.document;

import dev.langchain4j.data.message.Content;
import dev.langchain4j.service.SystemMessage;
import java.util.List;

/**
 * The intake agent: the first thing that looks at a file after someone uploads it.
 *
 * <p>This interface <em>is</em> the agent. There is no implementation to write — LangChain4j builds
 * one at runtime from the system message below and the shape of {@link DocumentAnalysis}. The file
 * arrives as {@link Content} (a {@code PdfFileContent} or an {@code ImageContent}), so the model
 * reads the actual PDF or photo; nothing extracts text first.
 *
 * <p>Note the missing {@code @UserMessage}: LangChain4j only forwards {@link Content} arguments when
 * there is no user-message template to render instead. Instructions therefore live in
 * {@link SystemMessage}, and the argument carries the file.
 *
 * <h2>Your job</h2>
 *
 * Everything already runs — upload a file and this agent replies. It just replies badly, because it
 * has been told almost nothing. Write the system message, then work on {@link DocumentAnalysis}.
 * Those two files are the entire agent; nothing else in the application needs to change.
 */
public interface DocumentAnalyzer {

    // TODO: tell the agent what it is looking at, and what to do with it. Three things are wanted:
    //   what kind of document this is, the facts worth lifting out of it, and whether the file
    //   itself is legible and complete. Be specific about the last one — "poor quality" helps
    //   nobody, "the total at the bottom of the receipt is cut off" does.
    @SystemMessage("You look at documents.")
    DocumentAnalysis analyse(List<Content> document);
}
