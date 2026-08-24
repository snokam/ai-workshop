# Task 8 — Case: File claim with AI chat

You write an interview instead of a form.

The brief is `docs/tasks/task_8_create_case_chat.md`, from the repository root.

## The parts

Do them in this order. Each is one file, and the `TODO` at the top of it repeats the steps.

| | File | What it is for |
|---|---|---|
| 1 | [`agent/CaseIntakeInterviewer.java`](./agent/CaseIntakeInterviewer.java) | Write the interviewer |

### Part 1 · `agent/CaseIntakeInterviewer.java`

**Write the interviewer.**

An interview instead of a form. It reads a transcript and either asks for what is missing or
decides.

The hard part is when to stop asking. Too eager and it opens the wrong case; too cautious and it
interrogates someone who has already said enough. And there is no scenario for "something else",
so when nothing fits it must say so rather than force a poor match.

## Everything else in the folder

- at the top — `CaseInterviewController`, `InterviewCaseOpener`
- `agent/` — `CaseIntakeInterviewer`, `CreateCaseChatConfig`
- `model/` — `CaseScenario`, `InterviewTurn`

## What it uses from the tasks before it

- **task 1, Your first agent** — `Case`, `CaseIntake`, `CaseStatus`, `CaseStore`, `CaseType`, `CreatedCase` and 1 more

No task before this one refers to anything in here, which is the rule the workshop runs on.
`TaskDependencyTest` fails if it is ever broken.
