package com.example.aiworkshop.document;

import dev.langchain4j.model.output.structured.Description;
import java.util.List;

/**
 * The agent's verdict on the uploaded file as an artefact — is it legible, is it whole, is it the
 * document it appears to be. Not a judgement of the contents.
 *
 * <p>Never a gate on upload: a poor file is still accepted and still stored. It does, though, hold
 * the Case it landed in at {@code NEEDS_REVIEW} until a Case Handler Reviews the Document or the
 * Claimant sends a better copy — advice to the Claimant, a signal to the handler, and in neither
 * case something standing between someone and their own Case.
 *
 * @param verdict the headline judgement, so the UI can pick a colour without reading prose
 * @param reason one plain-language sentence explaining the verdict, written for either reader
 * @param issues the specific problems found, one short phrase each; empty when the file is fine
 */
public record QualityAssessment(
        @Description("Overall usability of the file: GOOD, ACCEPTABLE or POOR.") Quality verdict,
        @Description("One plain-language sentence explaining the verdict. Describe the file, do not address anyone.")
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
