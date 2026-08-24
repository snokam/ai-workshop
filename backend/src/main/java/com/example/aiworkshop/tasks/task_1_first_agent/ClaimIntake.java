package com.example.aiworkshop.tasks.task_1_first_agent;

import com.example.aiworkshop.workshop.WorkshopTask;
import com.example.aiworkshop.workshop.TaskNotImplementedException;
import com.example.aiworkshop.tasks.task_1_first_agent.agent.ClaimTypeClassifier;
import com.example.aiworkshop.tasks.task_1_first_agent.store.ClaimStore;
import com.example.aiworkshop.tasks.task_1_first_agent.model.CreatedClaim;
import com.example.aiworkshop.tasks.task_1_first_agent.model.ClaimTypeSuggestion;
import com.example.aiworkshop.tasks.task_1_first_agent.model.ClaimType;
import com.example.aiworkshop.tasks.task_1_first_agent.model.Claim;
import com.example.aiworkshop.tasks.task_1_first_agent.model.ClaimStatus;
import java.time.Year;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;

@Service
public class ClaimIntake {
    private final ClaimTypeClassifier classifier;
    private final ClaimStore claims;

    private final AtomicInteger nextReference = new AtomicInteger(1001);

    ClaimIntake(ClaimTypeClassifier classifier, ClaimStore claims) {
        this.classifier = classifier;
        this.claims = claims;
    }

    public CreatedClaim open(String description) {
        // TODO — task 1, part 3. Turn the answer into a claim.
        //
        // Steps:
        //
        //   1. ClaimTypeSuggestion suggestion = classifier.classify(ClaimType.catalog(), description)
        //   2. if suggestion.type() is null, throw new NothingWeCoverException(suggestion.rationale())
        //      — the controller turns that into a 422 the claimant reads
        //   3. take a number from nextReference.getAndIncrement() for the id and the reference
        //      (the format elsewhere is CASE-<year>-<number>, and Year.now().getValue() gives the year)
        //   4. type.requiredDocuments() is the checklist that comes with the type
        //   5. new Claim(id, reference, type, requiredDocuments), then claims.save(theClaim)
        //   6. return a CreatedClaim — read the record for the order of its components; the status of a
        //      brand-new claim is ClaimStatus.AWAITING_DOCUMENTS, since nothing has arrived yet
        //
        // Step 4 is where the model's answer stops being a suggestion and becomes the shape of someone's
        // claim, which is the whole point of the task.

        throw new TaskNotImplementedException(WorkshopTask.FIRST_AGENT);
    }

    /**
     * The description was clear enough to read and describes nothing this insurer covers.
     *
     * <p>There used to be a claim type called "General enquiry" that caught these, which meant every
     * unhelpful answer became a claim somebody had to close, and the claimant was told nothing. It is
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
