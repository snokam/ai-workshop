package com.example.aiworkshop.cases;

import java.time.Year;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;

/**
 * Opens a Case from what a Claimant typed. The counterpart to {@code DocumentIntake} on the Case
 * side: it hands the description to the classifier, turns the chosen {@link CaseType} into a Case
 * with that type's Required Documents, and stores it.
 *
 * <p>Thin on purpose. The one decision that needs a model — which kind of case this is — is the
 * classifier's; everything else here is bookkeeping: mint an identifier, copy the checklist off the
 * type, save. A Case created this way is indistinguishable afterwards from a seeded one, which is
 * why nothing downstream has to know a type was ever involved.
 */
@Service
public class CaseIntake {

    private final CaseTypeClassifier classifier;
    private final CaseStore cases;

    /**
     * Every Case is opened by a Claimant now, so the numbering starts from scratch. A plain counter
     * is enough for a single-process POC; persistence is where a real sequence would live.
     */
    private final AtomicInteger nextReference = new AtomicInteger(1001);

    CaseIntake(CaseTypeClassifier classifier, CaseStore cases) {
        this.classifier = classifier;
        this.cases = cases;
    }

    public CreatedCase open(String description) {
        CaseTypeSuggestion suggestion = classifier.classify(CaseType.catalog(), description);
        CaseType type = suggestion.type();

        int number = nextReference.getAndIncrement();
        String id = String.valueOf(number);
        String reference = "CASE-%d-%d".formatted(Year.now().getValue(), number);
        List<String> requiredDocuments = type.requiredDocuments();

        Case theCase = new Case(id, reference, type, requiredDocuments);
        cases.save(theCase);

        return new CreatedCase(
                id,
                reference,
                type.label(),
                suggestion.confidence(),
                suggestion.rationale(),
                requiredDocuments,
                theCase.status(List.of()));
    }
}
