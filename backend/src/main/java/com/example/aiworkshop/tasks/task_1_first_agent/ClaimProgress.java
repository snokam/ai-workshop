package com.example.aiworkshop.tasks.task_1_first_agent;

import com.example.aiworkshop.tasks.task_1_first_agent.model.Claim;
import com.example.aiworkshop.tasks.task_1_first_agent.model.ClaimStatus;
import java.util.List;

/**
 * How far along a claim is.
 *
 * <p>This is a seam, and it is the reason the claim list works before task 2 is written. Task 1 can
 * ask the question but cannot answer it: a claim moves forward when documents arrive, and reading
 * documents is the next task. So task 1 declares the question here and answers it the only way it
 * honestly can — nothing has arrived — and task 2 replaces the answer by publishing a better bean.
 *
 * <p>The same shape appears twice more: task 3 hands its guardrails to task 2 as beans, and task 4
 * screens uploads by listening for task 2's event. In every claim the earlier task defines the seam
 * and the later one fills it, which is what keeps the chain pointing one way.
 */
public interface ClaimProgress {
    ClaimStatus statusOf(Claim theClaim);

    List<String> outstandingFor(Claim theClaim);

    /** What task 1 can say on its own: nothing has arrived yet, so everything is still outstanding. */
    class NothingHasArrivedYet implements ClaimProgress {
        @Override
        public ClaimStatus statusOf(Claim theClaim) {
            return ClaimStatus.AWAITING_DOCUMENTS;
        }

        @Override
        public List<String> outstandingFor(Claim theClaim) {
            return theClaim.requiredDocuments();
        }
    }
}
