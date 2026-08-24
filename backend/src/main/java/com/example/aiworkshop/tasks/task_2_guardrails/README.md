# Task 2 — Is this even a claim?

You write one guardrail, and it runs before the model does.

The brief is `docs/tasks/task_2_guardrails.md`, from the repository root.

## What is yours to write

Each of these has the exercise parked in it as a comment headed `── To set this task again`.

- `guardrails/ClaimCheck.java`
- `guardrails/ClaimDescriptionGuardrail.java`
- `guardrails/Guardrails.java`

## What is in this folder

- `guardrails/` — `ClaimCheck`, `ClaimDescriptionGuardrail`, `GuardrailConfig`, `Guardrails`

`GuardrailConfig` publishes the guardrail as a bean. Task 1's agent takes whichever guardrails exist
and knows nothing about who wrote them, so task 1 can be finished and run before this task is
started.

## What it uses from the tasks before it

The ChatModel from task 1, and nothing else. The check is an agent like any other, built from the
same connection — a second, narrower one asked a single closed question in front of the first.

No task before this one refers to anything in here. That is the rule the workshop runs on, and
`TaskDependencyTest` fails if it is ever broken.
