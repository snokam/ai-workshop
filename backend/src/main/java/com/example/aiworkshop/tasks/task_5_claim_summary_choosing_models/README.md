# Task 5 — Summaries, and choosing a model

You decide which model does which job, having read what the calls actually cost.

## The part

One file, and the `TODO` in it has the steps.

| | File | What it is for |
|---|---|---|
| 1 | [`agent/SummaryConfig.java`](./agent/SummaryConfig.java) | Decide which model each job needs |

This is the only task with two agents of obviously different difficulty side by side, and until now
nothing has questioned them sharing a model.

| | is given | has to |
|---|---|---|
| [`agent/ClaimSummarizer.java`](./agent/ClaimSummarizer.java) | every document on the claim at once | read across them, notice where they disagree, write prose |
| [`agent/ClaimStatusWriter.java`](./agent/ClaimStatusWriter.java) | facts already worked out | put them in one sentence |

Both prompts are given. Both agents return `Result<String>`, so `SummaryDesk` logs what every call
cost — open a claim and read the log before you choose.

Measured on the status line while this was written:

| model | | tokens | for *"one short sentence"* |
|---|---|---|---|
| `gemini-2.5-flash` | 2.07s | 87 | "The motor claim is awaiting a police report to proceed." |
| `gemini-2.5-flash-lite` | 0.63s | 64 | `**Claim:** ... **Next Move:** ...` |

Three times faster and cheaper, and it ignored the format instruction. Both halves of that are real,
which is why the answer is not "use the cheap one where you can".

One number worth staring at: on the summariser, `inputTokenCount = 54`, `outputTokenCount = 51`,
`totalTokenCount = 244`. The other 139 are thinking tokens — spent, billed, and invisible in both the
prompt and the answer. Any cost estimate built from what you can see is wrong.

```bash
cd backend && ./mvnw test -Pevaluate
```

That scores the summary against [`evaluation/SummaryRubric.java`](./evaluation/SummaryRubric.java)
using [`evaluation/SummaryJudge.java`](./evaluation/SummaryJudge.java) — both given. It is how you
tell whether a model change made the answer worse rather than only cheaper.
