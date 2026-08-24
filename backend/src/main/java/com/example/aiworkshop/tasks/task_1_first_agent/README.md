# Task 1 — Your first agent

You write the model itself, an agent that reads a sentence, and the claim its answer opens.

## The parts

Do them in this order. Each is one file, and the `TODO` at the top of it has the steps.

| | File | What it is for |
|---|---|---|
| 1 | [`agent/VertexAiConfig.java`](./agent/VertexAiConfig.java) | Build the model |
| 2 | [`agent/ClaimTypeClassifier.java`](./agent/ClaimTypeClassifier.java) | Write the agent |
| 3 | [`ClaimIntake.java`](./ClaimIntake.java) | Turn the answer into a claim |

## If you finish early

- **Add a field to `ClaimTypeSuggestion`** — say, the one question worth asking back. Change nothing
  else. It gets filled in, because the record is the schema.
- **Take `{{claimTypes}}` out** and hard-code the list in the prompt. It still works, and it is now
  two lists that have to agree.
- **Turn the temperature up** in `application.properties` and classify the same borderline
  description ten times. How stable is a decision you were about to build a checklist on?
