# Task 4 — Evaluation

Build test sets and measure the agents you wrote in tasks 1 and 2.

A prompt feels right long before it is right. You learn how to tell the difference: a small set of
examples with known answers, run over the agents you already wrote, and read the rows where the
agent disagrees with you.

## The parts

Do them in this order. Each is one file, and the `TODO` at the top of it has the steps.

| | File | What it is for |
|---|---|---|
| 1 | [`LabelledClaim.java`](./LabelledClaim.java) | Label the claims you would argue about |
| 2 | [`GuardrailProbe.java`](./GuardrailProbe.java) | Write what should and should not get past the door |

## Running them

```bash
cd backend && ./mvnw test -Pevaluate
```

That runs both evaluations and nothing else. They are kept out of the ordinary `./mvnw test` because
they call a real model — a normal test run stays free and needs no credentials.

| Evaluation | Asks about | Set |
|---|---|---|
| `ClassifierEvaluation` | the classifier from task 1 | `LabelledClaim` |
| `GuardrailEvaluation` | both guardrails from task 2 | `GuardrailProbe` |

Both print a table instead of asserting. That is deliberate: a number decides nothing, and the
exercise is reading the rows that disagree.
