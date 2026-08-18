package com.example.aiworkshop.cases.interview;

import com.example.aiworkshop.cases.model.Case;
import com.example.aiworkshop.cases.model.CreatedCase;
import com.example.aiworkshop.cases.store.CaseStore;
import com.example.aiworkshop.documents.model.MatchConfidence;
import java.time.Year;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;

/**
 * Opens a Case from a scenario the interview settled on. The interview's counterpart to task 1's
 * {@code CaseIntake}, kept separate on purpose: it writes into the same shared {@link CaseStore} but
 * mints its own identifiers, so the interview adds a way to open a Case without touching the one-shot
 * path or its counter.
 *
 * <p>The number range starts high so the two independent counters do not overlap within a run — good
 * enough for an in-memory POC, where a real sequence would live in the store instead.
 */
@Service
public class InterviewCaseOpener {

    private final CaseStore cases;

    private final AtomicInteger nextReference = new AtomicInteger(9001);

    InterviewCaseOpener(CaseStore cases) {
        this.cases = cases;
    }

    /** Turns a chosen {@link CaseScenario} into a stored Case carrying that scenario's checklist. */
    public CreatedCase open(CaseScenario scenario, MatchConfidence confidence, String rationale) {
        int number = nextReference.getAndIncrement();
        String id = String.valueOf(number);
        String reference = "CASE-%d-%d".formatted(Year.now().getValue(), number);
        List<String> requiredDocuments = scenario.requiredDocuments();

        Case theCase = new Case(id, reference, scenario.caseType(), requiredDocuments);
        cases.save(theCase);

        return new CreatedCase(
                id,
                reference,
                scenario.caseType().label(),
                confidence,
                rationale,
                requiredDocuments,
                theCase.status(List.of()));
    }
}
