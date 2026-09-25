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
        return List.of(
                new LabelledClaim(
                        "My laptop was stolen out of the boot of my car while it was parked at the"
                                + " shopping centre. The side window was smashed.",
                        ClaimType.MOTOR,
                        "Two losses in one sentence and the labels split on which one you read first."
                                + " The smashed window is damage to the vehicle, so MOTOR — but the laptop"
                                + " is belongings, and contents cover often follows the item rather than"
                                + " the place. Labelled MOTOR because the vehicle damage is certain and"
                                + " the contents part depends on a policy the classifier cannot see."),
                new LabelledClaim(
                        "I tore a ligament skiing in Austria and paid for an MRI at a private clinic"
                                + " there. I want the cost back.",
                        ClaimType.TRAVEL,
                        "The genuinely arguable one. Treatment abroad on a trip is what travel cover is"
                                + " for, so TRAVEL — but it is also private treatment, which is exactly"
                                + " what HEALTH_TREATMENT describes. Expect the model to waver here, and"
                                + " read its rationale before calling it wrong."),
                new LabelledClaim(
                        "My GP referred me to a specialist and the public waiting list is eleven"
                                + " months. Can I use my insurance to go private?",
                        ClaimType.HEALTH_TREATMENT,
                        "A question rather than a loss, and it should still open a claim. Tests that"
                                + " the classifier reads what the person wants, not whether something"
                                + " has already gone wrong."),
                new LabelledClaim(
                        "Dropped my phone in the hotel pool on the second day of the holiday.",
                        ClaimType.TRAVEL,
                        "Short, and deliberately the mirror of the suitcase example. A possession"
                                + " damaged, but on a trip. Labelled TRAVEL to stay consistent with that"
                                + " row — if the model answers HOME_CONTENTS to both, it is us who are"
                                + " inconsistent, not it."),
                new LabelledClaim(
                        "Jeg har vært sykmeldt i fjorten måneder etter en ryggskade og har nå fått"
                                + " innvilget arbeidsavklaringspenger. Inntekten min har falt kraftig.",
                        ClaimType.DISABILITY,
                        "Norwegian, and it leans on a domestic term the catalogue mentions but does"
                                + " not explain. Checks that the classifier works in the language the"
                                + " room actually writes in, not only in English."),
                new LabelledClaim(
                        "water everywhere kitchen floor ruined came home to it",
                        ClaimType.HOME_CONTENTS,
                        "A real claim typed badly: no punctuation, no capitals, no subject. The risk is"
                                + " that it reads as noise and never gets past the claim check in task 2."
                                + " The same string is a probe in GuardrailProbe for that reason."),
                new LabelledClaim(
                        "The garage repaired my car after the last claim and did it so badly the door"
                                + " no longer shuts. I want to complain about them, not claim again.",
                        null,
                        "A complaint about a supplier, not a loss to insure. It is full of motor"
                                + " vocabulary, so the pull towards MOTOR is strong and wrong — this is"
                                + " the row that asks whether the agent matches on the situation or on"
                                + " the words."),
                new LabelledClaim(
                        "My premium went up 30% at renewal and nobody can tell me why.",
                        null,
                        "Nothing has happened to insure. It belongs to customer service, and opening a"
                                + " claim for it gives the person a reference number that will never"
                                + " answer their question."),
                new LabelledClaim(
                        "A tree came down in the storm and crushed the roof of my summer house.",
                        null,
                        "The uncomfortable label. Buildings cover is not one of the five — contents are"
                                + " belongings inside a home, not the structure. Expect the model to"
                                + " answer HOME_CONTENTS, and expect to argue about whether that is the"
                                + " model being wrong or the catalogue being too small."));
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
