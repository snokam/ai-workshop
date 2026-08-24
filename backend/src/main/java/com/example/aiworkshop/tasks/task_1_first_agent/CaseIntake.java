package com.example.aiworkshop.tasks.task_1_first_agent;

import com.example.aiworkshop.workshop.WorkshopTask;
import com.example.aiworkshop.workshop.TaskNotImplementedException;
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
        // TODO — task 1, part 3. Turn the answer into a case.
        //
        // Steps:
        //
        //   1. CaseTypeSuggestion suggestion = classifier.classify(CaseType.catalog(), description)
        //   2. if suggestion.type() is null, throw new NothingWeCoverException(suggestion.rationale())
        //      — the controller turns that into a 422 the claimant reads
        //   3. take a number from nextReference.getAndIncrement() for the id and the reference
        //      (the format elsewhere is CASE-<year>-<number>, and Year.now().getValue() gives the year)
        //   4. type.requiredDocuments() is the checklist that comes with the type
        //   5. new Case(id, reference, type, requiredDocuments), then cases.save(theCase)
        //   6. return a CreatedCase — read the record for the order of its components; the status of a
        //      brand-new case is CaseStatus.AWAITING_DOCUMENTS, since nothing has arrived yet
        //
        // Step 4 is where the model's answer stops being a suggestion and becomes the shape of someone's
        // case, which is the whole point of the task.

        throw new TaskNotImplementedException(WorkshopTask.FIRST_AGENT);
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
