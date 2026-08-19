package com.example.aiworkshop.cases.interview;

import com.example.aiworkshop.tasks.task_7_create_case_chat.model.CaseScenario;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * The taxonomy the interviewer is shown and opens cases from. Rendered from the enum, so these check
 * the two never drift: every scenario reaches the prompt, and every scenario maps to a real type.
 */
class CaseScenarioTest {

    @Test
    void catalogueNamesEveryScenario() {
        String catalogue = CaseScenario.catalog();

        for (CaseScenario scenario : CaseScenario.values()) {
            assertThat(catalogue)
                    .describedAs("every scenario should be shown to the agent, including %s", scenario.name())
                    .contains(scenario.name());
        }
    }

    @Test
    void everyScenarioOpensAsARealCaseType() {
        for (CaseScenario scenario : CaseScenario.values()) {
            assertThat(scenario.caseType())
                    .describedAs("%s must map to a CaseType a handler recognises", scenario.name())
                    .isNotNull();
        }
    }
}
