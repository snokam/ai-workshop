package com.example.aiworkshop.tasks.task_8_evaluation;

import com.example.aiworkshop.tasks.task_1_first_agent.model.CaseType;
import com.example.aiworkshop.tasks.task_1_first_agent.model.CaseTypeSuggestion;
import com.example.aiworkshop.tasks.task_1_first_agent.agent.CaseTypeClassifier;
import com.example.aiworkshop.tasks.task_8_evaluation.LabelledCase;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Task 8. The only thing here that calls a model, and the only thing that asks whether the agent is
 * any good rather than whether it is wired up.
 *
 * <p>Disabled on purpose. It costs ten calls and needs credentials, so it runs when you ask for it:
 *
 * <pre>./mvnw test -Dtest=ClassifierEvaluation -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false</pre>
 *
 * <p>Take the {@code @Disabled} off to run it from an IDE.
 *
 * <p>It prints rather than asserts, and that is the exercise. A number on its own decides nothing:
 * the useful part is reading the disagreements and working out which of them are the model being
 * wrong and which are the label being an opinion. Half of the set was chosen to be arguable for
 * exactly that reason.
 */
@SpringBootTest
@Disabled("calls the model ten times — run it deliberately, see the class comment")
class ClassifierEvaluation {

    @Autowired
    private CaseTypeClassifier classifier;

    @Test
    void scoreTheClassifier() {
        List<String> disagreements = new ArrayList<>();
        int agreed = 0;

        System.out.printf("%n%-62s %-16s %-16s %s%n", "description", "expected", "answered", "confidence");
        System.out.println("-".repeat(120));

        for (LabelledCase example : LabelledCase.all()) {
            CaseTypeSuggestion answer = classifier.classify(CaseType.catalog(), example.description());
            boolean same = answer.type() == example.expected();
            agreed += same ? 1 : 0;

            System.out.printf(
                    "%-62s %-16s %-16s %s%s%n",
                    example.description().length() > 60
                            ? example.description().substring(0, 57) + "..."
                            : example.description(),
                    example.expected(),
                    answer.type(),
                    answer.confidence(),
                    same ? "" : "   <-- disagrees");

            if (!same) {
                disagreements.add("%s%n    expected %s, answered %s (%s)%n    the label says: %s%n    the agent says: %s"
                        .formatted(
                                example.description(),
                                example.expected(),
                                answer.type(),
                                answer.confidence(),
                                example.why(),
                                answer.rationale()));
            }
        }

        System.out.printf("%n%d of %d agreed with the label.%n", agreed, LabelledCase.all().size());
        if (!disagreements.isEmpty()) {
            System.out.printf("%nThe %d worth arguing about:%n%n", disagreements.size());
            disagreements.forEach(d -> System.out.println(d + System.lineSeparator()));
        }
        System.out.println(
                """
                Now decide, one at a time: is the model wrong, or is the label an opinion?
                Only the first kind is a bug. Changing the prompt to win the second kind is how an
                agent gets worse at the job while getting better at the test.
                """);
    }
}
