package com.example.aiworkshop.tasks.task_7_dynamic_form_with_streaming.model;

import com.example.aiworkshop.tasks.task_1_first_agent.model.ClaimType;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A finer-grained situation within a {@link ClaimType}, and the one thing the intake interview exists
 * to pin down. Where {@link ClaimType} answers "what kind of insurance", a scenario answers "what
 * actually happened" — and it is the scenario, not the type, that owns the precise checklist. A
 * cancelled trip and a stolen bag are both TRAVEL, but they need different documents, so they are
 * different scenarios here.
 *
 * <p>This is the taxonomy the interviewer (task 7) chooses from, and {@link #requiredDocuments()} is
 * what a Claim opened through the interview is created with. The one-shot classifier of task 1 never
 * sees it, so the two intakes stay independent.
 *
 * <p>Types that are not (yet) worth splitting carry a single general scenario whose documents mirror
 * the {@link ClaimType}'s own list, so every type is still reachable through the interview.
 */
public enum ClaimScenario {

    // --- Travel: the one type where the situation genuinely changes what is needed ---------------
    TRAVEL_CANCELLATION(
            ClaimType.TRAVEL,
            "Cancelled trip",
            "The trip was cancelled before departure — illness, a death in the family, or another"
                    + " reason it could not go ahead.",
            List.of(
                    "travel booking confirmation",
                    "cancellation confirmation from the travel operator",
                    "medical certificate"),
            List.of(
                    "the travel dates",
                    "why the trip could not go ahead",
                    "roughly what the trip cost")),

    TRAVEL_BAGGAGE(
            ClaimType.TRAVEL,
            "Lost, delayed or damaged baggage",
            "Baggage did not arrive, arrived late, or arrived damaged.",
            List.of(
                    "carrier's baggage report (PIR)",
                    "receipts for the affected items",
                    "travel booking confirmation"),
            List.of(
                    "the flight number",
                    "what was in the bag",
                    "roughly what those things were worth")),

    TRAVEL_ILLNESS(
            ClaimType.TRAVEL,
            "Illness or injury on the trip",
            "The person fell ill or was injured while travelling and needed treatment.",
            List.of(
                    "medical certificate from the treating doctor",
                    "receipts for medical expenses",
                    "travel booking confirmation"),
            List.of(
                    "where they were treated",
                    "what the treatment was for",
                    "roughly what it cost")),

    TRAVEL_THEFT(
            ClaimType.TRAVEL,
            "Theft or robbery on the trip",
            "Belongings were stolen while travelling.",
            List.of(
                    "police report from the country it happened in",
                    "receipts or proof of ownership for the stolen items",
                    "travel booking confirmation"),
            List.of(
                    "where it happened",
                    "what was taken",
                    "roughly what those things were worth")),

    // --- The rest: one general scenario each, mirroring the type's own checklist for now ---------
    HOME_CONTENTS_THEFT(
            ClaimType.HOME_CONTENTS,
            "Theft or break-in at home",
            "Belongings were stolen from the home, or the home was broken into.",
            List.of(
                    "police report",
                    "receipts or proof of ownership for the stolen items",
                    "photos of the damage caused getting in"),
            List.of(
                    "how they got in",
                    "what was taken",
                    "roughly what those things were worth")),

    HOME_CONTENTS_WATER(
            ClaimType.HOME_CONTENTS,
            "Water damage at home",
            "A leak, a burst pipe or an overflowing appliance damaged the home or what was in it.",
            List.of(
                    "plumber's report or repair invoice",
                    "photos of the damage",
                    "receipts or proof of ownership for the affected items"),
            List.of(
                    "where the water came from",
                    "which rooms were affected",
                    "roughly what the damage is worth")),

    HOME_CONTENTS_FIRE(
            ClaimType.HOME_CONTENTS,
            "Fire or smoke damage at home",
            "Fire, smoke or soot damaged the home or what was in it.",
            List.of(
                    "fire service report",
                    "photos of the damage",
                    "receipts or proof of ownership for the affected items"),
            List.of(
                    "where the fire started",
                    "which rooms were affected",
                    "roughly what the damage is worth")),

    DISABILITY_GENERAL(
            ClaimType.DISABILITY,
            "Disability claim",
            "A claim for loss of income after long-term illness or injury reduced the ability to work.",
            ClaimType.DISABILITY.requiredDocuments(),
            List.of(
                    "when the condition started",
                    "what work they can no longer do")),

    HEALTH_TREATMENT_GENERAL(
            ClaimType.HEALTH_TREATMENT,
            "Health treatment claim",
            "A request to use health or treatment insurance to reach private treatment.",
            ClaimType.HEALTH_TREATMENT.requiredDocuments(),
            List.of(
                    "what treatment is needed",
                    "which hospital or clinic",
                    "roughly what it costs")),

    MOTOR_GENERAL(
            ClaimType.MOTOR,
            "Motor claim",
            "Damage to, a collision involving, or theft of a car or other motor vehicle.",
            ClaimType.MOTOR.requiredDocuments(),
            List.of(
                    "where the vehicle was",
                    "the registration number",
                    "roughly what the repair costs"));

    private final ClaimType claimType;
    private final String label;
    private final String description;
    private final List<String> requiredDocuments;
    private final List<String> factsWorthHaving;

    ClaimScenario(
            ClaimType claimType,
            String label,
            String description,
            List<String> requiredDocuments,
            List<String> factsWorthHaving) {
        this.claimType = claimType;
        this.label = label;
        this.description = description;
        this.requiredDocuments = requiredDocuments;
        this.factsWorthHaving = factsWorthHaving;
    }

    /** The {@link ClaimType} a Claim of this scenario is opened as — its label is what handlers see. */
    public ClaimType claimType() {
        return claimType;
    }

    /** The human-readable name of the scenario. */
    public String label() {
        return label;
    }

    /** What the interviewer reads to tell this scenario apart from its siblings. */
    public String description() {
        return description;
    }

    /**
     * Facts a person can type, as opposed to documents they have to go and find.
     *
     * <p>Separate from {@link #requiredDocuments()} on purpose. The helper in task 7 reads what
     * somebody is writing and may name one thing still worth adding — and with only a document list
     * to draw on, it asked for a police report: collected on the next screen, and not something
     * anybody can type into a box.
     */
    public List<String> factsWorthHaving() {
        return factsWorthHaving;
    }

    /** The Required Documents a Claim opened as this scenario is created with. */
    public List<String> requiredDocuments() {
        return requiredDocuments;
    }

    /**
     * The whole taxonomy rendered for the interviewer's prompt: scenarios grouped under the kind of
     * insurance they belong to, each with what it will be asked for. The documents are here because an
 * agent that knows a situation is a baggage claim but not what a baggage claim needs can only say so
 * vaguely — it is the difference between "this looks like a baggage claim" and "you will be asked for
 * the carrier's baggage report".
 *
 * <p>Rendered from the enum so adding a scenario updates what the agent is
     * shown, rather than a second list drifting out of step with this one.
     */
    public static String catalog() {
        Map<ClaimType, List<ClaimScenario>> byType =
                java.util.Arrays.stream(values())
                        .collect(Collectors.groupingBy(ClaimScenario::claimType, LinkedHashMap::new, Collectors.toList()));

        StringBuilder rendered = new StringBuilder();
        byType.forEach((type, scenarios) -> {
            rendered.append(type.label()).append('\n');
            for (ClaimScenario scenario : scenarios) {
                rendered.append("  - %s (%s): %s%n".formatted(scenario.name(), scenario.label, scenario.description));
                rendered.append("      worth asking for: %s%n".formatted(String.join(", ", scenario.factsWorthHaving)));
                rendered.append("      documents, later: %s%n".formatted(String.join(", ", scenario.requiredDocuments)));
            }
        });
        return rendered.toString().strip();
    }
}
