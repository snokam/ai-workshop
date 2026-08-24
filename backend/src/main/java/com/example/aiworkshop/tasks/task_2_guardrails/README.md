# Task 2 — Is this even a claim?

You write one guardrail, and it runs before the model does.

The brief is `docs/tasks/task_2_guardrails.md`, from the repository root.

## What is yours to write

Each of these has the exercise parked in it as a comment headed `── To set this task again`.

- `guardrails/ClaimDescriptionGuardrail.java`
- `guardrails/Guardrails.java`

## What is in this folder

- `guardrails/` — `ClaimDescriptionGuardrail`, `GuardrailConfig`, `Guardrails`

`GuardrailConfig` publishes the guardrail as a bean. Task 1's agent takes whichever guardrails exist
and knows nothing about who wrote them, so task 1 can be finished and run before this task is
started.

## What it uses from the tasks before it

Nothing at all. It decides on the text alone — no model, no credentials, no network — which is what
makes an input guardrail the only check here that costs nothing when it refuses.

No task before this one refers to anything in here. That is the rule the workshop runs on, and
`TaskDependencyTest` fails if it is ever broken.
