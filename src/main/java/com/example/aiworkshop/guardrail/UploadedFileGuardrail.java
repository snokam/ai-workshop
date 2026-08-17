package com.example.aiworkshop.guardrail;

import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.PdfFileContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailRequest;
import dev.langchain4j.guardrail.InputGuardrailResult;
import java.util.List;

/**
 * An {@link InputGuardrail}: it runs on the user message on its way to the model, before a single
 * token is spent.
 *
 * <p><b>What it can and cannot do.</b> It sees the message LangChain4j assembled — for intake, one
 * sentence of ours and one file. It does <em>not</em> see inside the file: the words printed in a
 * PDF or photographed on a page are pixels and bytes until the model reads them, and no guardrail
 * on this side of the call can know what they say. Anyone claiming an input guardrail stops prompt
 * injection through an uploaded document is selling something.
 *
 * <p><b>What it is for, then.</b> It holds the boundary that decides <em>whose words</em> reach the
 * model. Intake sends a fixed instruction and the file itself; nothing a Claimant typed — the
 * filename above all — is allowed to become part of the prompt. That was a comment in
 * {@code DocumentIntake} and an intention in a code review. Here it is a check that fails the call.
 *
 * <p>Failures are {@link InputGuardrailResult#fatal}, which stops the call rather than retrying it.
 * A retry would send the same offending message again; this failure means the application built a
 * prompt it should not have, which is a bug to fix and not a condition to sit out.
 */
public class UploadedFileGuardrail implements InputGuardrail {

    /** The only text intake is allowed to put in front of the model. */
    public static final String INTAKE_INSTRUCTION = "Analyse the attached file.";

    @Override
    public InputGuardrailResult validate(InputGuardrailRequest request) {
        List<Content> contents = request.userMessage().contents();

        long files = contents.stream()
                .filter(content -> content instanceof PdfFileContent || content instanceof ImageContent)
                .count();
        if (files != 1) {
            return fatal("Intake sends exactly one file to the model; this message carries " + files + ".");
        }

        for (Content content : contents) {
            if (content instanceof TextContent text && !INTAKE_INSTRUCTION.equals(text.text())) {
                // The only way text other than ours gets here is if something upstream started
                // putting user-supplied strings into the prompt. Naming the text in the failure is
                // safe — it never reaches a Claimant, and whoever is reading the log needs to see
                // what got through.
                return fatal("Only the intake instruction may accompany the file. Found: \"" + text.text() + "\"");
            }
        }
        return success();
    }
}
