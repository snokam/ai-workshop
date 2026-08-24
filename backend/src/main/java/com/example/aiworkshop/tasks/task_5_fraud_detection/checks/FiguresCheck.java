package com.example.aiworkshop.tasks.task_5_fraud_detection.checks;

import com.example.aiworkshop.workshop.WorkshopTask;
import com.example.aiworkshop.workshop.TaskNotImplementedException;
import com.example.aiworkshop.tasks.task_3_document_agent.model.ExtractedField;
import com.example.aiworkshop.tasks.task_5_fraud_detection.FraudScreener.Upload;
import com.example.aiworkshop.tasks.task_5_fraud_detection.model.FraudScreening.Indicator;
import com.example.aiworkshop.tasks.task_5_fraud_detection.model.FraudScreening.Kind;
import com.example.aiworkshop.tasks.task_5_fraud_detection.model.FraudScreening.Weight;
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
        // TODO — task 5, part 4. A check written from nothing.
        //
        // No scaffolding for this one. upload.analysis().fields() is a List<ExtractedField>, each a name and
        // a value as they appeared in the document. Decide what "the figures do not add up" means and write
        // it.
        //
        // A reasonable first pass: find the fields that look like money, find the one that looks like a
        // total, and compare the sum of the rest against it.
        //
        // Expect false positives before you expect fraud. An early version of this summed an organisation
        // number because it had digits and spaces, and reported a receipt as inconsistent by 912 345 678.
        // Decide what makes a value money — a currency token, two decimals — and what disqualifies a name.

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

}
