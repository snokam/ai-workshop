package com.example.aiworkshop.document;

import dev.langchain4j.model.output.structured.Description;

/**
 * One fact lifted out of a document.
 *
 * <p>Deliberately untyped name/value pairs. Once the four document types are pinned down, each type
 * gets its own record with real components ({@code InvoiceFields}, {@code MedicalReportFields}, …)
 * and the agent is asked for that type specifically. Until then, letting the agent choose the fields
 * is what keeps this working on any document someone drags in during the demo.
 *
 * @param name the field label, in the document's own wording
 * @param value the value as it appears in the document
 */
public record ExtractedField(
        @Description("The name of the fact, using the document's own wording.") String name,
        @Description("The value, as it appears in the document.") String value) {}
