package com.example.aiworkshop.tasks.task_5_claim_summary_choosing_models;

import com.example.aiworkshop.tasks.task_5_claim_summary_choosing_models.evaluation.SummaryJudge;
import com.example.aiworkshop.tasks.task_5_claim_summary_choosing_models.evaluation.SummaryRubric;
import com.example.aiworkshop.tasks.task_1_first_agent.model.ClaimType;
import com.example.aiworkshop.tasks.task_3_document_agent.model.ExtractedField;
import com.example.aiworkshop.tasks.task_3_document_agent.model.QualityAssessment.Quality;
import com.example.aiworkshop.tasks.task_5_claim_summary_choosing_models.agent.SummaryConfig;
import com.example.aiworkshop.tasks.task_5_claim_summary_choosing_models.ModelPrices;
import com.example.aiworkshop.tasks.task_5_claim_summary_choosing_models.agent.ClaimSummarizer;
import com.example.aiworkshop.workshop.TaskNotImplementedException;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.Result;
import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.service.AiServices;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Scoring prose, where there is nothing to compare against — task 5's own evaluation, and a different
 * shape from the two in task 4.
 *
 * <pre>cd backend && ./mvnw test -Pevaluate</pre>
 *
 * <p>Nobody can write down the correct summary of a claim, so the two techniques before this one are
 * both unavailable. What is left is to write down what a good summary must be true of, and ask each
 * question separately — which is {@link SummaryRubric}, and which is the only part of this you
 * could have done without a model.
 *
 * <p>Then something has to answer the questions. A person can, and for four questions about one
 * summary a person is better. This runs a model at it instead, because at a hundred summaries a
 * person will not, and the interesting thing to see is exactly how much you would be trusting.
 */
@SpringBootTest
@Tag("evaluation")
class SummaryEvaluation {

    @Autowired
    private ClaimSummarizer summarizer;

    @Autowired
    private ChatModel chatModel;

    @Test
    void scoreTheSummary() {
        List<DocumentForSummary> documents = twoDocumentsOnAClaim();
        Result<String> summarised;
        long start = System.nanoTime();
        try {
            summarised = summarizer.summarise(ClaimType.MOTOR.label(), documents);
        } catch (TaskNotImplementedException notYet) {
            System.out.println("\nThere is nothing to judge yet — task 5's summariser is not written.\n");
            return;
        }
        long millis = (System.nanoTime() - start) / 1_000_000;
        String summary = summarised.content();
        printWhatItCost(millis, summarised);
        SummaryJudge judge = AiServices.create(SummaryJudge.class, chatModel);

        System.out.printf("%nThe summary being judged:%n%n%s%n%n", summary);
        System.out.println("-".repeat(100));

        int held = 0;
        for (SummaryRubric question : SummaryRubric.all()) {
            SummaryJudge.Verdict verdict = judge.judge(question.question(), documents.toString(), summary);
            held += verdict.holds() ? 1 : 0;

            System.out.printf("%n%s  %s%n", verdict.holds() ? "yes" : "NO ", question.question());
            System.out.printf("    quoting: %s%n", verdict.quote());
            System.out.printf("    because: %s%n", verdict.because());
            if (!verdict.holds()) {
                System.out.printf("    this matters because: %s%n", question.whyItMatters());
            }
        }

        System.out.printf("%n%d of %d held.%n%n", held, SummaryRubric.all().size());
        printWhatItCost(millis, summarised);
        System.out.println(
                """
                Now do the part that matters: read the summary yourself and answer the four questions
                by hand, before you look at what the judge said.

                Where you and it disagree is the finding. If it passed something you would not have
                shown a claim handler, you have just watched a model mark its own homework and give
                itself the benefit of the doubt — which is the ordinary outcome, and the reason a
                number out of this is not evidence on its own.

                Then ask what it could not have caught. The judge and the summariser are the same
                model. Anything they are both wrong about is invisible to this, no matter how many
                questions you add.
                """);
    }

    private static List<DocumentForSummary> twoDocumentsOnAClaim() {
        return List.of(
                new DocumentForSummary(
                        "repair-receipt.pdf",
                        "repair receipt",
                        "A receipt from Nordic Bil & Service AS for repairs to a Volvo V60.",
                        List.of(
                                new ExtractedField("Date", "2026-07-14"),
                                new ExtractedField("Amount NOK", "20 468,75"),
                                new ExtractedField("Vehicle", "Volvo V60, reg. EL 84213")),
                        Quality.GOOD),
                new DocumentForSummary(
                        "damage.png",
                        "photograph of damage",
                        "A photograph of a car's front bumper, cracked across the near side.",
                        List.of(new ExtractedField("Shows", "front bumper, cracked")),
                        Quality.GOOD));
    }

    /**
     * What the choice in SummaryConfig actually cost, in tokens and in money.
     *
     * <p>Printed twice on purpose — once before the rubric and once after — because the two numbers
     * only mean something together. A model that halves the bill and fails two more questions has not
     * saved you anything.
     */
    private static void printWhatItCost(long millis, Result<String> result) {
        String model = SummaryConfig.READING_EVERY_DOCUMENT;
        TokenUsage usage = result.tokenUsage();
        double dollars = ModelPrices.dollarsFor(model, usage);

        System.out.printf("%n  %-24s %-24s %8s %8s %8s %12s%n", "job", "model", "time", "tokens", "unseen", "cost");
        System.out.printf(
                "  %-24s %-24s %7dms %8s %8d %12s%n",
                "reading every document",
                model,
                millis,
                usage == null || usage.totalTokenCount() == null ? "?" : usage.totalTokenCount(),
                ModelPrices.hiddenThinking(usage),
                dollars < 0 ? "no price" : "$%.6f".formatted(dollars));
        System.out.println("""

                  "unseen" is thinking: tokens the model spent that appear in neither the prompt nor
                  the answer. They are billed at the output rate, so on a reasoning model most of what
                  you pay for is text you never see. Published prices, checked 26 August 2026 — read
                  ModelPrices if a number looks wrong.
                """);
    }
}
