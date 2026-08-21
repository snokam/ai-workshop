package com.example.aiworkshop.tasks.task_3_guardrails.guardrails;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.langchain4j.guardrail.OutputGuardrail;
import dev.langchain4j.guardrail.OutputGuardrailRequest;
import dev.langchain4j.guardrail.OutputGuardrailResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalysisGuardrail implements OutputGuardrail {
    private static final Logger log = LoggerFactory.getLogger(AnalysisGuardrail.class);

    static final String REQUIRED_DOCUMENTS = "requiredDocuments";

    private static final ObjectMapper JSON = new ObjectMapper();

    @Override
    public OutputGuardrailResult validate(OutputGuardrailRequest request) {
        String reply = request.responseFromLLM().aiMessage().text();

        JsonNode analysis;
        try {
        analysis = JSON.readTree(jsonIn(reply));
        } catch (Exception e) {
        log.warn(
        "Guardrail: the reply could not be read as an analysis. It began: {}",
        reply.substring(0, Math.min(200, reply.length())));
        return reprompt(
        "The model's reply was not JSON",
        "Your reply could not be read as JSON. Send the analysis again, as a single JSON object"
        + " matching the schema and nothing else.");
        }

        if (analysis.path("quality").path("verdict").isMissingNode()) {
        return reprompt(
        "The model's reply had no quality verdict",
        "Your reply was missing the quality assessment. Send the whole analysis again, including"
        + " the quality verdict, and follow the schema exactly.");
        }

        String claimed = text(analysis, "matchedRequiredDocument");
        if (claimed == null) {
        return success();
        }

        List<String> allowed = requiredDocumentsIn(request);
        if (!allowed.contains(claimed)) {
        log.warn(
        "Guardrail: the agent matched '{}', which is not one of {}. Recording no match instead.",
        claimed,
        allowed);
        return strikeOut(analysis);
        }

        Optional<String> aimedAtUs = AddressedToTheAgent.phraseIn(analysis);
        if (aimedAtUs.isPresent()) {
        log.warn(
        "Guardrail: the document says \"{}\", which is addressed to the software rather than to a"
        + " person. Striking out its claim to be '{}'.",
        aimedAtUs.get(),
        claimed);
        return strikeOut(analysis);
        }

        return success();

        // ── To set this task again ────────────────────────────────────────────────────────
        // TODO — task 3. Check what came back before anyone downstream sees it.
        //
        // request.responseFromLLM().aiMessage().text() is the raw reply. Two things go wrong often
        // enough to be worth catching: it is not JSON at all — Gemini likes to wrap it in a ```json
        // fence, which jsonIn(...) below already strips — and it is JSON but missing the quality
        // verdict. Both are recoverable, so reprompt(...) rather than fail: say what was wrong and
        // ask for the whole analysis again.
        //
        // success() is what no guardrail at all looks like.
        // return success();
    }

    /**
     * Both rules end the same way: the claim is removed and the confidence dropped. The document is
     * still stored, still shown, still summarised — it simply does not get to satisfy anything the
     * case asked for.
     *
     * <p>One guardrail owns the rewrite on purpose. This started as two, and the second one's plain
     * success() handed back the original reply and quietly undid the first one's correction. Output
     * guardrails that rewrite do not chain: whichever runs last decides, and one that has nothing to
     * say says "use what the model sent".
     */
    private OutputGuardrailResult strikeOut(JsonNode analysis) {
        ObjectNode corrected = ((ObjectNode) analysis).deepCopy();
        corrected.putNull("matchedRequiredDocument");
        corrected.put("matchConfidence", "LOW");
        return successWith(corrected.toString());
    }

    static String jsonIn(String reply) {
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
