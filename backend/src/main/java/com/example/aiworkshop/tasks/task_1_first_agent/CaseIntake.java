package com.example.aiworkshop.tasks.task_1_first_agent;

import com.example.aiworkshop.tasks.task_1_first_agent.agent.CaseTypeClassifier;
import com.example.aiworkshop.tasks.task_1_first_agent.store.CaseStore;
import com.example.aiworkshop.tasks.task_1_first_agent.model.CreatedCase;
import com.example.aiworkshop.tasks.task_1_first_agent.model.CaseTypeSuggestion;
import com.example.aiworkshop.tasks.task_1_first_agent.model.CaseType;
import com.example.aiworkshop.tasks.task_1_first_agent.model.Case;
import com.example.aiworkshop.tasks.task_1_first_agent.model.CaseStatus;
import java.time.Year;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;

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
        if (suggestion.type() == null) {
            throw new NothingWeCoverException(suggestion.rationale());
        }
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
                CaseStatus.AWAITING_DOCUMENTS);

        // ── To set this task again ────────────────────────────────────────────────────────
        // TODO — task 1, part 3. Turn the agent's answer into a Case.
        //
        // classifier.classify(CaseType.catalog(), description) is the call. What comes back is a
        // CaseTypeSuggestion: a type, a confidence and a sentence of reasoning.
        //
        // The type is what decides the checklist — CaseType.requiredDocuments() — so this is where
        // the model's answer stops being a suggestion and starts being the shape of someone's case.
        // Give it a reference, save it, and return the CreatedCase the screen shows.
        // throw new TaskNotImplementedException(WorkshopTask.FIRST_AGENT);
    }

    /**
     * The description was clear enough to read and describes nothing this insurer covers.
     *
     * <p>There used to be a case type called "General enquiry" that caught these, which meant every
     * unhelpful answer became a case somebody had to close, and the claimant was told nothing. It is
     * better to say so: the person can then take it somewhere that can help, which they cannot do
     * while a reference number is telling them it is in hand.
     */
    public static class NothingWeCoverException extends RuntimeException {
        public NothingWeCoverException(String rationale) {
            super(rationale == null || rationale.isBlank()
                    ? "We could not match this to any of the insurance we handle."
                    : rationale);
        }
    }
}
