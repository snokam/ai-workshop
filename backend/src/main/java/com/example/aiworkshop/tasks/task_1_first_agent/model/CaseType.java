package com.example.aiworkshop.tasks.task_1_first_agent.model;

import java.util.List;
import java.util.stream.Collectors;

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

    public String label() {
        return label;
    }

    public String description() {
        return description;
    }

    public List<String> requiredDocuments() {
        return requiredDocuments;
    }

    public static String catalog() {
        return java.util.Arrays.stream(values())
                .map(type -> "- %s (%s): %s".formatted(type.name(), type.label, type.description))
                .collect(Collectors.joining("\n"));
    }
}
