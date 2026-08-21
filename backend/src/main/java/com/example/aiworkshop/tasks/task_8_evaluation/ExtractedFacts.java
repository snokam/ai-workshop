package com.example.aiworkshop.tasks.task_8_evaluation;

import java.util.List;

/**
 * What a person reading one of the sample documents would say is in it.
 *
 * <p>The classifier answered with one value out of a list, so scoring it was a comparison. This
 * agent answers with a handful of facts it chose itself, and there is no list to compare against:
 * two readers of the same receipt will write the total the same way and the vendor differently, and
 * neither is wrong. So the question changes from "did it match" to "did it find", and the score
 * stops being a percentage of anything.
 *
 * <p>Two lists, because there are two ways to be wrong. {@code mustFind} is what a handler would be
 * annoyed to have missed. {@code mustNotSay} is what the document does not contain, and an agent
 * that produces one of those has not misread anything — it has made it up, which is the failure
 * that survives a demo and ruins a case.
 */
public record ExtractedFacts(String file, List<String> mustFind, List<String> mustNotSay, String why) {

    public static List<ExtractedFacts> all() {
        // TODO — task 8, part 2. Write down what a handler needs from each file. One version:
        //
        // return List.of(
        //         new ExtractedFacts(
        //                 "repair-receipt.pdf",
        //                 List.of("20 468,75", "2026-07-14", "NORDIC BIL"),
        //                 List.of("covered by your policy", "claim approved", "we will reimburse"),
        //                 "The total, the date and who issued it are what a handler needs. Note that the total"
        //                         + " is written the way the document writes it, spaces and comma and all —"
        //                         + " the first version of this file said 20468 and scored a miss against a"
        //                         + " document that plainly contains the number. The phrases it must not say are"
        //                         + " the ones a helpful model adds when it starts deciding the claim instead of"
        //                         + " reading the paper. 'Approved' alone is not one of them: the receipt says it"
        //                         + " about the card payment, and a check that looks for it fails honestly."),
        //         new ExtractedFacts(
        //                 "receipt.png",
        //                 List.of(),
        //                 List.of("claim", "policy"),
        //                 "A photograph rather than a PDF, and the same agent has to read it. Fill mustFind in"
        //                         + " yourself from what you can see in the image — doing that by hand is most of"
        //                         + " what building an evaluation set actually is."),
        //         new ExtractedFacts(
        //                 "driving_licence.png",
        //                 List.of(),
        //                 List.of("policy number", "claim number"),
        //                 "An identity document, where inventing a field is far worse than missing one. What"
        //                         + " should it refuse to guess at?"));
        //

        return List.of();
    }

    // ── To set this task again ────────────────────────────────────────────────────────
    // TODO — task 8, part 2. Fill in the two empty mustFind lists.
    //
    // Open the files in assets/, read them the way a case handler would, and write down the facts
    // you would be annoyed to find missing. Then run ExtractionEvaluation and see how much of it
    // the agent found.
    //
    // Notice what you had to decide while doing it. Is "20 468,75" the same answer as "20468.75"?
    // Is a vendor named in full the same as its trading name? Every one of those is a scoring rule
    // you have just invented, and the reason extraction evaluations are harder than they look.
}
