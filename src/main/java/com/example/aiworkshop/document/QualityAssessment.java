package com.example.aiworkshop.document;

import dev.langchain4j.model.output.structured.Description;
import java.util.List;

/**
 * The agent's verdict on the uploaded file as an artefact — is it legible, is it whole, is it the
 * document it appears to be. Not a judgement of the contents.
 *
 * <p>This is advice shown to the person who uploaded, never a gate: a poor file is still accepted
 * and still stored. Blocking an upload behind a model's opinion is a worse experience than letting
 * it through with a warning attached.
 *
 * @param verdict the headline judgement, so the UI can pick a colour without reading prose
 * @param reason one plain-language sentence explaining the verdict to a non-expert
 * @param issues the specific problems found, one short phrase each; empty when the file is fine
 */
public record QualityAssessment(
        @Description("Overall usability of the file: GOOD, ACCEPTABLE or POOR.") Quality verdict,
        @Description("One sentence, in plain language, explaining the verdict to the person who uploaded the file.")
                String reason,
        @Description(
                        "Specific problems, one short phrase each, e.g. 'the bottom of the receipt is cut off'."
                                + " Empty if there are none.")
                List<String> issues) {

    /** Deliberately three values: a two-value verdict makes every imperfect scan look like a failure. */
    public enum Quality {
        /** Fully legible and complete. */
        GOOD,
        /** Usable, but with flaws worth mentioning. */
        ACCEPTABLE,
        /** Parts are unreadable or missing; a case handler will likely have to ask for it again. */
        POOR
    }
}
