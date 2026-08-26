# Task 5 — Summaries, and choosing a model

Pick which model each of two agents should use, after seeing what the calls cost.

Not every job needs the same model. You learn to decide by measuring rather than by habit: two
agents of very different difficulty, two models, and what each call actually costs.

## The part

| | File | What it is for |
|---|---|---|
| 1 | [`agent/SummaryConfig.java`](./agent/SummaryConfig.java) | Name the model each job runs on |

Two agents sit side by side here, and nothing until now has questioned them sharing a model:

| | is given | has to |
|---|---|---|
| [`ClaimSummarizer`](./agent/ClaimSummarizer.java) | every document on the claim at once | read across them, notice where they disagree, write prose |
| [`ClaimStatusWriter`](./agent/ClaimStatusWriter.java) | facts already worked out | put them in one sentence |

Both prompts are given. What you write is two strings:

| name | |
|---|---|
| `gemini-2.5-flash-lite` | fastest and cheapest. Thinks less, and does not reliably keep a format it was asked for |
| `gemini-2.5-flash` | what the rest of the workshop runs on. Thinks before it answers |
| `gemini-2.5-pro` | strongest, slowest, dearest |

They are built from the name you type, through task 1's
[`Models`](../task_1_first_agent/agent/Models.java) factory — so trying another is a one-word edit
rather than new wiring.

## Measure before you choose

Open a claim on the handler's screen — `/claimhandler`, then any claim. The status line is the
sentence at the top, the summary the prose below it. Both are written on first open and cached
after, so watch the backend log:

```
summary on gemini-2.5-flash took 1907ms, 497 tokens (205 of them thinking), $0.000635
status line on gemini-2.5-flash-lite took 574ms, 334 tokens (0 of them thinking), $0.000042
```

Fifteen times the cost — and 205 of those tokens are *thinking*: spent, billed at the output rate,
and invisible in both the prompt and the answer. Any estimate built from what you can see is wrong.

That does not make the cheap one better value. Asked for "one short sentence" it answers in
markdown, in a line that is rendered as plain text. The saving is real and so is the mess.

```bash
cd backend && ./mvnw test -Pevaluate -Dtest=SummaryEvaluation
```

scores the summary against the rubric in [`evaluation/`](./evaluation) and prints the cost beside
the score, which is how you tell whether a cheaper model made the answer worse rather than only
cheaper. Drop the `-Dtest` to run every evaluation in the workshop instead.

## Where everything is

| | |
|---|---|
| [`agent/`](./agent) | the two agents, their prompts, and the part you write |
| [`SummaryDesk.java`](./SummaryDesk.java) | what the screen calls. Logs time, tokens and cost on every call |
| [`ModelPrices.java`](./ModelPrices.java) | published prices and the arithmetic, with the date they were checked |
| [`store/`](./store) | where a summary is kept, so re-opening a claim does not pay for it again |
| [`evaluation/`](./evaluation) | the rubric and the judge that `-Pevaluate` runs |
