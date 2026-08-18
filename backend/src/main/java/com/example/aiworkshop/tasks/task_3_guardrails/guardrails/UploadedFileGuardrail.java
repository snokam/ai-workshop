package com.example.aiworkshop.tasks.task_3_guardrails.guardrails;

import com.example.aiworkshop.tasks.task_3_guardrails.Guardrails;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.PdfFileContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailRequest;
import dev.langchain4j.guardrail.InputGuardrailResult;
import java.util.List;

public class UploadedFileGuardrail implements InputGuardrail {
    public static final String INTAKE_INSTRUCTION = "Analyse the attached file.";

    @Override
    public InputGuardrailResult validate(InputGuardrailRequest request) {
        // TODO — task 3. Refuse anything that is not one file plus the intake instruction.
        //
        // request.userMessage().contents() is what is about to be sent. Exactly one PdfFileContent
        // or ImageContent belongs there, and the only text allowed beside it is
        // Guardrails.INTAKE_INSTRUCTION. Return fatal(...) with a reason when it is anything else.
        //
        // Returning success() unconditionally is what an unwritten guardrail does: it lets
        // everything through, which is why the tests below are red.
        return success();

        // ── One version of the answer ──────────────────────────────────────────────────────
        // Try it yourself first. Uncomment this a piece at a time if you get stuck, or write
        // your own and read this after to argue with it.
        //
        // List<Content> contents = request.userMessage().contents();
        //
        // long files = contents.stream()
        // .filter(content -> content instanceof PdfFileContent || content instanceof ImageContent)
        // .count();
        // if (files != 1) {
        // return fatal("Intake sends exactly one file to the model; this message carries " + files + ".");
        // }
        //
        // for (Content content : contents) {
        // if (content instanceof TextContent text && !Guardrails.INTAKE_INSTRUCTION.equals(text.text())) {
        // return fatal("Only the intake instruction may accompany the file. Found: \"" + text.text() + "\"");
        // }
        // }
        // return success();
    }
}
