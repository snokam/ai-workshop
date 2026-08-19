# Task 6 — Across documents

You write the expensive agent: every document at once, and what that costs.

The brief is `docs/tasks/task_5_summary.md`, from the repository root.

## What is yours to write

Each of these has the exercise parked in it as a comment headed `── To set this task again`.

- `DocumentForSummary.java`
- `agent/CaseSummarizer.java`

## What is in this folder

- at the top — `DocumentForSummary`, `SummaryDesk`
- `agent/` — `CaseStatusWriter`, `CaseSummarizer`, `SummaryConfig`
- `store/` — `CaseSummaryStore`

## What it uses from the tasks before it

- **task 1, Your first agent** — `Case`, `CaseStatus`
- **task 2, Give it a file** — `ExtractedField`, `Quality`, `UploadedDocument`

No task before this one refers to anything in here. That is the rule the workshop runs on —
you can stop after any task and what you have still works — and `TaskDependencyTest` fails if
it is ever broken.
