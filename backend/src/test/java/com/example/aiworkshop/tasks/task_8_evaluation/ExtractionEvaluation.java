package com.example.aiworkshop.tasks.task_8_evaluation;

import com.example.aiworkshop.tasks.task_2_document_agent.DocumentIntake;
import com.example.aiworkshop.tasks.task_2_document_agent.agent.DocumentAnalyzer;
import com.example.aiworkshop.tasks.task_2_document_agent.model.DocumentAnalysis;
import com.example.aiworkshop.tasks.task_2_document_agent.model.ExtractedField;
import com.example.aiworkshop.tasks.task_2_document_agent.store.DocumentFiles;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.TextContent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Task 8, the second technique: scoring an agent that answers with facts rather than a category.
 *
 * <pre>./mvnw test -Dtest=ExtractionEvaluation -Dsurefire.failIfNoSpecifiedTests=false</pre>
 *
 * <p>The classifier evaluation could count agreements because there was one answer and a list to
 * compare it against. Here the agent decides both what the facts are and how to word them, so
 * "agreed" is not available. What is available is coverage — of the things a handler needs, how
 * many are somewhere in the answer — and its opposite, whether anything appears that was never in
 * the document.
 *
 * <p>Those two are not the same measurement and should never be added together. An agent that finds
 * everything and invents nothing is working. An agent that finds everything and invents one figure
 * is dangerous, and a single number would hide that.
 */
@SpringBootTest
@Disabled("reads three files with the model — run it deliberately, see the class comment")
class ExtractionEvaluation {

    private static final Path ASSETS = Path.of("../assets");

    @Autowired
    private DocumentAnalyzer analyzer;

    @Test
    void scoreTheExtraction() throws Exception {
        System.out.printf("%n%-24s %-9s %s%n", "file", "found", "invented");
        System.out.println("-".repeat(90));

        for (ExtractedFacts expected : ExtractedFacts.all()) {
            DocumentAnalysis analysis = analyzer.analyse(fileAt(expected.file()), List.of("receipt for the repair"));
            String answered = everythingItSaid(analysis).toLowerCase(Locale.ROOT);

            List<String> missed = expected.mustFind().stream()
                    .filter(fact -> !answered.contains(fact.toLowerCase(Locale.ROOT)))
                    .toList();
            List<String> invented = expected.mustNotSay().stream()
                    .filter(phrase -> answered.contains(phrase.toLowerCase(Locale.ROOT)))
                    .toList();

            int wanted = expected.mustFind().size();
            System.out.printf(
                    "%-24s %-9s %s%n",
                    expected.file(),
                    wanted == 0 ? "—" : (wanted - missed.size()) + "/" + wanted,
                    invented.isEmpty() ? "none" : String.join(", ", invented) + "   <-- look at this");

            if (wanted == 0) {
                System.out.printf("    nothing to find yet: %s%n", expected.why());
            }
            if (!missed.isEmpty()) {
                System.out.printf("    did not find: %s%n", String.join(", ", missed));
            }
            System.out.printf("    it said: %s%n%n", renderedFields(analysis));
        }

        System.out.println(
                """
                Two questions, and they are not the same question.

                What it missed is a coverage problem, and the fix is usually the prompt — the agent was
                not told that the total is the thing a handler looks for first.

                What it invented is not a coverage problem and no amount of prompting reliably fixes
                it. Decide instead where you would catch it: a check in Java against the document, a
                field the agent is not allowed to fill, or a human who has to look before it counts.

                And notice how much arguing you did with the scoring itself. "20 468,75" against
                "20468.75" is a decision you made, not one the agent got wrong. Every extraction
                evaluation is mostly that, which is why so few teams have one.
                """);
    }

    private List<Content> fileAt(String name) throws Exception {
        byte[] bytes = Files.readAllBytes(ASSETS.resolve(name));
        String mimeType = name.endsWith(".pdf") ? "application/pdf" : "image/png";
        return List.of(
                TextContent.from(DocumentIntake.INTAKE_INSTRUCTION), DocumentFiles.contentOf(bytes, mimeType));
    }

    private static String everythingItSaid(DocumentAnalysis analysis) {
        return analysis.category() + " " + analysis.summary() + " " + renderedFields(analysis);
    }

    private static String renderedFields(DocumentAnalysis analysis) {
        return analysis.fields().stream()
                .map(field -> field.name() + "=" + field.value())
                .reduce((a, b) -> a + " | " + b)
                .orElse("(no fields)");
    }
}
