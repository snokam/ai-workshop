package com.example.aiworkshop.tasks.task_4_evaluation;

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
        // TODO — task 4, part 2. Write down what a handler needs.
        //
        // Return a List.of(new ExtractedFacts(file, mustFind, mustNotSay, why), ...) for the files in
        // assets/ — repair-receipt.pdf, receipt.png, driving_licence.png.
        //
        // The two lists are not the same measurement and must never be added together:
        //
        //   mustFind    what a handler would be annoyed to have missed
        //   mustNotSay  what the document does not contain — an agent that produces one of these has not
        //               misread anything, it has made it up
        //
        // Open the files and write down what you see. You will invent scoring rules as you go: is
        // "20 468,75" the same answer as "20468"? Deciding that is most of what building an evaluation
        // set actually is.

        return List.of();
    }

}
