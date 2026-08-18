package com.example.aiworkshop.tasks.task_2_postprocessing;

import com.example.aiworkshop.tasks.task_2_postprocessing.model.FraudScreening;
import com.example.aiworkshop.tasks.task_2_postprocessing.model.FraudScreening.Indicator;
import com.example.aiworkshop.document.DocumentAnalysis;
import com.example.aiworkshop.tasks.task_2_postprocessing.checks.FraudCheck;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FraudScreener {

    private static final Logger log = LoggerFactory.getLogger(FraudScreener.class);

    private final List<FraudCheck> checks;
    private final Map<String, FraudScreening> screenings = new ConcurrentHashMap<>();

    public FraudScreener(List<FraudCheck> checks) {
        this.checks = checks;
    }

    public FraudScreening screen(Upload upload) {
        List<Indicator> found = new ArrayList<>();
        for (FraudCheck check : checks) {
            try {
                found.addAll(check.screen(upload));
            } catch (RuntimeException e) {
                log.warn(
                        "Check {} failed on {}; the document is stored either way",
                        check.getClass().getSimpleName(),
                        upload.filename(),
                        e);
            }
        }
        found.sort(Comparator.comparing(Indicator::weight).reversed());

        FraudScreening screening = new FraudScreening(upload.documentId(), List.copyOf(found));
        screenings.put(upload.documentId(), screening);
        log.info("Screened {} with {} checks: {} indicator(s)", upload.filename(), checks.size(), found.size());
        return screening;
    }

    public List<FraudScreening> findAllFor(List<String> documentIds) {
        return documentIds.stream()
                .map(screenings::get)
                .filter(Objects::nonNull)
                .filter(FraudScreening::foundSomething)
                .toList();
    }

    public record Upload(
            String documentId,
            String caseId,
            String filename,
            String contentType,
            byte[] content,
            String contentHash,
            DocumentAnalysis analysis) {

        public boolean isImage() {
            return contentType.startsWith("image/");
        }

        public boolean isJpeg() {
            return contentType.equals("image/jpeg");
        }
    }
}
