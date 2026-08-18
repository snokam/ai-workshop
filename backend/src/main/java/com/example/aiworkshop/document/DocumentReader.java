package com.example.aiworkshop.document;

import dev.langchain4j.data.message.Content;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import java.util.List;
import com.example.aiworkshop.tasks.task_2_document_agent.DocumentAnalyzer;

/**
 * A second look at the original file, for a question no Extraction answers.
 *
 * <p>One-shot and given no Case context at all — not the Case Summary, not what the Case is waiting
 * for, not why it is being asked. A reader that knew what answer would be convenient could be led to
 * it, and the whole value of going back to the file is that it reports what is on the file.
 *
 * <p>It exists because a tool cannot hand the model a file. LangChain4j supports returning image
 * content from a tool, but the documented provider list covers neither Vertex AI Gemini nor the
 * OpenAI-compatible Foundry path this application also runs on, and PDF content is not a supported
 * tool return type on any of them. Anyone tempted to simplify this agent away will meet that first.
 *
 * <p>Shaped like {@link DocumentAnalyzer}: the file is the user message, so the question rides in
 * the system message as a template variable rather than competing with it for the user turn.
 *
 * <p>Per ADR 0002 the English rule is stated here rather than inherited from anywhere.
 */
public interface DocumentReader {

    @SystemMessage(
            """
            You are looking at one file on behalf of a case handler who cannot make something out on
            it. You know nothing else about the case, and you do not need to.

            The question is: {{question}}

            Answer only from what is visible on the file. Quote what you can read. If part of it is
            obscured, cut off, or too poor to be sure of, say which part and say what you can see
            rather than guessing at the rest.

            If the file does not show what was asked about at all, say so plainly and stop. That is a
            useful answer — it is what tells a case handler to stop looking and ask the claimant
            instead. A plausible answer invented from the rest of the file is the one thing that
            would make this worse than not asking.

            Write in English, whatever language the file is in. Anything you quote off the file —
            field names, values, amounts — stays exactly as it appears there, untranslated.
            """)
    String read(@UserMessage List<Content> file, @V("question") String question);
}
