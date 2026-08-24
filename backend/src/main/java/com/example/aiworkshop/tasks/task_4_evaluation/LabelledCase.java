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
        // TODO — task 4. Label the descriptions you would argue about. One version:
        //
        // return List.of(
        //         new LabelledCase(
        //                 "Someone reversed into my parked car outside the shop and I paid for the repair myself.",
        //                 CaseType.MOTOR,
        //                 "Unambiguous. If this one is wrong, something is broken rather than debatable."),
        //         new LabelledCase(
        //                 "My suitcase never turned up after my flight home and I had to buy clothes and toiletries.",
        //                 CaseType.TRAVEL,
        //                 "Unambiguous."),
        //         new LabelledCase(
        //                 "There was a break-in at my flat last week and my laptop and camera are gone.",
        //                 CaseType.HOME_CONTENTS,
        //                 "Unambiguous."),
        //         new LabelledCase(
        //                 "I have been signed off work for four months with a back injury and my pay has stopped.",
        //                 CaseType.DISABILITY,
        //                 "Unambiguous."),
        //         new LabelledCase(
        //                 "I need to see a specialist about my knee and the waiting list is nine months.",
        //                 CaseType.HEALTH_TREATMENT,
        //                 "Unambiguous."),
        //         new LabelledCase(
        //                 "My laptop was stolen from my car while it was parked at the airport.",
        //                 CaseType.HOME_CONTENTS,
        //                 "Contents or motor? Nothing happened to the car. Argue about it before you accept"
        //                         + " whatever the model said."),
        //         new LabelledCase(
        //                 "I broke my ankle on holiday in Spain and paid a private clinic to treat it.",
        //                 CaseType.TRAVEL,
        //                 "Travel or health treatment? Both are true. The label here is a policy decision, not a"
        //                         + " fact about the sentence."),
        //         new LabelledCase(
        //                 "Water came through the ceiling from the flat upstairs and ruined the carpet and a sofa.",
        //                 CaseType.HOME_CONTENTS,
        //                 "Contents, though someone will argue it is the building's problem and not a contents"
        //                         + " claim at all."),
        //         new LabelledCase(
        //                 "My neighbour keeps parking across my drive and I want to make a complaint.",
        //                 null,
        //                 "Nothing on the list covers it, and there is no longer a case type that catches"
        //                         + " everything else — so the honest answer is no type at all. A model that"
        //                         + " answers MOTOR here is matching words rather than reading."),
        //         new LabelledCase(
        //                 "hi",
        //                 null,
        //                 "Nothing to go on. This one should never reach the classifier at all — task 2's"
        //                         + " guardrail refuses it first — so if it does, something upstream is off."));
        //

        return List.of();
    }

    // ── To set this task again ────────────────────────────────────────────────────────
    // TODO — task 4. Write the cases you would argue about.
    //
    // The ten above are a worked example and the easy half of them proves nothing. Add your own
    // until the set contains descriptions you genuinely cannot call, then run
    // ClassifierEvaluation and sort every disagreement into two piles: the model was wrong, or the
    // label was an opinion. Only the first is a bug.
    //
    // Worth trying: a description in Norwegian; two claims in one sentence; something written
    // angrily; something so short it says nothing; a description that names one category outright
    // and is plainly about another.
}
