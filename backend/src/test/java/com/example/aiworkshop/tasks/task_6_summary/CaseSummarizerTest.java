package com.example.aiworkshop.tasks.task_6_summary;

import com.example.aiworkshop.tasks.task_6_summary.DocumentForSummary;
import static org.assertj.core.api.Assertions.assertThat;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.V;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * What the expensive agent is shown, which is the decision worth protecting.
 *
 * <p>It is handed each document's reading, not its bytes. Reading them again here would pay twice
 * for an answer task 2 already produced, and the two readings could disagree — the same file
 * described two ways, on one screen.
 */
class CaseSummarizerTest {

    private static Method summarise() {
        return Arrays.stream(CaseSummarizer.class.getMethods())
                .filter(m -> m.getName().equals("summarise"))
                .findFirst()
                .orElseThrow();
    }

    @Test
    void hasASystemMessageOfItsOwn() {
        SystemMessage system = summarise().getAnnotation(SystemMessage.class);

        assertThat(system).describedAs("summarise() needs an @SystemMessage").isNotNull();
        assertThat(String.join("\n", system.value()))
                .describedAs("the @SystemMessage still holds the brief it shipped with — write the prompt")
                .doesNotContain("TODO");
    }

    @Test
    void readsTheReadingsRatherThanTheFiles() {
        Method summarise = summarise();

        assertThat(summarise.getParameters()[1].getParameterizedType().getTypeName())
                .describedAs("the summariser is given what task 2 already worked out, not the files again")
                .contains(DocumentForSummary.class.getName())
                .contains(List.class.getName());
        assertThat(summarise.getParameters()[1].getAnnotation(V.class)).isNotNull();
    }
}
