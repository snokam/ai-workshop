package com.example.aiworkshop.cases;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

/**
 * The Case Types are hardcoded data, not model output — so this pins their shape without Spring or a
 * model. The classifier is only ever allowed to return one of these, so every one has to be a Case
 * worth opening: a label to show, and a checklist to create it with. {@code OTHER} is the one
 * exception, the fallback with no checklist.
 */
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

    /** The escape hatch: a Case can be opened with no checklist, but only for OTHER. */
    @Test
    void onlyOtherIsAllowedAnEmptyChecklist() {
        assertThat(CaseType.OTHER.requiredDocuments()).isEmpty();
    }

    /** The catalogue is rendered from the enum, so a type not appearing in it could never be chosen. */
    @Test
    void theCatalogueNamesEveryType() {
        String catalog = CaseType.catalog();
        for (CaseType type : CaseType.values()) {
            assertThat(catalog).contains(type.name()).contains(type.label());
        }
    }
}
