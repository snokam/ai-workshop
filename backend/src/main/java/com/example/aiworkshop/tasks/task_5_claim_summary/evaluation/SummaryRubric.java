package com.example.aiworkshop.tasks.task_5_claim_summary.evaluation;

import java.util.List;
import java.util.stream.Stream;

/**
 * Questions to ask about a summary, when there is no right answer to compare it with.
 *
 * <p>Task 4 scored two things that had labels: a claim type is one of five, and a probe either got
 * past the door or did not. Nobody can write down the correct summary of a claim, so both of those
 * techniques are unavailable here and the question has to change shape.
 *
 * <p>What can be written down is what a good summary must be <em>true of</em>. Each question below
 * is one a reader could answer yes or no without arguing. That is the whole trick: not "is it good",
 * which nothing can score, but a handful of things that would each be obviously wrong if they
 * failed.
 *
 * <p>Two rules make a question usable, and both are easier to see once broken.
 *
 * <p><b>It has to be answerable from the summary alone.</b> A question that needs the source
 * documents to settle is a question about the world, and you will end up scoring the rubric rather
 * than the agent.
 *
 * <p><b>It has to be able to fail.</b> "Is it well written?" cannot be answered no by anything, so
 * it measures nothing and will report a perfect score forever.
 */
public record SummaryRubric(String question, String whyItMatters) {

    /** What the evaluation runs over: the worked examples, then yours. */
    public static List<SummaryRubric> all() {
        return Stream.concat(examples().stream(), yours().stream()).toList();
    }

    public static List<SummaryRubric> yours() {
        // TODO — task 5, part 2. Add the questions the examples do not cover.
        //
        // Return a List.of(new SummaryRubric(question, whyItMatters), ...). Three or four on top of the
        // two examples is plenty.
        //
        // Read a summary the agent actually wrote first — run the application, open a claim with two
        // documents on it, and read what comes back. Write down what would have to be wrong with it for
        // you to refuse to show it to a claim handler. Those are the questions.
        //
        // Things worth a question, if you want somewhere to start:
        //
        //   - is it written about the documents, or to the reader? The claimant and the handler both see
        //     this, and "you should send us the receipt" is addressed to one of them.
        //   - does it say whether the claim should be paid? Deciding is the handler's job, and an agent
        //     that volunteers a verdict is answering a question nobody asked it.
        //   - would a handler who read only this know what to do next? A summary that is accurate and
        //     useless has still failed.
        //   - does it mention every document, or quietly drop the one it found hardest to read?
        //
        // whyItMatters is printed when a question fails, for whoever reads the run later. Say what goes
        // wrong in the world when it fails, not what the question means.

        return List.of();
    }

    private static List<SummaryRubric> examples() {
        return List.of(
                // The failure that matters most, and the one a rubric can actually catch. Note it is
                // answerable by reading: a figure is either in the documents or it is not.
                new SummaryRubric(
                        "Does every figure or date in it appear in one of the documents it was shown?",
                        "A summary that invents a total is worse than no summary, because it is the one"
                                + " thing nobody re-checks."),

                // A question about the summary alone, with nothing to look up. Contrast it with "is the
                // summary accurate?", which needs the whole claim and is really four questions.
                new SummaryRubric(
                        "Is it under six sentences?",
                        "It sits at the top of a screen a handler reads forty times a day. A summary long"
                                + " enough to skip is one that gets skipped."));
    }
}
