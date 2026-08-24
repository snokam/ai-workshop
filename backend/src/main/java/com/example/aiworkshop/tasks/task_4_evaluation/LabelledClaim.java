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
public record LabelledClaim(String description, ClaimType expected, String why) {

    /** What the evaluation runs over: the worked examples, then yours. */
    public static List<LabelledClaim> all() {
        return Stream.concat(examples().stream(), yours().stream()).toList();
    }

    public static List<LabelledClaim> yours() {
        // TODO — task 4, part 1. Add the claims you would argue about.
        //
        // Return a List.of(new LabelledClaim(description, expectedType, why), ...). Seven or eight on top
        // of the three examples is plenty.
        //
        // Do not write eight easy ones. Half the value is in rows reasonable people disagree about —
        // a laptop stolen from a car, an injury on holiday treated privately, a phone dropped in a hotel
        // pool — because those are what tell you whether a disagreement is the model being wrong or the
        // label being an opinion.
        //
        // expectedType may be null. There is no claim type for "something else", so "my neighbour parks
        // across my drive" has no right answer among the five, and that is a legitimate label.
        //
        // The why is read by whoever looks at the disagreements later, which is you in twenty minutes.
        // Write down why you chose it, not what you chose.

        return List.of();
    }

    private static List<LabelledClaim> examples() {
        return List.of(
                new LabelledClaim(
                        "Someone reversed into my parked car outside the office and drove off.",
                        ClaimType.MOTOR,
                        "Plain. A row like this tells you the wiring works, and almost nothing else."),
                new LabelledClaim(
                        "My suitcase never arrived and I had to buy clothes for the week.",
                        ClaimType.TRAVEL,
                        "Arguable, and that is the point. It happened on a trip, so TRAVEL — but the loss is"
                                + " belongings, and a reader who thinks in terms of what was lost says"
                                + " HOME_CONTENTS. Decide which you meant before you call the model wrong."),
                new LabelledClaim(
                        "My neighbour keeps parking across my driveway and I want to know my options.",
                        null,
                        "None of the five fit. Expecting null is a real label: it asks whether the agent"
                                + " will admit that nothing matches, or force the nearest one to make the"
                                + " question go away."));
    }
}
