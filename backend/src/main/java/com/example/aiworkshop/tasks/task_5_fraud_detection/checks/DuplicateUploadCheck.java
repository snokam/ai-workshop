package com.example.aiworkshop.tasks.task_5_fraud_detection.checks;

import com.example.aiworkshop.workshop.WorkshopTask;
import com.example.aiworkshop.workshop.TaskNotImplementedException;
import com.example.aiworkshop.tasks.task_5_fraud_detection.FraudScreener.Upload;
import com.example.aiworkshop.tasks.task_5_fraud_detection.model.FraudScreening.Indicator;
import com.example.aiworkshop.tasks.task_5_fraud_detection.model.FraudScreening.Kind;
import com.example.aiworkshop.tasks.task_5_fraud_detection.model.FraudScreening.Weight;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;

@Component
public class DuplicateUploadCheck implements FraudCheck {

    private final Map<String, List<String>> seenBefore = new ConcurrentHashMap<>();

    @Override
    public List<Indicator> screen(Upload upload) {
        // TODO — task 5, part 1. The same bytes, seen before.
        //
        // Return the indicators for this upload, or List.of() when there is nothing to say.
        //
        //   upload.contentHash()  the file's fingerprint, already computed at intake
        //   upload.caseId()       which case it arrived on
        //   upload.filename()
        //
        // Keep what you have seen across calls — a Map from hash to where it was seen is enough — and flag
        // anything that arrives twice.
        //
        // Weigh it. The same file twice on one case is someone double-clicking: Weight.NOTE. The same file
        // on a different case is something else entirely: Weight.STRONG. Kind.ALREADY_UPLOADED for both.

        throw new TaskNotImplementedException(WorkshopTask.FRAUD_DETECTION);
    }

    private static Indicator indicatorFor(Upload upload, List<String> earlier) {
        boolean anotherCase = earlier.stream().anyMatch(seen -> !seen.startsWith(upload.caseId() + " / "));
        return new Indicator(
                Kind.ALREADY_UPLOADED,
                anotherCase ? Weight.STRONG : Weight.NOTE,
                anotherCase
                        ? "The same file, byte for byte, has already been uploaded to a different case."
                        : "The same file has already been uploaded to this case, and was not read again.",
                List.copyOf(earlier));
    }
}
