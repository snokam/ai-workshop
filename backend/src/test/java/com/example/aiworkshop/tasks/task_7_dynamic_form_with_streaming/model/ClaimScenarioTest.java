package com.example.aiworkshop.tasks.task_7_dynamic_form_with_streaming.model;

import com.example.aiworkshop.tasks.task_1_first_agent.model.ClaimType;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * The taxonomy the interviewer is shown and opens claims from. Rendered from the enum, so these check
 * the two never drift: every scenario reaches the prompt, and every scenario maps to a real type.
 */
class ClaimScenarioTest {

    @Test
    void catalogueNamesEveryScenario() {
        String catalogue = ClaimScenario.catalog();

        for (ClaimScenario scenario : ClaimScenario.values()) {
            assertThat(catalogue)
                    .describedAs("every scenario should be shown to the agent, including %s", scenario.name())
                    .contains(scenario.name());
        }
    }

    @Test
    void everyScenarioOpensAsARealClaimType() {
        for (ClaimScenario scenario : ClaimScenario.values()) {
            assertThat(scenario.claimType())
                    .describedAs("%s must map to a ClaimType a handler recognises", scenario.name())
                    .isNotNull();
        }
    }
}
