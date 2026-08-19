package com.example.aiworkshop.tasks.task_1_first_agent.model;

import com.example.aiworkshop.tasks.task_1_first_agent.model.CaseType;
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

    @Test
    void everySpecificTypeCarriesAChecklist() {
        Arrays.stream(CaseType.values())
                .filter(type -> type != CaseType.OTHER)
                .forEach(type -> assertThat(type.requiredDocuments())
                        .as("required documents of %s", type)
                        .isNotEmpty());
    }

    @Test
    void onlyOtherIsAllowedAnEmptyChecklist() {
        assertThat(CaseType.OTHER.requiredDocuments()).isEmpty();
    }

    @Test
    void theCatalogueNamesEveryType() {
        String catalog = CaseType.catalog();
        for (CaseType type : CaseType.values()) {
            assertThat(catalog).contains(type.name()).contains(type.label());
        }
    }
}
