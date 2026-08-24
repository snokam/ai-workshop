package com.example.aiworkshop.tasks.task_5_claim_summary_using_memory.evaluation;

import java.util.List;

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

    /** What the evaluation runs over. Given — the exercise in this task is the memory. */
    public static List<SummaryRubric> all() {
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
                                + " enough to skip is one that gets skipped."),
                new SummaryRubric(
                        "Does it describe the documents rather than address the reader?",
                        "The claimant and the handler both read this. 'You should send us the receipt' is"
                                + " written to one of them and is wrong for the other."),
                new SummaryRubric(
                        "Does it avoid saying whether the claim should be paid?",
                        "Deciding is the handler's job. An agent that volunteers a verdict is answering a"
                                + " question nobody asked it, and someone will act on it."),
                new SummaryRubric(
                        "Would a handler who read only this know what to do next?",
                        "The point of it. A summary that is accurate and useless has still failed."),
                new SummaryRubric(
                        "Is every document it was shown accounted for?",
                        "The one it leaves out is the one it found hardest to read, which is the one worth"
                                + " looking at. A summary that quietly drops a document reads as complete."));
    }
}
