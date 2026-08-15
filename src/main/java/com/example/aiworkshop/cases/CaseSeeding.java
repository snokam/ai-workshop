package com.example.aiworkshop.cases;

import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Puts a few Cases in the store at startup, so the handler screen is not empty in front of an
 * audience.
 *
 * <p>They start with no Documents attached, on purpose. A seeded Document would need a seeded
 * analysis, and invented model output is the one thing worth not faking at a workshop about models —
 * a Case that is genuinely empty is honest, and filling it takes one drag-and-drop on stage.
 *
 * <p>The Required Documents are written the way a person would write them, because that is exactly
 * what the intake agent is asked to match free text against (ADR 0001). Three Cases, deliberately
 * different: one waiting on everything, one waiting on a single document, one that needs only a
 * form.
 */
@Component
class CaseSeeding implements CommandLineRunner {

    private final CaseStore cases;

    CaseSeeding(CaseStore cases) {
        this.cases = cases;
    }

    @Override
    public void run(String... args) {
        cases.save(new Case(
                "1001",
                "CASE-2026-1001",
                List.of("proof of identity", "receipt for the repair", "photo of the damage")));
        cases.save(new Case(
                "1002", "CASE-2026-1002", List.of("medical certificate", "employer's statement of absence")));
        cases.save(new Case("1003", "CASE-2026-1003", List.of("completed claim form")));
    }
}
