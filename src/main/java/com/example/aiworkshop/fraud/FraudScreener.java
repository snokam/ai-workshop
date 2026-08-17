package com.example.aiworkshop.fraud;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Runs every {@link FraudCheck} over one upload and records what they found.
 *
 * <p>Called from intake, where the bytes still exist. It is the only place a Screening is made and
 * the only place one is stored.
 *
 * <p>The whole class is built around one rule: <b>screening cannot cost anyone their upload</b>. A
 * check that throws is logged and skipped; the other checks still run; the Document is stored
 * either way. The worst outcome available here is an empty Screening, which is also the ordinary
 * outcome for an ordinary document — so a handler cannot read "nothing found" as proof the checks
 * ran. That is what the log line is for.
 */
@Service
public class FraudScreener {

    private static final Logger log = LoggerFactory.getLogger(FraudScreener.class);

    private final List<FraudCheck> checks;
    private final FraudScreeningStore store;

    public FraudScreener(List<FraudCheck> checks, FraudScreeningStore store) {
        this.checks = checks;
        this.store = store;
    }

    /** Screens one file and keeps the result against the Document id. Never throws. */
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
