# Task 3 — Don't be talked round

You write two checks, one on the way out and one on the way back.

The brief is `docs/tasks/task_3_guardrails.md`, from the repository root.

## What is yours to write

Each of these has the exercise parked in it as a comment headed `── To set this task again`.

- `guardrails/AnalysisGuardrail.java`
- `guardrails/Guardrails.java`
- `guardrails/UploadedFileGuardrail.java`

## What is in this folder

- `guardrails/` — `AnalysisGuardrail`, `GuardrailConfig`, `Guardrails`, `UploadedFileGuardrail`

## What it uses from the tasks before it

- **task 2, Give it a file** — `DocumentIntake`

No task before this one refers to anything in here. That is the rule the workshop runs on —
you can stop after any task and what you have still works — and `TaskDependencyTest` fails if
it is ever broken.
