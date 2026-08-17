package com.example.aiworkshop.guardrail;

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

/**
 * An {@link OutputGuardrail}: it runs on the model's reply before LangChain4j parses it into a
 * {@code DocumentAnalysis}, and it is where the document-handling rules that Java can actually
 * enforce are enforced.
 *
 * <p>This is the half of the defence that works. The intake agent is told a document is evidence and
 * never instruction, and mostly that holds — but "mostly" is not a security property, and a document
 * that talks the agent into something has to meet a rule that no prose can talk its way past. The
 * rules here are the ones with a definite answer:
 *
 * <ol>
 *   <li><b>A match must be a Required Document the Case actually asked for.</b> The agent is handed
 *       the list and told to copy a label back verbatim. A label that is not on the list matched
 *       nothing — whether the agent paraphrased it, invented it, or was told to invent it by the
 *       document. Corrected in place with {@link OutputGuardrailResult#successWith}: the reply is
 *       rewritten with a null match and the upload proceeds. No second model call, no failed upload,
 *       and the Claimant sees the ordinary "matches none of the documents this case needs".
 *   <li><b>The reply must be an analysis.</b> Unparseable JSON, or JSON missing the parts every
 *       Document needs, earns a {@link OutputGuardrailResult#reprompt} — the one case worth paying
 *       for another call, because there is nothing here to correct and nothing to store.
 * </ol>
 *
 * <p>What it deliberately does not do is judge the prose. A guardrail that tried to decide whether a
 * summary had been influenced by a document would be a second model with the same weakness as the
 * first, and it would fail closed on honest documents — which, on this application, means holding up
 * somebody's insurance claim over a turn of phrase.
 */
public class AnalysisGuardrail implements OutputGuardrail {

    private static final Logger log = LoggerFactory.getLogger(AnalysisGuardrail.class);

    /** The template variable the intake agent is given; see {@code DocumentAnalyzer}. */
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
        if (allowed.contains(claimed)) {
            return success();
        }

        log.warn(
                "Guardrail: the agent matched '{}', which is not one of {}. Recording no match instead.",
                claimed,
                allowed);
        ObjectNode corrected = ((ObjectNode) analysis).deepCopy();
        corrected.putNull("matchedRequiredDocument");
        corrected.put("matchConfidence", "LOW");
        return successWith(corrected.toString());
    }

    /**
     * The JSON inside the reply.
     *
     * <p>An output guardrail sees the model's raw text, not the object LangChain4j will parse out of
     * it — it runs first, which is the whole point of it. Gemini wraps structured output in a
     * markdown fence, so the raw text is {@code ```json\n{...}\n```} and reading it as JSON fails on
     * the first backtick. LangChain4j strips that itself, one step later; this does the same, one
     * step earlier.
     *
     * <p>Taking everything between the first brace and the last is deliberately blunt, and it is
     * enough: the object is the reply, and anything a model writes around it is decoration.
     */
    private static String jsonIn(String reply) {
        int start = reply.indexOf('{');
        int end = reply.lastIndexOf('}');
        return start < 0 || end < start ? reply : reply.substring(start, end + 1);
    }

    /**
     * The Case's Required Documents, read off the same template variables the system message was
     * rendered from. The guardrail therefore checks the reply against the list this call actually
     * used, rather than against a list it was configured with once and would go stale.
     */
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
