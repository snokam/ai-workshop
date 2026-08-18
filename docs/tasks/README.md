# Tasks

The workshop exercises. Each folder is one task: what to build, why it is worth building, and how to
know it works.

Every task's answer is already in this repository — the application runs, complete, before anyone
starts. That is deliberate. These are not tests, and an afternoon spent stuck on a compiler error
teaches nobody anything about agents. Try it yourself, then read what is there and argue with it.

| | |
|---|---|
| [task_1_guardrails](./task_1_guardrails.md) | A document that gives the agent orders, and the two guardrails that make it not matter whether the agent obeys |
| [task_2_postprocessing](./task_2_postprocessing.md) | What happens after the answer is accepted, in plain Java — no model, no network, no credentials |

The domain language every task uses is in [CONTEXT.md](../../CONTEXT.md). The decisions behind the
application, including the ones that were rejected, are in [docs/adr](../adr).
