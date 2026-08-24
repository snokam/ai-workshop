package com.example.aiworkshop.tasks.task_4_evaluation;

import com.example.aiworkshop.tasks.task_1_first_agent.model.CaseType;
import java.util.List;

/**
 * One description, and the case type a person thinks it should open.
 *
 * <p>The set below is deliberately not a set of easy ones. Half of it is here because reasonable
 * people disagree about it, and those are the rows worth your time — a suite of unambiguous
 * examples tells you the model can do the job you already knew it could do.
 */
public record LabelledCase(String description, CaseType expected, String why) {

    public static List<LabelledCase> all() {
        // TODO — task 4, part 1. Label the cases you would argue about.
        //
        // Return a List.of(new LabelledCase(description, expectedType, why), ...).
        //
        // Ten is plenty. Do not write ten easy ones: half the value is in the rows reasonable people
        // disagree about — a laptop stolen from a car, an injury on holiday treated privately — because
        // those are what tell you whether a disagreement is the model being wrong or the label being an
        // opinion.
        //
        // expectedType may be null. There is no case type for "something else" any more, so "my neighbour
        // parks across my drive" has no right answer among the five, and that is a legitimate label.
        //
        // The why is for the person reading the disagreements later, which is probably you in twenty minutes.

        return List.of();
    }

}
