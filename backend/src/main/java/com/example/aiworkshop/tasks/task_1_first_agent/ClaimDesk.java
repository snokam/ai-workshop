package com.example.aiworkshop.tasks.task_1_first_agent;

import com.example.aiworkshop.tasks.task_1_first_agent.model.Claim;
import com.example.aiworkshop.tasks.task_1_first_agent.model.ClaimOverview;
import com.example.aiworkshop.tasks.task_1_first_agent.store.ClaimStore;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * The claims, listed.
 *
 * <p>This desk only knows about claims. It asks {@link ClaimProgress} how far along each one is
 * rather than working it out, so it never has to look at a document — which is what lets the list
 * page work with task 1 alone, and lets task 2 improve the answer without touching this file.
 */
@Service
public class ClaimDesk {
    private final ClaimStore claims;
    private final ClaimProgress progress;

    public ClaimDesk(ClaimStore claims, ClaimProgress progress) {
        this.claims = claims;
        this.progress = progress;
    }

    public List<ClaimOverview> list() {
        return claims.findAll().stream().map(this::overviewOf).toList();
    }

    public Claim require(String claimId) {
        return claims.findById(claimId).orElseThrow(() -> new UnknownClaimException(claimId));
    }

    public ClaimOverview overviewOf(Claim theClaim) {
        return new ClaimOverview(
                theClaim.id(),
                theClaim.reference(),
                theClaim.type().label(),
                progress.statusOf(theClaim),
                theClaim.requiredDocuments(),
                progress.outstandingFor(theClaim));
    }

    public static class UnknownClaimException extends RuntimeException {
        public UnknownClaimException(String claimId) {
            super("No such claim: " + claimId);
        }
    }

    public static class UnknownProposalException extends RuntimeException {
        public UnknownProposalException(String proposalId) {
            super("No such proposal: " + proposalId);
        }
    }

    public static class UnknownDocumentException extends RuntimeException {
        public UnknownDocumentException(String filename) {
            super("No document called '" + filename + "' is attached to this claim.");
        }
    }
}
