package com.example.aiworkshop.tasks.task_4_evaluation;

import com.example.aiworkshop.tasks.task_1_first_agent.model.ClaimType;
import com.example.aiworkshop.tasks.task_1_first_agent.model.ClaimTypeSuggestion;
import com.example.aiworkshop.tasks.task_1_first_agent.agent.ClaimTypeClassifier;
import com.example.aiworkshop.workshop.TaskNotImplementedException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * The first evaluation: is the classifier you wrote in task 1 any good?
 *
 * <pre>cd backend && ./mvnw test -Pevaluate</pre>
 *
 * <p>Kept out of the ordinary test run by its tag, because it calls a real model once per row.
 * {@code ./mvnw test} stays free and needs no credentials; this costs about ten calls.
 *
 * <p>The rules it is scored against are the classifier's own: it picks exactly one of the five claim
 * types, or nothing at all when none of them fit, and says how sure it is. Those are the rules to
 * label against in {@link LabelledClaim}.
 *
 * <p>It prints rather than asserts, and that is the exercise. A number on its own decides nothing —
 * the useful part is reading the disagreements and working out which are the model being wrong and
 * which are the label being an opinion. Half of the set should be arguable for exactly that reason.
 */
@SpringBootTest
@Tag("evaluation")
class ClassifierEvaluation {

    @Autowired
    private ClaimTypeClassifier classifier;

    @Test
    void scoreTheClassifier() {
        try {
            classifier.classify(ClaimType.catalog(), "a warm-up call, to fail early if task 1 is not written");
        } catch (TaskNotImplementedException notYet) {
            System.out.println("\nThere is nothing to evaluate yet — task 1's classifier is not written.\n");
            return;
        }

        List<String> disagreements = new ArrayList<>();
        int agreed = 0;

        System.out.printf("%n%-62s %-16s %-16s %s%n", "description", "expected", "answered", "confidence");
        System.out.println("-".repeat(120));

        for (LabelledClaim example : LabelledClaim.all()) {
            ClaimTypeSuggestion answer = classifier.classify(ClaimType.catalog(), example.description());
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
                disagreements.add("%s%n    expected %s, answered %s (%s)%n    the agent says: %s"
                        .formatted(
                                example.description(),
                                example.expected(),
                                answer.type(),
                                answer.confidence(),
                                answer.rationale()));
            }
        }

        System.out.printf("%n%d of %d agreed with the label.%n", agreed, LabelledClaim.all().size());
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
