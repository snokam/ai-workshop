package com.example.aiworkshop.cases;

import java.time.Year;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;
import com.example.aiworkshop.tasks.task_1_first_agent.CaseTypeClassifier;

@Service
public class CaseIntake {
    private final CaseTypeClassifier classifier;
    private final CaseStore cases;

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
