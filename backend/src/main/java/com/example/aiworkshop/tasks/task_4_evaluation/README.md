# Task 4 — How would you know?

You build the two sets the evaluations run over, then read what they say about the agents you wrote.

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

## If you finish early

- **Run it three times** and count how many answers move. Then decide what your temperature should be.
- **Write a fifth attack.** The set that matters is not the one that shipped, it is the one you
  thought of that is not in it — and the four there were written by the same person who wrote the
  guardrails, which is the weakest possible test and worse here than anywhere, because an attacker
  is trying.
- **Score the chat by its tools.** Ask it five questions whose answers need a specific tool, and
  check which it called. That is a fourth technique again, and the one closest to how agents are
  evaluated in practice.
- **Score the confidence.** Of the answers that disagreed with the label, how many said `HIGH`? An
  agent that is confidently wrong is worse than one that is unsure.
- **Write the keyword baseline** — twenty lines of `contains()`. Whatever it scores is the bar the
  model has to clear to be worth its cost.
