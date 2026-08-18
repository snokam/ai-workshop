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
        // TODO — task 3. Refuse anything that is not one file plus the intake instruction.
        //
        // request.userMessage().contents() is what is about to be sent. Exactly one PdfFileContent
        // or ImageContent belongs there, and the only text allowed beside it is
        // Guardrails.INTAKE_INSTRUCTION. Return fatal(...) with a reason when it is anything else.
        //
        // Returning success() unconditionally is what an unwritten guardrail does: it lets
        // everything through, which is why the tests below are red.
        return success();
    }
}
