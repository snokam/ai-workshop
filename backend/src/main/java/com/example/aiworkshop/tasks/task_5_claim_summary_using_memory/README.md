# Task 5 — Claim summary using memory

You give the expensive agent a memory of what it said last time, so a handler coming back to a claim
is told what changed rather than the same thing again.

## The part

One file, and the `TODO` in it has the steps.

| | File | What it is for |
|---|---|---|
| 1 | [`agent/SummaryConfig.java`](./agent/SummaryConfig.java) | Give the summariser a memory |

Everything else is given. [`agent/ClaimSummarizer.java`](./agent/ClaimSummarizer.java) is the
expensive agent — every document on a claim, read together, in one prompt on every screen load — and
its `@MemoryId` is why this task exists. [`DocumentForSummary.java`](./DocumentForSummary.java) is
what it is shown per document, and worth reading: every component of it is paid for on every open,
and there are deliberately no bytes, because the file was already read in task 3.

Two things to get right, and only one of them is the number.

**One memory per claim**, built inside the lambda rather than once outside it. A provider that
ignores its id compiles, starts, and behaves perfectly on one claim; open a second and the summariser
is looking at the first claimant's documents. Nothing throws and the summary reads as fluently as
ever. `SummaryConfigTest` is what catches it.

**The window is a trade**, and it is the real exercise. A message here is not a chat line — it is a
whole claim's documents rendered into a prompt, plus the summary that came back. Small and it forgets
the documents first, so the next summary is written from the last summary: a copy of a copy, losing a
detail a round. Large and every past round is re-sent on every screen load.

```bash
cd backend && ./mvnw test -Pevaluate
```

That scores the summary against [`evaluation/SummaryRubric.java`](./evaluation/SummaryRubric.java) —
six questions a reader could answer yes or no without arguing — using
[`evaluation/SummaryJudge.java`](./evaluation/SummaryJudge.java), a model asked one of them at a
time. Both are given. Read the judge sceptically: asking a model whether text is good measures its
agreeableness, and the three things that make it a judge rather than a rubber stamp are in that file.

It is how you tell which window you chose was better.
