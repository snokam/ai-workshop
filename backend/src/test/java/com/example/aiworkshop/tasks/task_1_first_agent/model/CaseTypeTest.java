package com.example.aiworkshop.tasks.task_1_first_agent.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class CaseTypeTest {
    @Test
    void everyTypeHasALabelAndADescription() {
        for (CaseType type : CaseType.values()) {
            assertThat(type.label()).as("label of %s", type).isNotBlank();
            assertThat(type.description()).as("description of %s", type).isNotBlank();
        }
    }

    /**
     * There is no longer a type that catches everything else, so there is no longer a type allowed an
     * empty checklist. Every case this insurer opens is a case it knows what to ask for.
     */
    @Test
    void everyTypeCarriesAChecklist() {
        Arrays.stream(CaseType.values())
                .forEach(type -> assertThat(type.requiredDocuments())
                        .as("required documents of %s", type)
                        .isNotEmpty());
    }

    @Test
    void theCatalogueNamesEveryType() {
        String catalog = CaseType.catalog();
        for (CaseType type : CaseType.values()) {
            assertThat(catalog).contains(type.name()).contains(type.label());
        }
    }
}
