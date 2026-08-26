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
        // Ask the agent, then build a claim out of what it answers. Real names where you could not
        // guess them, the rest in your own words:
        //
        //   suggestion = classifier.classify(ClaimType.catalog(), description)
        //
        //   if the suggestion has no type:
        //       throw new NothingWeCoverException(suggestion.rationale())
        //
        //   type      = suggestion.type()
        //   number    = nextReference.getAndIncrement()
        //   id        = that number, as a String
        //   reference = "CLAIM-%d-%d".formatted(Year.now().getValue(), number)
        //   documents = type.requiredDocuments()
        //
        //   build a new Claim(id, reference, type, documents) and hand it to claims.save(...)
        //
        //   return a CreatedClaim of:
        //       id, reference, type.label(),
        //       the suggestion's confidence and rationale,
        //       documents,
        //       ClaimStatus.AWAITING_DOCUMENTS
        //
        // Two of those lines are worth more than the typing.
        //
        // The no-type branch is the agent being allowed to say "none of these".
        // NothingWeCoverException becomes a 422 the claimant reads, instead of a claim nobody can
        // ever settle.
        //
        // claims.save(...) is where the model's answer stops being a suggestion and becomes
        // someone's actual claim, with a reference number they will quote at you. That is the point
        // of the task.

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
