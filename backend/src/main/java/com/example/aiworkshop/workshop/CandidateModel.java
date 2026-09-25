package com.example.aiworkshop.workshop;

import java.util.List;

/**
 * A model somebody might run this workshop on.
 *
 * <p>Task 4's evaluations ask how well one model does the job. This asks a different question —
 * whether the job can be done on a given model at all — and it is the one a facilitator has the
 * day before, when somebody says they only have access to a different one.
 *
 * <p>Three capabilities decide it, and the workshop stops dead without any of them: an answer that
 * parses into a record, a file the model will look at, and a tool it will actually call. A model
 * that is merely worse at classifying is a quality problem. A model that cannot return JSON in the
 * shape it was asked for is a different afternoon.
 */
public record CandidateModel(String label, Provider provider, String modelName, String note) {

    public enum Provider {
        FOUNDRY,
        VERTEX,
        ANTHROPIC
    }

    /**
     * A candidate needs credentials for its provider to be checked at all. The ones it does not have
     * are reported as unavailable rather than failing the run, so this is worth running with whatever
     * is to hand.
     */
    public static List<CandidateModel> all() {
        return List.of(
                new CandidateModel(
                        "gpt-5.6-luna",
                        Provider.FOUNDRY,
                        "gpt-5.6-luna",
                        "What the workshop is written against, and what the room will use."),
                new CandidateModel(
                        "gpt-5.4-mini",
                        Provider.FOUNDRY,
                        "gpt-5.4-mini",
                        "The smallest deployment. Task 7 streams from it, so it has to manage the"
                                + " three capabilities too, not only be quick."),
                new CandidateModel(
                        "claude-sonnet-4-6",
                        Provider.FOUNDRY,
                        "claude-sonnet-4-6",
                        "A different family altogether, on the same resource. If the workshop holds on"
                                + " both, it is the prompts and the records doing the work rather than"
                                + " one vendor's habits."),
                new CandidateModel(
                        "gemini-2.5-flash",
                        Provider.VERTEX,
                        "gemini-2.5-flash",
                        "The other provider entirely, for a room that has Google Cloud rather than"
                                + " Azure. Needs gcloud credentials."));
    }
}
