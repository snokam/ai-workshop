package com.example.aiworkshop.tasks.task_1_first_agent;

import com.example.aiworkshop.tasks.task_1_first_agent.model.Case;
import com.example.aiworkshop.tasks.task_1_first_agent.model.CaseStatus;
import java.util.List;

/**
 * How far along a case is.
 *
 * <p>This is a seam, and it is the reason the case list works before task 2 is written. Task 1 can
 * ask the question but cannot answer it: a case moves forward when documents arrive, and reading
 * documents is the next task. So task 1 declares the question here and answers it the only way it
 * honestly can — nothing has arrived — and task 2 replaces the answer by publishing a better bean.
 *
 * <p>The same shape appears twice more: task 3 hands its guardrails to task 2 as beans, and task 4
 * screens uploads by listening for task 2's event. In every case the earlier task defines the seam
 * and the later one fills it, which is what keeps the chain pointing one way.
 */
public interface CaseProgress {
    CaseStatus statusOf(Case theCase);

    List<String> outstandingFor(Case theCase);

    /** What task 1 can say on its own: nothing has arrived yet, so everything is still outstanding. */
    class NothingHasArrivedYet implements CaseProgress {
        @Override
        public CaseStatus statusOf(Case theCase) {
            return CaseStatus.AWAITING_DOCUMENTS;
        }

        @Override
        public List<String> outstandingFor(Case theCase) {
            return theCase.requiredDocuments();
        }
    }
}
