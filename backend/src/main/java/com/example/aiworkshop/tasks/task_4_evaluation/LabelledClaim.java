package com.example.aiworkshop.tasks.task_4_evaluation;

import com.example.aiworkshop.tasks.task_1_first_agent.model.ClaimType;
import java.util.List;
import java.util.stream.Stream;

/**
 * One description, and the claim type a person thinks it should open.
 *
 * <p>The rules to label against are the classifier's own, from task 1: it picks exactly one of the
 * five claim types, or nothing at all when none of them fit, and says how sure it is.
 *
 * <p>Three rows are written below to show the shape. They are also the three kinds of row worth
 * having — one plain, one where reasonable people disagree, and one with no right answer among the
 * five — because a suite of unambiguous examples only tells you the model can do the job you already
 * knew it could do.
 */
public record LabelledClaim(String description, ClaimType expected) {

    /** What the evaluation runs over: the worked examples, then yours. */
    public static List<LabelledClaim> all() {
        return Stream.concat(examples().stream(), yours().stream()).toList();
    }

    public static List<LabelledClaim> yours() {
        return List.of(
                new LabelledClaim(
                        "My laptop was stolen out of the boot of my car while it was parked at the station.",
                        ClaimType.HOME_CONTENTS),
                new LabelledClaim(
                        "I broke my ankle on holiday in Spain and paid a private clinic to set it.",
                        ClaimType.TRAVEL),
                new LabelledClaim(
                        "I have been signed off work for eight months and I do not know what I am entitled to.",
                        ClaimType.DISABILITY),
                new LabelledClaim(
                        "vann i kjelleren etter styrtregn i natt",
                        ClaimType.HOME_CONTENTS),
                new LabelledClaim(
                        "The dentist says I need a crown and I want to know if it is covered before I book.",
                        ClaimType.HEALTH_TREATMENT),
                new LabelledClaim(
                        "Someone keyed the whole side of my van outside the depot last night.",
                        ClaimType.MOTOR),
                new LabelledClaim(
                        "I want to complain about how long my last claim took and who handled it.",
                        null),
                new LabelledClaim(
                        "My phone went into the hotel pool on the second day and has not turned on since.",
                        ClaimType.TRAVEL));
    }

    private static List<LabelledClaim> examples() {
        return List.of(
                // Plain. A row like this tells you the wiring works, and almost nothing else.
                new LabelledClaim(
                        "Someone reversed into my parked car outside the office and drove off.",
                        ClaimType.MOTOR),

                // Arguable, and that is the point. It happened on a trip, so TRAVEL — but the loss is
                // belongings, and a reader who thinks about what was lost says HOME_CONTENTS. Decide
                // which you meant before you call the model wrong.
                new LabelledClaim(
                        "My suitcase never arrived and I had to buy clothes for the week.",
                        ClaimType.TRAVEL),

                // None of the five fit. Expecting null is a real label: it asks whether the agent will
                // admit that nothing matches, or force the nearest one to make the question go away.
                new LabelledClaim(
                        "My neighbour keeps parking across my driveway and I want to know my options.",
                        null));
    }
}
