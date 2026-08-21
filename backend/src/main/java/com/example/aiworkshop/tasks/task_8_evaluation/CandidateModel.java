package com.example.aiworkshop.tasks.task_8_evaluation;

import java.util.List;

/**
 * A model somebody might run this workshop on.
 *
 * <p>The other four evaluations ask how well one model does the job. This one asks a different
 * question — whether the job can be done on a given model at all — and it is the question a
 * facilitator has the day before, when someone says they only have access to a different one.
 *
 * <p>Three capabilities decide it, and the workshop stops dead without any of them: an answer that
 * parses into a record, a file the model will look at, and a tool it will actually call. A model
 * that is merely worse at classifying is a quality problem. A model that cannot return JSON in the
 * shape it was asked for is a different afternoon.
 */
public record CandidateModel(String label, Provider provider, String modelName, String note) {

    public enum Provider {
        VERTEX,
        ANTHROPIC
    }

    public static List<CandidateModel> all() {
        return List.of(
                new CandidateModel(
                        "gemini-2.5-flash",
                        Provider.VERTEX,
                        "gemini-2.5-flash",
                        "What the workshop is written against, and what the room will use."),
                new CandidateModel(
                        "claude-sonnet-4-5",
                        Provider.ANTHROPIC,
                        "claude-sonnet-4-5",
                        "A different family altogether. If the workshop holds on both, it is the"
                                + " prompts and the records doing the work rather than one vendor's habits."));
    }
}
