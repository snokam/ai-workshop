package com.example.aiworkshop.tasks.task_7_create_claim_chat;

import com.example.aiworkshop.tasks.task_7_create_claim_chat.model.ClaimScenario;
import com.example.aiworkshop.tasks.task_1_first_agent.model.Claim;
import com.example.aiworkshop.tasks.task_1_first_agent.model.ClaimStatus;
import com.example.aiworkshop.tasks.task_1_first_agent.model.CreatedClaim;
import com.example.aiworkshop.tasks.task_1_first_agent.store.ClaimStore;
import com.example.aiworkshop.tasks.task_1_first_agent.model.MatchConfidence;
import java.time.Year;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;

/**
 * Opens a Claim from a scenario the interview settled on. The interview's counterpart to task 1's
 * {@code ClaimIntake}, kept separate on purpose: it writes into the same shared {@link ClaimStore} but
 * mints its own identifiers, so the interview adds a way to open a Claim without touching the one-shot
 * path or its counter.
 *
 * <p>The number range starts high so the two independent counters do not overlap within a run — good
 * enough for an in-memory POC, where a real sequence would live in the store instead.
 */
@Service
public class InterviewClaimOpener {

    private final ClaimStore claims;

    private final AtomicInteger nextReference = new AtomicInteger(9001);

    InterviewClaimOpener(ClaimStore claims) {
        this.claims = claims;
    }

    /** Turns a chosen {@link ClaimScenario} into a stored Claim carrying that scenario's checklist. */
    public CreatedClaim open(ClaimScenario scenario, MatchConfidence confidence, String rationale) {
        int number = nextReference.getAndIncrement();
        String id = String.valueOf(number);
        String reference = "CLAIM-%d-%d".formatted(Year.now().getValue(), number);
        List<String> requiredDocuments = scenario.requiredDocuments();

        Claim theClaim = new Claim(id, reference, scenario.claimType(), requiredDocuments);
        claims.save(theClaim);

        return new CreatedClaim(
                id,
                reference,
                scenario.claimType().label(),
                confidence,
                rationale,
                requiredDocuments,
                ClaimStatus.AWAITING_DOCUMENTS);
    }
}
