package com.example.aiworkshop.tasks.task_3_guardrails.guardrails;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.langchain4j.guardrail.OutputGuardrail;
import dev.langchain4j.guardrail.OutputGuardrailRequest;
import dev.langchain4j.guardrail.OutputGuardrailResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalysisGuardrail implements OutputGuardrail {
    private static final Logger log = LoggerFactory.getLogger(AnalysisGuardrail.class);

    static final String REQUIRED_DOCUMENTS = "requiredDocuments";

    private static final ObjectMapper JSON = new ObjectMapper();

    @Override
    public OutputGuardrailResult validate(OutputGuardrailRequest request) {
        // TODO — task 3. Check what came back before anyone downstream sees it.
        //
        // request.responseFromLLM().aiMessage().text() is the raw reply. Two things go wrong often
        // enough to be worth catching: it is not JSON at all — Gemini likes to wrap it in a ```json
        // fence, which jsonIn(...) below already strips — and it is JSON but missing the quality
        // verdict. Both are recoverable, so reprompt(...) rather than fail: say what was wrong and
        // ask for the whole analysis again.
        //
        // success() is what no guardrail at all looks like.
        return success();
    }

    private static String jsonIn(String reply) {
        int start = reply.indexOf('{');
        int end = reply.lastIndexOf('}');
        return start < 0 || end < start ? reply : reply.substring(start, end + 1);
    }

    private static List<String> requiredDocumentsIn(OutputGuardrailRequest request) {
        Map<String, Object> variables = request.requestParams().variables();
        Object required = variables == null ? null : variables.get(REQUIRED_DOCUMENTS);
        if (!(required instanceof List<?> labels)) {
            return List.of();
        }
        List<String> allowed = new ArrayList<>();
        labels.forEach(label -> allowed.add(String.valueOf(label)));
        return allowed;
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? null : value.asText();
    }
}
