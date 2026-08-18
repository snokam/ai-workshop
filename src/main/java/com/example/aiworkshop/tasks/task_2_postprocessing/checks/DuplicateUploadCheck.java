package com.example.aiworkshop.tasks.task_2_postprocessing.checks;

import com.example.aiworkshop.tasks.task_2_postprocessing.FraudScreener.Upload;
import com.example.aiworkshop.tasks.task_2_postprocessing.model.FraudScreening.Indicator;
import com.example.aiworkshop.tasks.task_2_postprocessing.model.FraudScreening.Kind;
import com.example.aiworkshop.tasks.task_2_postprocessing.model.FraudScreening.Weight;
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
        List<String> earlier = seenBefore.computeIfAbsent(upload.contentHash(), key -> new CopyOnWriteArrayList<>());

        List<Indicator> found = earlier.isEmpty() ? List.of() : List.of(indicatorFor(upload, earlier));
        earlier.add(upload.caseId() + " / " + upload.filename());
        return found;
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
