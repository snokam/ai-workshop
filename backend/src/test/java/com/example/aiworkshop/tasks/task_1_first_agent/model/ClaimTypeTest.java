package com.example.aiworkshop.tasks.task_1_first_agent.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class ClaimTypeTest {
    @Test
    void everyTypeHasALabelAndADescription() {
        for (ClaimType type : ClaimType.values()) {
            assertThat(type.label()).as("label of %s", type).isNotBlank();
            assertThat(type.description()).as("description of %s", type).isNotBlank();
        }
    }

    /**
     * There is no longer a type that catches everything else, so there is no longer a type allowed an
     * empty checklist. Every claim this insurer opens is a claim it knows what to ask for.
     */
    @Test
    void everyTypeCarriesAChecklist() {
        Arrays.stream(ClaimType.values())
                .forEach(type -> assertThat(type.requiredDocuments())
                        .as("required documents of %s", type)
                        .isNotEmpty());
    }

    @Test
    void theCatalogueNamesEveryType() {
        String catalog = ClaimType.catalog();
        for (ClaimType type : ClaimType.values()) {
            assertThat(catalog).contains(type.name()).contains(type.label());
        }
    }
}
