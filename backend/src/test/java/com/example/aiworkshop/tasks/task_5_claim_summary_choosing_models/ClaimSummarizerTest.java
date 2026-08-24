package com.example.aiworkshop.tasks.task_5_claim_summary_choosing_models;

import com.example.aiworkshop.tasks.task_5_claim_summary_choosing_models.agent.ClaimSummarizer;
import static org.assertj.core.api.Assertions.assertThat;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.V;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
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
class ClaimSummarizerTest {

    private static Method summarise() {
        return Arrays.stream(ClaimSummarizer.class.getMethods())
                .filter(m -> m.getName().equals("summarise"))
                .findFirst()
                .orElseThrow();
    }

    @Test
    void hasASystemMessageOfItsOwn() {
        SystemMessage system = summarise().getAnnotation(SystemMessage.class);

        assertThat(system).describedAs("summarise() needs an @SystemMessage").isNotNull();
    }

    @Test
    void readsTheReadingsRatherThanTheFiles() {
        Method summarise = summarise();

        // By type rather than by position: the memory id was added in front of these later, and a
        // test that counts parameters breaks on a change that is not about what it is testing.
        Parameter documents = Arrays.stream(summarise.getParameters())
                .filter(p -> p.getParameterizedType().getTypeName().contains(DocumentForSummary.class.getName()))
                .findFirst()
                .orElse(null);

        assertThat(documents)
                .describedAs("the summariser is given what task 3 already worked out, not the files again")
                .isNotNull();
        assertThat(documents.getParameterizedType().getTypeName()).contains(List.class.getName());
        assertThat(documents.getAnnotation(V.class)).isNotNull();
    }
}
