package com.example.aiworkshop.tasks.task_7_create_case_chat;

import com.example.aiworkshop.tasks.task_7_create_case_chat.InterviewCaseOpener;
import com.example.aiworkshop.tasks.task_7_create_case_chat.model.CaseScenario;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.aiworkshop.tasks.task_1_first_agent.model.CreatedCase;
import com.example.aiworkshop.tasks.task_1_first_agent.store.CaseStore;
import com.example.aiworkshop.tasks.task_1_first_agent.model.MatchConfidence;
import org.junit.jupiter.api.Test;

/**
 * What opening a case from a settled scenario does — no model involved. The opener runs only after
 * the agent has decided, so this is the part of task 7 that is plumbing and can be pinned down
 * exactly: the checklist comes from the scenario, and the reference is minted clear of task 1's.
 */
class InterviewCaseOpenerTest {

    @Test
    void opensACaseCarryingTheScenariosChecklist() {
        CaseStore cases = new CaseStore();
        InterviewCaseOpener opener = new InterviewCaseOpener(cases);

        CreatedCase created = opener.open(CaseScenario.TRAVEL_BAGGAGE, MatchConfidence.HIGH, "Bag never arrived.");

        assertThat(created.typeLabel()).isEqualTo(CaseScenario.TRAVEL_BAGGAGE.caseType().label());
        assertThat(created.requiredDocuments())
                .describedAs("the case is opened with the scenario's documents, not the whole type's")
                .isEqualTo(CaseScenario.TRAVEL_BAGGAGE.requiredDocuments());
        assertThat(created.confidence()).isEqualTo(MatchConfidence.HIGH);
        assertThat(created.rationale()).isEqualTo("Bag never arrived.");
        assertThat(cases.findById(created.id()))
                .describedAs("opening a case saves it in the shared store handlers read from")
                .isPresent();
    }

    @Test
    void mintsReferencesInItsOwnRangeSoTheTwoIntakesNeverCollide() {
        InterviewCaseOpener opener = new InterviewCaseOpener(new CaseStore());

        CreatedCase created = opener.open(CaseScenario.MOTOR_GENERAL, MatchConfidence.LOW, "A dent in the door.");

        assertThat(Integer.parseInt(created.id()))
                .describedAs("the interview counts from 9001, clear of task 1's range that starts at 1001")
                .isGreaterThanOrEqualTo(9001);
        assertThat(created.reference()).startsWith("CASE-");
    }
}
