package com.example.aiworkshop.document.model;

import dev.langchain4j.model.output.structured.Description;
import java.util.List;

public record QualityAssessment(
        @Description("Overall usability of the file: GOOD, ACCEPTABLE or POOR.") Quality verdict,
        @Description("One plain-language sentence explaining the verdict. Describe the file, do not address anyone.")
                String reason,
        @Description(
                        "Specific problems, one short phrase each, e.g. 'the bottom of the receipt is cut off'."
                                + " Empty if there are none.")
                List<String> issues) {
    public enum Quality {
        GOOD,
        ACCEPTABLE,
        POOR
    }
}
