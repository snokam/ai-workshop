package com.example.aiworkshop.tasks.task_1_first_agent;

import com.example.aiworkshop.tasks.task_1_first_agent.model.Case;
import com.example.aiworkshop.tasks.task_1_first_agent.model.CaseOverview;
import com.example.aiworkshop.tasks.task_1_first_agent.store.CaseStore;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * The cases, listed.
 *
 * <p>This desk only knows about cases. It asks {@link CaseProgress} how far along each one is
 * rather than working it out, so it never has to look at a document — which is what lets the list
 * page work with task 1 alone, and lets task 2 improve the answer without touching this file.
 */
@Service
public class CaseDesk {
    private final CaseStore cases;
    private final CaseProgress progress;

    public CaseDesk(CaseStore cases, CaseProgress progress) {
        this.cases = cases;
        this.progress = progress;
    }

    public List<CaseOverview> list() {
        return cases.findAll().stream().map(this::overviewOf).toList();
    }

    public Case require(String caseId) {
        return cases.findById(caseId).orElseThrow(() -> new UnknownCaseException(caseId));
    }

    public CaseOverview overviewOf(Case theCase) {
        return new CaseOverview(
                theCase.id(),
                theCase.reference(),
                theCase.type().label(),
                progress.statusOf(theCase),
                theCase.requiredDocuments(),
                progress.outstandingFor(theCase));
    }

    public static class UnknownCaseException extends RuntimeException {
        public UnknownCaseException(String caseId) {
            super("No such case: " + caseId);
        }
    }

    public static class UnknownProposalException extends RuntimeException {
        public UnknownProposalException(String proposalId) {
            super("No such proposal: " + proposalId);
        }
    }

    public static class UnknownDocumentException extends RuntimeException {
        public UnknownDocumentException(String filename) {
            super("No document called '" + filename + "' is attached to this case.");
        }
    }
}
