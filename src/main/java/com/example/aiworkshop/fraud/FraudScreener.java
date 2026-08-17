package com.example.aiworkshop.fraud;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FraudScreener {
    private static final Logger log = LoggerFactory.getLogger(FraudScreener.class);

    private final List<FraudCheck> checks;
    private final FraudScreeningStore store;

    public FraudScreener(List<FraudCheck> checks, FraudScreeningStore store) {
        this.checks = checks;
        this.store = store;
    }

    public FraudScreening screen(ScreenedFile file) {
        List<FraudIndicator> found = new ArrayList<>();
        for (FraudCheck check : checks) {
            try {
                found.addAll(check.screen(file));
            } catch (RuntimeException e) {
                log.warn("Fraud check {} failed on document {}; skipping it", check.name(), file.documentId(), e);
            }
        }
        found.sort(Comparator.comparing(FraudIndicator::weight).reversed());

        FraudScreening screening = new FraudScreening(file.documentId(), List.copyOf(found), Instant.now());
        store.save(screening);
        log.info(
                "Screened {} with {} checks: {}",
                file.filename(),
                checks.size(),
                found.isEmpty() ? "nothing found" : found.size() + " indicator(s), heaviest " + screening.heaviest());
        return screening;
    }
}
