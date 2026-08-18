package com.example.aiworkshop.tasks.task_2_document_agent;

import static org.assertj.core.api.Assertions.assertThat;

import dev.langchain4j.data.message.Content;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * The wiring that decides whether the file is sent at all.
 *
 * <p>The second test here is the one worth having. Without {@code @UserMessage} on the file
 * argument LangChain4j looks for a message template, finds none, and sends the call anyway — with
 * no file and no error. The agent then answers, confidently, about a document it never saw.
 */
class DocumentAnalyzerTest {

    private static Method analyse() throws NoSuchMethodException {
        return DocumentAnalyzer.class.getMethod("analyse", List.class, List.class);
    }

    @Test
    void hasASystemMessageOfItsOwn() throws Exception {
        SystemMessage system = analyse().getAnnotation(SystemMessage.class);

        assertThat(system).describedAs("analyse() needs an @SystemMessage").isNotNull();
        assertThat(String.join("\n", system.value()))
                .describedAs("the @SystemMessage still holds the brief it shipped with — write the prompt")
                .doesNotContain("TODO");
    }

    @Test
    void sendsTheFileAsTheUserMessage() throws Exception {
        Parameter file = analyse().getParameters()[0];

        assertThat(file.getAnnotation(UserMessage.class))
                .describedAs(
                        "without @UserMessage the file is never sent, the call still succeeds, and the agent"
                                + " describes a document it was not given")
                .isNotNull();
        assertThat(file.getParameterizedType().getTypeName())
                .describedAs("the file goes as Content, so a PDF or an image reaches the model as itself")
                .contains(Content.class.getName());
    }

    @Test
    void rendersTheChecklistAsAVariable() throws Exception {
        assertThat(analyse().getParameters()[1].getAnnotation(V.class))
                .describedAs("the required documents belong in the instructions, not in the turn with the file")
                .isNotNull();
        assertThat(String.join("\n", analyse().getAnnotation(SystemMessage.class).value()))
                .contains("{{requiredDocuments}}");
    }
}
