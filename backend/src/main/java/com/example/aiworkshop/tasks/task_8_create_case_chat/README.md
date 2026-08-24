# Task 8 — Case: File claim with AI chat

You write an intake agent allowed to ask before it commits.

The brief is `docs/tasks/task_8_create_case_chat.md`, from the repository root.

## What is yours to write

Each of these has the exercise parked in it as a comment headed `── To set this task again`.

- `agent/CaseIntakeInterviewer.java`

## What is in this folder

- at the top — `CaseInterviewController`, `InterviewCaseOpener`
- `agent/` — `CaseIntakeInterviewer`, `CreateCaseChatConfig`
- `model/` — `CaseScenario`, `InterviewTurn`

## What it uses from the tasks before it

- **task 1, Your first agent** — `Case`, `CaseStatus`, `CaseStore`, `CaseType`, `CreatedCase`, `MatchConfidence`

No task before this one refers to anything in here. That is the rule the workshop runs on —
you can stop after any task and what you have still works — and `TaskDependencyTest` fails if
it is ever broken.
