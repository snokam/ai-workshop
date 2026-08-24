package com.example.aiworkshop.tasks.task_7_create_claim_chat;

import com.example.aiworkshop.tasks.task_7_create_claim_chat.model.ClaimScenario;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.aiworkshop.tasks.task_1_first_agent.model.CreatedClaim;
import com.example.aiworkshop.tasks.task_1_first_agent.store.ClaimStore;
import com.example.aiworkshop.tasks.task_1_first_agent.model.MatchConfidence;
import org.junit.jupiter.api.Test;

/**
 * What opening a claim from a settled scenario does — no model involved. The opener runs only after
 * the agent has decided, so this is the part of task 7 that is plumbing and can be pinned down
 * exactly: the checklist comes from the scenario, and the reference is minted clear of task 1's.
 */
class InterviewClaimOpenerTest {

    @Test
    void opensAClaimCarryingTheScenariosChecklist() {
        ClaimStore claims = new ClaimStore();
        InterviewClaimOpener opener = new InterviewClaimOpener(claims);

        CreatedClaim created = opener.open(ClaimScenario.TRAVEL_BAGGAGE, MatchConfidence.HIGH, "Bag never arrived.");

        assertThat(created.typeLabel()).isEqualTo(ClaimScenario.TRAVEL_BAGGAGE.claimType().label());
        assertThat(created.requiredDocuments())
                .describedAs("the claim is opened with the scenario's documents, not the whole type's")
                .isEqualTo(ClaimScenario.TRAVEL_BAGGAGE.requiredDocuments());
        assertThat(created.confidence()).isEqualTo(MatchConfidence.HIGH);
        assertThat(created.rationale()).isEqualTo("Bag never arrived.");
        assertThat(claims.findById(created.id()))
                .describedAs("opening a claim saves it in the shared store handlers read from")
                .isPresent();
    }

    @Test
    void mintsReferencesInItsOwnRangeSoTheTwoIntakesNeverCollide() {
        InterviewClaimOpener opener = new InterviewClaimOpener(new ClaimStore());

        CreatedClaim created = opener.open(ClaimScenario.MOTOR_GENERAL, MatchConfidence.LOW, "A dent in the door.");

        assertThat(Integer.parseInt(created.id()))
                .describedAs("the interview counts from 9001, clear of task 1's range that starts at 1001")
                .isGreaterThanOrEqualTo(9001);
        assertThat(created.reference()).startsWith("CASE-");
    }
}
