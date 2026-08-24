# Task 5 — Claim summary

You write the expensive agent, what it remembers between one look at a claim and the next,
and the questions that say whether its answer is any good.

## The parts

Do them in this order. Each is one file, and the `TODO` at the top of it has the steps.

| | File | What it is for |
|---|---|---|
| 1 | [`agent/ClaimSummarizer.java`](./agent/ClaimSummarizer.java) | Write the expensive agent |
| 2 | [`agent/SummaryConfig.java`](./agent/SummaryConfig.java) | Give it a memory |
| 3 | [`evaluation/SummaryRubric.java`](./evaluation/SummaryRubric.java) | Say what a good summary must be true of |

Part 2 is the first memory in the workshop — task 6's chat has one too, and it is given there. A
handler who read this claim yesterday does not want the same six sentences plus two; they want what
arrived since, and whether it agrees with what was already there. The agent can only write that if it
can see what it said last time.

Two things to get right, and only one of them is the number. **One memory per claim** — built inside
the lambda, not once outside it, or one claimant's documents get summarised onto another claimant's
screen. That is what `SummaryConfigTest` checks. **The window is a trade**: small and it forgets the
documents first, so the next summary is written from the last summary — a copy of a copy, losing a
detail a round. Large and every past round is re-sent on every screen load.

Part 3 is how you tell which window was better.

Part 3 is a different kind of evaluation from task 4's. There, both sets had labels — a claim type is
one of five, a probe either got past the door or did not. Nobody can write down the correct summary
of a claim, so there is nothing to match against. What can be written down is what a good one must be
**true of**, as questions a reader could answer yes or no without arguing.

[`evaluation/SummaryJudge.java`](./evaluation/SummaryJudge.java) beside it is given: a model asked
one of your questions at a time. Read it sceptically — it is the technique here most easily used
badly, because asking a model whether some text is good measures its agreeableness. Three things
make it a judge rather than a rubber stamp, and all three are in that file.

```bash
cd backend && ./mvnw test -Pevaluate
```

Answer your own questions by hand before you read what the judge said. Where you disagree is the
finding.

[`DocumentForSummary.java`](./DocumentForSummary.java) beside it is given, and worth reading before
you start: it is what the agent is shown for each document, and every component of it is paid for on
every open.
