package com.example.aiworkshop.tasks.task_8_create_case_chat.model;

import com.example.aiworkshop.tasks.task_1_first_agent.model.CaseType;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A finer-grained situation within a {@link CaseType}, and the one thing the intake interview exists
 * to pin down. Where {@link CaseType} answers "what kind of insurance", a scenario answers "what
 * actually happened" — and it is the scenario, not the type, that owns the precise checklist. A
 * cancelled trip and a stolen bag are both TRAVEL, but they need different documents, so they are
 * different scenarios here.
 *
 * <p>This is the taxonomy the interviewer (task 7) chooses from, and {@link #requiredDocuments()} is
 * what a Case opened through the interview is created with. The one-shot classifier of task 1 never
 * sees it, so the two intakes stay independent.
 *
 * <p>Types that are not (yet) worth splitting carry a single general scenario whose documents mirror
 * the {@link CaseType}'s own list, so every type is still reachable through the interview.
 */
public enum CaseScenario {

    // --- Travel: the one type where the situation genuinely changes what is needed ---------------
    TRAVEL_CANCELLATION(
            CaseType.TRAVEL,
            "Cancelled trip",
            "The trip was cancelled before departure — illness, a death in the family, or another"
                    + " reason it could not go ahead.",
            List.of(
                    "travel booking confirmation",
                    "cancellation confirmation from the travel operator",
                    "medical certificate")),

    TRAVEL_BAGGAGE(
            CaseType.TRAVEL,
            "Lost, delayed or damaged baggage",
            "Baggage did not arrive, arrived late, or arrived damaged.",
            List.of(
                    "carrier's baggage report (PIR)",
                    "receipts for the affected items",
                    "travel booking confirmation")),

    TRAVEL_ILLNESS(
            CaseType.TRAVEL,
            "Illness or injury on the trip",
            "The person fell ill or was injured while travelling and needed treatment.",
            List.of(
                    "medical certificate from the treating doctor",
                    "receipts for medical expenses",
                    "travel booking confirmation")),

    TRAVEL_THEFT(
            CaseType.TRAVEL,
            "Theft or robbery on the trip",
            "Belongings were stolen while travelling.",
            List.of(
                    "police report from the country it happened in",
                    "receipts or proof of ownership for the stolen items",
                    "travel booking confirmation")),

    // --- The rest: one general scenario each, mirroring the type's own checklist for now ---------
    HOME_CONTENTS_GENERAL(
            CaseType.HOME_CONTENTS,
            "Home contents claim",
            "Belongings at home were lost or damaged: theft, or water, fire or similar damage.",
            CaseType.HOME_CONTENTS.requiredDocuments()),

    DISABILITY_GENERAL(
            CaseType.DISABILITY,
            "Disability claim",
            "A claim for loss of income after long-term illness or injury reduced the ability to work.",
            CaseType.DISABILITY.requiredDocuments()),

    HEALTH_TREATMENT_GENERAL(
            CaseType.HEALTH_TREATMENT,
            "Health treatment claim",
            "A request to use health or treatment insurance to reach private treatment.",
            CaseType.HEALTH_TREATMENT.requiredDocuments()),

    MOTOR_GENERAL(
            CaseType.MOTOR,
            "Motor claim",
            "Damage to, a collision involving, or theft of a car or other motor vehicle.",
            CaseType.MOTOR.requiredDocuments()),

    OTHER(
            CaseType.OTHER,
            "Something else",
            "None of the specific scenarios fit what the person is describing.",
            List.of());

    private final CaseType caseType;
    private final String label;
    private final String description;
    private final List<String> requiredDocuments;

    CaseScenario(CaseType caseType, String label, String description, List<String> requiredDocuments) {
        this.caseType = caseType;
        this.label = label;
        this.description = description;
        this.requiredDocuments = requiredDocuments;
    }

    /** The {@link CaseType} a Case of this scenario is opened as — its label is what handlers see. */
    public CaseType caseType() {
        return caseType;
    }

    /** The human-readable name of the scenario. */
    public String label() {
        return label;
    }

    /** What the interviewer reads to tell this scenario apart from its siblings. */
    public String description() {
        return description;
    }

    /** The Required Documents a Case opened as this scenario is created with. */
    public List<String> requiredDocuments() {
        return requiredDocuments;
    }

    /**
     * The whole taxonomy rendered for the interviewer's prompt: scenarios grouped under the kind of
     * insurance they belong to. Rendered from the enum so adding a scenario updates what the agent is
     * shown, rather than a second list drifting out of step with this one.
     */
    public static String catalog() {
        Map<CaseType, List<CaseScenario>> byType =
                java.util.Arrays.stream(values())
                        .collect(Collectors.groupingBy(CaseScenario::caseType, LinkedHashMap::new, Collectors.toList()));

        StringBuilder rendered = new StringBuilder();
        byType.forEach((type, scenarios) -> {
            rendered.append(type.label()).append('\n');
            for (CaseScenario scenario : scenarios) {
                rendered.append("  - %s (%s): %s%n".formatted(scenario.name(), scenario.label, scenario.description));
            }
        });
        return rendered.toString().strip();
    }
}
