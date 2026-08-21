package com.example.aiworkshop.tasks.task_8_evaluation;

import com.example.aiworkshop.tasks.task_1_first_agent.agent.CaseTypeClassifier;
import com.example.aiworkshop.tasks.task_1_first_agent.agent.VertexAiProperties;
import com.example.aiworkshop.tasks.task_1_first_agent.model.CaseType;
import com.example.aiworkshop.tasks.task_1_first_agent.model.CaseTypeSuggestion;
import com.example.aiworkshop.tasks.task_2_document_agent.DocumentIntake;
import com.example.aiworkshop.tasks.task_2_document_agent.agent.DocumentAnalyzer;
import com.example.aiworkshop.tasks.task_2_document_agent.model.DocumentAnalysis;
import com.example.aiworkshop.tasks.task_2_document_agent.store.DocumentFiles;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.vertexai.gemini.VertexAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Task 8, the portability question: will this workshop run on a model that is not the default?
 *
 * <pre>./mvnw test -Dtest=ModelComparison -Dsurefire.failIfNoSpecifiedTests=false</pre>
 *
 * <p>Every other evaluation here holds the model still and asks how good the answer is. This one
 * holds the task still and changes the model, because the failure it is looking for is different in
 * kind. A model that classifies a little worse costs you an argument about labels. A model that
 * cannot return an answer in the shape it was asked for costs you the exercise, and you find out in
 * front of the room.
 *
 * <p>Three probes, one per thing the workshop cannot do without:
 *
 * <ul>
 *   <li><b>structured output</b> — the classifier answers into a record. Task 1 onwards.
 *   <li><b>a file it will read</b> — an image sent as inline data. Task 2 onwards.
 *   <li><b>a tool it will call</b> — not merely can, but does, unprompted. Task 6.
 * </ul>
 *
 * <p>A model missing any of the three is not a slower workshop, it is a different one.
 */
@SpringBootTest
@Disabled("builds every candidate model and calls each three times — run it deliberately")
class ModelComparison {

    /** The same settings the application runs on, including how it finds the project. */
    @Autowired
    private VertexAiProperties vertex;

    @Test
    void everyCandidateModelIsCheckedAgainstWhatTheWorkshopNeeds() {
        System.out.printf("%n%-24s %-12s %-12s %-12s %-9s %s%n",
                "model", "structured", "reads file", "calls tool", "seconds", "verdict");
        System.out.println("-".repeat(104));

        for (CandidateModel candidate : CandidateModel.all()) {
            long started = System.nanoTime();
            ChatModel model;
            try {
                model = build(candidate);
            } catch (Exception unavailable) {
                System.out.printf("%-24s %s%n", candidate.label(), "unavailable — " + firstLine(unavailable));
                continue;
            }
            if (model == null) {
                System.out.printf(
                        "%-24s %s%n",
                        candidate.label(),
                        "skipped — set ANTHROPIC_API_KEY to include it (see anthropicOrNull)");
                continue;
            }

            Probe structured = probe(() -> {
                CaseTypeSuggestion answer = AiServices.create(CaseTypeClassifier.class, model)
                        .classify(CaseType.catalog(), "Someone reversed into my parked car outside the shop.");
                return answer.type() != null && answer.confidence() != null;
            });
            Probe readsFile = probe(() -> {
                DocumentAnalysis analysis = AiServices.create(DocumentAnalyzer.class, model)
                        .analyse(aReceiptImage(), List.of("receipt for the repair"));
                return analysis.category() != null && !analysis.category().isBlank();
            });
            Probe callsTool = probe(() -> {
                Clock clock = new Clock();
                String reply = AiServices.builder(AsksTheTime.class)
                        .chatModel(model)
                        .tools(clock)
                        .build()
                        .ask("What is on the workshop clock right now? Use the tool.");
                return clock.wasCalled && reply != null;
            });

            double seconds = (System.nanoTime() - started) / 1_000_000_000.0;
            boolean usable = structured.passed && readsFile.passed && callsTool.passed;
            System.out.printf(
                    "%-24s %-12s %-12s %-12s %-9.1f %s%n",
                    candidate.label(),
                    structured,
                    readsFile,
                    callsTool,
                    seconds,
                    usable ? "the workshop runs on this" : "NOT USABLE — see below");

            for (Probe p : List.of(structured, readsFile, callsTool)) {
                if (!p.passed && p.why != null) {
                    System.out.printf("      %s%n", p.why);
                }
            }
        }

        System.out.println(
                """

                A model that fails "structured" cannot do task 1, so nothing after it runs either.
                "reads file" is task 2 onwards, "calls tool" is task 6.

                Timing is one call each and proves nothing about a room of twenty people, but a model
                three times slower than the default turns a sixty-minute task into something else.

                Add a model to CandidateModel.all() and run this again. That is the whole of what it
                takes to answer "can we use X instead" without finding out on the day.
                """);
    }

