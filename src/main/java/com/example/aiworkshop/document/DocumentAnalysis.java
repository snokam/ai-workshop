package com.example.aiworkshop.document;

import dev.langchain4j.model.output.structured.Description;
import java.util.List;

/**
 * What the intake agent returns after looking at one uploaded file.
 *
 * <p>This record is the output schema. LangChain4j derives the JSON contract from it and parses the
 * model's reply back into it — there is no prompt anywhere saying "reply with JSON", and no parsing
 * code to write.
 *
 * <h2>Your job</h2>
 *
 * The components are fixed, because the screen renders them. What is missing is the {@link
 * Description} on each one — the only place a field is explained to the model. Add them and watch
 * the answers sharpen without touching the system message.
 *
 * <p>The shape of this record is itself part of the prompt: adding a component here is how you ask
 * the agent for something new.
 */
public record DocumentAnalysis(
        // TODO: describe each component to the model.
        @Description("TODO") String category,
        @Description("TODO") String summary,
        @Description("TODO") List<ExtractedField> fields,
        @Description("TODO") QualityAssessment quality) {}
