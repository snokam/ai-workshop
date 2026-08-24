# Task 4 — How would you know?

You build the two sets the evaluations run over, then read what they say about the agents you wrote.

The brief is `docs/tasks/task_4_evaluation.md`, from the repository root.

## The parts

Do them in this order. Each is one file, and the `TODO` at the top of it has the steps.

| | File | What it is for |
|---|---|---|
| 1 | [`LabelledCase.java`](./LabelledCase.java) | Label the cases you would argue about |
| 2 | [`GuardrailProbe.java`](./GuardrailProbe.java) | Write what should and should not get past the door |

## Running them

```bash
cd backend && ./mvnw test -Pevaluate
```

That runs both evaluations and nothing else. They are kept out of the ordinary `./mvnw test` because
they call a real model — a normal test run stays free and needs no credentials.

| Evaluation | Asks about | Set |
|---|---|---|
| `ClassifierEvaluation` | the classifier from task 1 | `LabelledCase` |
| `GuardrailEvaluation` | both guardrails from task 2 | `GuardrailProbe` |

Both print a table instead of asserting. That is deliberate: a number decides nothing, and the
exercise is reading the rows that disagree.