    // ── the three probes ─────────────────────────────────────────────────────────────────────

    private record Probe(boolean passed, String why) {
        @Override
        public String toString() {
            return passed ? "yes" : "NO";
        }
    }

    private interface Check {
        boolean run() throws Exception;
    }

    private static Probe probe(Check check) {
        try {
            return new Probe(check.run(), null);
        } catch (Exception e) {
            return new Probe(false, firstLine(e));
        }
    }

    /** A tool with a reason to be called: the model cannot know this without asking. */
    static class Clock {
        boolean wasCalled;

        @Tool("The current time on the workshop clock, which is not the wall clock.")
        String workshopTime() {
            wasCalled = true;
            return "14:05, during task 6";
        }
    }

    interface AsksTheTime {
        @SystemMessage("Answer using the tools you have. Do not guess a time.")
        String ask(@UserMessage String question);
    }

    // ── building each candidate ──────────────────────────────────────────────────────────────

    private ChatModel build(CandidateModel candidate) {
        return switch (candidate.provider()) {
            case VERTEX -> VertexAiGeminiChatModel.builder()
                    .project(vertex.projectId())
                    .location(vertex.location())
                    .modelName(candidate.modelName())
                    .temperature(0.2f)
                    .maxOutputTokens(16384)
                    .maxRetries(1)
                    .build();
            case ANTHROPIC -> anthropicOrNull(candidate);
        };
    }

    /**
     * Claude, through the Anthropic API.
     *
     * <p>Not through Vertex, which is where the rest of this runs and which does serve Claude — the
     * check for that returns 200 from
     * {@code .../publishers/anthropic/models/claude-sonnet-4-5:rawPredict}. LangChain4j's Anthropic
     * client posts to {@code {baseUrl}/messages}, and no base URL produces a path ending in
     * {@code :rawPredict}, so the two do not meet. Reaching Vertex-hosted Claude would mean writing
     * a ChatModel against rawPredict, which is a bigger thing than this comparison.
     *
     * <p>So this needs ANTHROPIC_API_KEY, and says so rather than failing obscurely when it is
     * missing.
     */
    private static ChatModel anthropicOrNull(CandidateModel candidate) {
        String key = System.getenv("ANTHROPIC_API_KEY");
        if (key == null || key.isBlank()) {
            return null;
        }
        return AnthropicChatModel.builder()
                .apiKey(key)
                .modelName(candidate.modelName())
                .maxTokens(16384)
                .temperature(0.2)
                .timeout(Duration.ofSeconds(180))
                .maxRetries(1)
                .build();
    }

    private static List<Content> aReceiptImage() throws Exception {
        BufferedImage image = new BufferedImage(900, 300, BufferedImage.TYPE_INT_RGB);
        Graphics2D page = image.createGraphics();
        page.setColor(Color.WHITE);
        page.fillRect(0, 0, image.getWidth(), image.getHeight());
        page.setColor(Color.BLACK);
        page.setFont(new Font("Helvetica", Font.BOLD, 24));
        page.drawString("NORDIC BIL & SERVICE AS", 40, 60);
        page.setFont(new Font("Helvetica", Font.PLAIN, 20));
        int line = 110;
        for (String row : List.of(
                "Receipt 40219                    2026-07-14",
                "Replace front bumper                8 400,00",
                "Total NOK                          20 468,75")) {
            page.drawString(row, 40, line);
            line += 34;
        }
        page.dispose();

        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(image, "png", png);
        return List.of(
                TextContent.from(DocumentIntake.INTAKE_INSTRUCTION),
                DocumentFiles.contentOf(png.toByteArray(), "image/png"));
    }

    private static String firstLine(Throwable e) {
        String message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        return message.split("\n")[0].substring(0, Math.min(96, message.split("\n")[0].length()));
    }
}
