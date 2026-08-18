package com.example.aiworkshop.document;

import dev.langchain4j.model.output.structured.Description;

public record ExtractedField(
        @Description("The name of the fact, using the document's own wording.") String name,
        @Description("The value, as it appears in the document.") String value) {}
