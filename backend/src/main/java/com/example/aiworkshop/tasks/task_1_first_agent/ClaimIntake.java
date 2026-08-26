package com.example.aiworkshop.tasks.task_1_first_agent;

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
        ClaimTypeSuggestion suggestion = classifier.classify(ClaimType.catalog(), description);
        if (suggestion.type() == null) {
            throw new NothingWeCoverException(suggestion.rationale());
        }
        ClaimType type = suggestion.type();

        int number = nextReference.getAndIncrement();
        String id = String.valueOf(number);
        String reference = "CLAIM-%d-%d".formatted(Year.now().getValue(), number);
        List<String> requiredDocuments = type.requiredDocuments();

        Claim theClaim = new Claim(id, reference, type, requiredDocuments);
        claims.save(theClaim);

        return new CreatedClaim(
                id,
                reference,
                type.label(),
                suggestion.confidence(),
                suggestion.rationale(),
                requiredDocuments,
                ClaimStatus.AWAITING_DOCUMENTS);
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
