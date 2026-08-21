package com.example.aiworkshop.tasks.task_4_fraud_detection.checks;

import com.example.aiworkshop.workshop.TaskNotImplementedException;
import com.example.aiworkshop.workshop.WorkshopTask;

import com.example.aiworkshop.tasks.task_2_document_agent.model.ExtractedField;
import com.example.aiworkshop.tasks.task_4_fraud_detection.FraudScreener.Upload;
import com.example.aiworkshop.tasks.task_4_fraud_detection.model.FraudScreening.Indicator;
import com.example.aiworkshop.tasks.task_4_fraud_detection.model.FraudScreening.Kind;
import com.example.aiworkshop.tasks.task_4_fraud_detection.model.FraudScreening.Weight;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class FiguresCheck implements FraudCheck {

    private static final BigDecimal TOLERANCE = new BigDecimal("0.05");

    @Override
    public List<Indicator> screen(Upload upload) {
        // if (upload.analysis() == null || upload.analysis().fields() == null) {
        //     return List.of();
        // }
        //
        // List<ExtractedField> amounts = new ArrayList<>();
        // ExtractedField total = null;
        // for (ExtractedField field : upload.analysis().fields()) {
        //     if (!looksLikeMoney(field) || amountIn(field).isEmpty()) {
        //         continue;
        //     }
        //     if (looksLikeATotal(field.name())) {
        //         total = field;
        //     } else {
        //         amounts.add(field);
        //     }
        // }
        //
        // if (total == null || amounts.size() < 2) {
        //     return List.of();
        // }
        //
        // BigDecimal stated = amountIn(total).orElseThrow();
        // BigDecimal summed = amounts.stream()
        //         .map(field -> amountIn(field).orElseThrow())
        //         .reduce(BigDecimal.ZERO, BigDecimal::add);
        //
        // if (stated.subtract(summed).abs().compareTo(TOLERANCE) <= 0) {
        //     return List.of();
        // }
        //
        // return List.of(new Indicator(
        //         Kind.FIGURES_DISAGREE,
        //         Weight.CONCERN,
        //         "The document states a total of %s, and the %d other amounts on it add up to %s."
        //                 .formatted(stated.toPlainString(), amounts.size(), summed.toPlainString()),
        //         amounts.stream()
        //                 .map(field -> field.name() + ": " + field.value())
        //                 .toList()));
        //

        throw new TaskNotImplementedException(WorkshopTask.FRAUD_DETECTION);

        }

    /**
     * Whether this field is money at all.
     *
     * <p>A receipt is covered in numbers that are not amounts — an organisation number, a receipt
     * number, a date, a phone number — and summing those produces a confident accusation about a
     * document nobody has looked at. So an amount has to say it is one: a currency, or two decimal
     * places. Anything that names itself an identifier is out regardless.
     */
    private static boolean looksLikeMoney(ExtractedField field) {
        String name = field.name().toLowerCase();
        for (String identifier : List.of("nr", "no.", "number", "nummer", "dato", "date", "tlf", "phone")) {
            if (name.contains(identifier)) {
                return false;
            }
        }
        String value = field.value() == null ? "" : field.value().toLowerCase();
        boolean saysCurrency = value.contains("kr") || value.contains("nok") || value.contains("€")
                || value.contains("$") || name.contains("beløp") || name.contains("amount")
                || name.contains("total") || name.contains("sum") || name.contains("pris");
        boolean hasØre = value.matches(".*[.,]\\d{2}\\b.*");
        return saysCurrency || hasØre;
    }

    private static boolean looksLikeATotal(String name) {
        String lower = name.toLowerCase();
        return lower.contains("total") || lower.contains("sum") || lower.contains("å betale");
    }

    private static Optional<BigDecimal> amountIn(ExtractedField field) {
        String digits = field.value() == null
                ? ""
                : field.value().replaceAll("[^0-9,.-]", "").replace(",", ".").trim();
        if (digits.isEmpty() || digits.chars().noneMatch(Character::isDigit)) {
            return Optional.empty();
        }
        try {
            return Optional.of(new BigDecimal(digits));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    // ── To set this task again ────────────────────────────────────────────────────────
    // TODO — task 4, part 2. Write a check from nothing.
    //
    // Delete this whole file and write it again. There is no registration to do: implement
    // FraudCheck, annotate the class @Component, and FraudScreener picks it up — adding a check is
    // adding a class and nothing else, which is the only structure in this task worth having.
    //
    // This one is different from the other three. They read the bytes or the file's metadata; this
    // reads what the *agent* extracted, so the model's answer becomes the input to code that cannot
    // be talked round. upload.analysis().fields() is a list of name/value pairs in the document's
    // own wording — which means no fixed schema, values as they were printed, and a Norwegian
    // receipt writing 1 234,50 where you expected 1234.50.
    //
    // Decide what to do about that, and about the case where nothing parses at all. A check that
    // throws is caught, logged and skipped, so the wrong answer here is a confident one.
}
