package com.example.aiworkshop.tasks.task_1_first_agent;

import static org.assertj.core.api.Assertions.assertThat;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import org.junit.jupiter.api.Test;

/**
 * What the agent is, checked without calling a model.
 *
 * <p>None of this asks whether the prompt is any good — no test can, and one that called the model
 * would be slow, need credentials, and go red for reasons that have nothing to do with your work.
 * What it can check is the wiring, and every assertion here is a mistake that fails silently: a
 * prompt still holding its TODO, a template variable that is never rendered, an argument that is
 * never sent.
 */
class CaseTypeClassifierTest {

    private static Method classify() throws NoSuchMethodException {
        return CaseTypeClassifier.class.getMethod("classify", String.class, String.class);
    }

    @Test
    void hasASystemMessageOfItsOwn() throws Exception {
        SystemMessage system = classify().getAnnotation(SystemMessage.class);

        assertThat(system)
                .describedAs("classify() needs an @SystemMessage: that is where the agent is defined")
                .isNotNull();
        assertThat(String.join("\n", system.value()))
                .describedAs("the @SystemMessage still holds the brief it shipped with — write the prompt")
                .doesNotContain("TODO");
    }

    @Test
    void rendersTheCatalogueInsteadOfHardCodingIt() throws Exception {
        assertThat(String.join("\n", classify().getAnnotation(SystemMessage.class).value()))
                .describedAs(
                        "the prompt should render {{caseTypes}}, so the list the agent chooses from is the enum"
                                + " the case is opened from rather than a second copy that goes stale")
                .contains("{{caseTypes}}");
    }

    @Test
    void keepsTheDescriptionInTheUserTurn() throws Exception {
        Parameter[] parameters = classify().getParameters();

        assertThat(parameters[0].getAnnotation(V.class))
                .describedAs("the catalogue is a template variable, so it belongs in the system message")
                .isNotNull();
        assertThat(parameters[1].getAnnotation(UserMessage.class))
                .describedAs(
                        "the description is untrusted free text and belongs in the user turn, not in the"
                                + " instructions")
                .isNotNull();
    }
}
