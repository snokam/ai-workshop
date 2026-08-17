package com.example.aiworkshop.cases;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The kinds of Case a Claimant can open, grounded in Storebrand's real products. Each carries the
 * Required Documents that kind of Case needs, so choosing a type is the same act as writing its
 * checklist — the classifier picks the type, and {@link #requiredDocuments()} is what the Case is
 * then created with.
 *
 * <p>A deliberate departure from the note in {@code CONTEXT.md} that Cases do not come in kinds: for
 * this POC the kinds are fixed and named here rather than left as free text (ADR 0003). {@link
 * #OTHER} is the escape hatch the classifier reaches for when nothing else fits, and is the only
 * type allowed to carry no Required Documents.
 *
 * <p>The {@code description} is written for the classifier, not the screen: it is what the agent
 * reads to tell one type from another, so it names the situations that land in each.
 */
public enum CaseType {
    TRAVEL(
            "Travel insurance claim",
            "Something went wrong on a trip: lost or delayed baggage, a cancelled or interrupted"
                    + " journey, or illness or injury while travelling.",
            List.of(
                    "travel booking confirmation",
                    "receipts for the affected items",
                    "carrier's confirmation of the delay or cancellation")),

    HOME_CONTENTS(
            "Home contents insurance claim",
            "Belongings at home were lost or damaged: theft or burglary, or water, fire or similar"
                    + " damage to the contents of a home.",
            List.of(
                    "police report",
                    "receipts or proof of ownership for the affected items",
                    "photos of the damage")),

    DISABILITY(
            "Disability insurance claim",
            "A claim for loss of income after long-term illness or injury has reduced the ability to"
                    + " work.",
            List.of(
                    "medical certificate",
                    "NAV decision on disability benefit or work assessment allowance",
                    "documentation of income")),

    HEALTH_TREATMENT(
            "Health treatment insurance claim",
            "A request to use health or treatment insurance to access private treatment, a"
                    + " specialist, or hospital care.",
            List.of("referral from a doctor", "medical certificate")),

    MOTOR(
            "Motor insurance claim",
            "Damage to, a collision involving, or theft of a car or other motor vehicle.",
            List.of("completed claim form", "photos of the damage", "police report")),

    OTHER(
            "General enquiry",
            "None of the specific case types fit what the person is describing.",
            List.of());

    private final String label;
    private final String description;
    private final List<String> requiredDocuments;

    CaseType(String label, String description, List<String> requiredDocuments) {
        this.label = label;
        this.description = description;
        this.requiredDocuments = requiredDocuments;
    }

    /** The human-readable name shown to a Claimant once the type is chosen. */
    public String label() {
        return label;
    }

    /** What the classifier reads to tell this type apart from the others. */
    public String description() {
        return description;
    }

    /** The Required Documents a Case of this type is created with. Empty only for {@link #OTHER}. */
    public List<String> requiredDocuments() {
        return requiredDocuments;
    }

    /**
     * The whole set rendered for the classifier's prompt: one line per type, name and description.
     * Rendered from the enum so adding a type here updates what the agent is shown, rather than a
     * second list drifting out of step with this one.
     */
    public static String catalog() {
        return java.util.Arrays.stream(values())
                .map(type -> "- %s (%s): %s".formatted(type.name(), type.label, type.description))
                .collect(Collectors.joining("\n"));
    }
}
