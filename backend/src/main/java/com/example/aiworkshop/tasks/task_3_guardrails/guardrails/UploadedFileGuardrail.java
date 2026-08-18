package com.example.aiworkshop.tasks.task_3_guardrails.guardrails;

import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.PdfFileContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailRequest;
import com.example.aiworkshop.tasks.task_3_guardrails.Guardrails;
import dev.langchain4j.guardrail.InputGuardrailResult;
import java.util.List;

public class UploadedFileGuardrail implements InputGuardrail {
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
            if (content instanceof TextContent text && !Guardrails.INTAKE_INSTRUCTION.equals(text.text())) {
                return fatal("Only the intake instruction may accompany the file. Found: \"" + text.text() + "\"");
            }
        }
        return success();
    }
}
