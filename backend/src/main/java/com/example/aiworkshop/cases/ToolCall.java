package com.example.aiworkshop.cases;

import dev.langchain4j.service.tool.ToolExecution;

/**
 * One thing the Case Chat agent looked up while answering, shown to the Case Handler underneath the
 * answer.
 *
 * <p>Not diagnostics. A handler reading an answer needs to know whether a number was fetched from a
 * named Document or produced from the index, and the arguments are where the Document's name is —
 * which is why they are carried rather than just the tool name.
 *
 * @param arguments the JSON the model sent, verbatim
 */
public record ToolCall(String name, String arguments) {

    public static ToolCall of(ToolExecution execution) {
        return new ToolCall(execution.request().name(), execution.request().arguments());
    }
}
