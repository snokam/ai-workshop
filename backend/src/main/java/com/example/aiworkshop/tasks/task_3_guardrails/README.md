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

This is the only task with no `model/`, and the reason is worth a moment. A guardrail does not
introduce anything of its own: the one on the way out reads the file task 2 is about to send, and
the one on the way back reads the `DocumentAnalysis` task 2 asked for — including
`ManipulationAttempt`, which is what the agent fills in when a document tries to give it orders.
That record belongs to task 2 because task 2's agent returns it, and a guardrail that owned it
would mean task 2 depending on task 3, which is the wrong way round.

## What it uses from the tasks before it

- **task 2, Give it a file** — `DocumentIntake`

No task before this one refers to anything in here. That is the rule the workshop runs on —
you can stop after any task and what you have still works — and `TaskDependencyTest` fails if
it is ever broken.
