package com.example.aiworkshop.tasks.task_4_evaluation;

import com.example.aiworkshop.tasks.task_2_guardrails.prompt_injection.PromptInjectionGuardrail;
import com.example.aiworkshop.tasks.task_4_evaluation.GuardrailProbe.Expected;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailResult;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * The second evaluation: are the two guardrails you wrote in task 2 any good?
 *
 * <pre>cd backend && ./mvnw test -Pevaluate</pre>
 *
 * <p>This runs the real guardrails, in the real order, over {@link GuardrailProbe}. Nothing is
 * mocked: each probe goes to the injection check first and to the claim check second, exactly as an
 * upload from the report screen would, and what comes back is what the person at the keyboard would
 * have got.
 *
 * <p>It prints rather than asserts. A guardrail is not a feature that works or does not — it is a
 * pair of error rates you are choosing between, and no assertion can pick the trade for you. What
 * the table gives you is which way each mistake went, which is the only part that matters:
 *
 * <ul>
 *   <li><b>refused something real</b> — a person is turned away at the door, and the injection
 *       guardrail deliberately tells them nothing about why. This is the expensive one.
 *   <li><b>let something through</b> — attacker-controlled text reached a model, or you paid for a
 *       call on "asdf".
 * </ul>
 *
 * <p>Do not average them. Nine out of ten is not ninety per cent of a guardrail: an attacker only
 * needs the tenth, and will send it a thousand times.
 */
@SpringBootTest
@Tag("evaluation")
class GuardrailEvaluation {

    /** The guardrails as the application has them, in the order {@code @Order} puts them in. */
    @Autowired
    private List<InputGuardrail> beforeTheCall;

    @Test
    void scoreTheGuardrails() {
        if (GuardrailProbe.all().isEmpty()) {
            System.out.println("\nGuardrailProbe.all() is empty — task 4, part 2 is writing the probes.\n");
            return;
        }

        List<String> wrong = new ArrayList<>();
        int held = 0;

        System.out.printf("%n%-58s %-24s %-24s %s%n", "typed into the box", "expected", "happened", "");
        System.out.println("-".repeat(118));

        for (GuardrailProbe probe : GuardrailProbe.all()) {
            Expected happened = whatHappensTo(probe.text());
            boolean agreed = happened == probe.expected();
            held += agreed ? 1 : 0;

            System.out.printf(
                    "%-58s %-24s %-24s %s%n",
                    shortened(probe.text()),
                    probe.expected(),
                    happened,
                    agreed ? "" : "   <-- " + howItWentWrong(probe.expected(), happened));

            if (!agreed) {
                wrong.add("%s%n    expected %s, got %s%n    the label says: %s%n    %s"
                        .formatted(probe.text(), probe.expected(), happened, probe.why(),
                                howItWentWrong(probe.expected(), happened)));
            }
        }

        System.out.printf("%n%d of %d behaved.%n", held, GuardrailProbe.all().size());
        if (!wrong.isEmpty()) {
            System.out.printf("%nThe %d that did not:%n%n", wrong.size());
            wrong.forEach(row -> System.out.println(row + System.lineSeparator()));
        }
        System.out.println(
                """
                For each one, decide which of these it is before you touch a prompt:

                  the label is wrong    you would not actually refuse that either, on reflection
                  the prompt is wrong   the rule you meant is not the rule you wrote
                  the model is wrong    the prompt says it plainly and the answer is still not it

                Only the third is a reason to reach for a different model, and it is the rarest.
                Tightening a prompt until this table is green is how a guardrail gets better at the
                test and worse at the job — the set is twelve rows and the box is open to everyone.
                """);
    }

    /**
     * What the person at the keyboard would actually experience, decided by which guardrail stopped
     * it. The loop is the same shape LangChain4j uses: run them in order, stop at the first refusal.
     */
    private Expected whatHappensTo(String text) {
        for (InputGuardrail guardrail : beforeTheCall) {
            InputGuardrailResult result = guardrail.validate(UserMessage.from(text));
            if (!result.isSuccess()) {
                return guardrail instanceof PromptInjectionGuardrail
                        ? Expected.ADDRESSED_TO_THE_SYSTEM
                        : Expected.NOTHING_TO_WORK_WITH;
            }
        }
        return Expected.REACHES_THE_MODEL;
    }

    private static String howItWentWrong(Expected expected, Expected happened) {
        if (expected == Expected.REACHES_THE_MODEL) {
            return "REFUSED SOMETHING REAL — a person was turned away";
        }
        if (happened == Expected.REACHES_THE_MODEL) {
            return expected == Expected.ADDRESSED_TO_THE_SYSTEM
                    ? "AN INJECTION REACHED THE MODEL"
                    : "paid for a call on text with nothing in it";
        }
        return "the wrong guardrail stopped it";
    }

    private static String shortened(String text) {
        return text.length() > 56 ? text.substring(0, 53) + "..." : text;
    }
}
