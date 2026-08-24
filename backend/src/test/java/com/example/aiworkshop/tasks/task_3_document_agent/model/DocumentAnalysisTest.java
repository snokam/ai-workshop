package com.example.aiworkshop.tasks.task_3_document_agent.model;

import static org.assertj.core.api.Assertions.assertThat;

import dev.langchain4j.model.output.structured.Description;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;

/**
 * The record is the schema, so an unwritten description is an unwritten prompt.
 *
 * <p>Nothing else would notice. A component with a placeholder on it still compiles, still appears
 * in the generated output format, and still gets filled in — with whatever the model makes of the
 * placeholder. The field arrives, the card renders, and the answer is quietly worse. That is the
 * failure this test exists to make loud.
 *
 * <p>The annotation is read off the <em>field</em>, not the record component. {@code @Description}
 * is declared {@code @Target({FIELD, TYPE})}, so writing it on a component puts it on the generated
 * field and nowhere else — ask the component or the accessor and both answer null, which is a
 * cheerful way to write a test that can only ever pass.
 */
class DocumentAnalysisTest {

    @Test
    void everyComponentHasADescriptionOfItsOwn() {
        assertThat(componentsWhere(description -> description == null))
                .describedAs("every component of DocumentAnalysis needs an @Description — it is what the"
                        + " model is told that field means")
                .isEmpty();
    }

    @Test
    void everyDescriptionHasBeenWritten() {
        assertThat(componentsWhere(description ->
                        description != null && String.join(" ", description.value()).contains("TODO")))
                .describedAs("these components still hold the note they shipped with — task 3, part 2 is"
                        + " writing the sentence the model is shown for each one")
                .isEmpty();
    }

    private static List<String> componentsWhere(Predicate<Description> unwanted) {
        return Arrays.stream(DocumentAnalysis.class.getRecordComponents())
                .filter(component -> unwanted.test(descriptionOn(component)))
                .map(RecordComponent::getName)
                .toList();
    }

    private static Description descriptionOn(RecordComponent component) {
        try {
            return DocumentAnalysis.class
                    .getDeclaredField(component.getName())
                    .getAnnotation(Description.class);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("a record component always has a field", e);
        }
    }
}
