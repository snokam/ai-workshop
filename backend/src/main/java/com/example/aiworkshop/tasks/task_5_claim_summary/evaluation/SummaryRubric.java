package com.example.aiworkshop.tasks.task_5_claim_summary.evaluation;

import java.util.List;

/**
 * Questions to ask about a summary, when there is no right answer to compare it with.
 *
 * <p>Nobody can write down the correct summary of a claim, so there is nothing to match. What can be
 * written down is what a good one must be true of, and each of these is a yes-or-no question a
 * reader could answer without arguing. That is the whole trick: not "is it good", which nothing can
 * score, but a handful of things that would each be obviously wrong if they failed.
 *
 * <p>Keep them answerable from the summary alone. A question that needs the source documents to
 * settle is a question about the world, and you will end up scoring the rubric rather than the
 * agent.
 */
public record SummaryRubric(String question, String whyItMatters) {

    public static List<SummaryRubric> all() {
        return List.of(
                new SummaryRubric(
                        "Does it describe the documents rather than address the reader?",
                        "The claimant and the handler both read this. 'You should send us the receipt' is"
                                + " written to one of them and is wrong for the other."),
                new SummaryRubric(
                        "Does every figure or date in it appear in one of the documents it was shown?",
                        "This is the failure that matters. A summary that invents a total is worse than no"
                                + " summary, because it is the one thing nobody re-checks."),
                new SummaryRubric(
                        "Does it avoid saying whether the claim should be paid?",
                        "Deciding is the handler's job. An agent that volunteers a verdict is answering a"
                                + " question nobody asked it, and someone will act on it."),
                new SummaryRubric(
                        "Would a handler who read only this know what to do next?",
                        "The point of it. A summary that is accurate and useless has still failed."));
    }
}
