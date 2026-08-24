# Task 1 — Your first agent

You write the model itself, an agent that reads a sentence, and the case its answer opens.

The brief is `docs/tasks/task_1_first_agent.md`, from the repository root.

## What is yours to write

Each of these has the exercise parked in it as a comment headed `── To set this task again`.

- `CaseIntake.java`
- `agent/CaseTypeClassifier.java`
- `agent/VertexAiConfig.java`

## What is in this folder

- at the top — `CaseDesk`, `CaseIntake`, `CaseProgress`, `CasesController`
- `agent/` — `CaseTypeClassifier`, `FirstAgentConfig`, `VertexAiConfig`, `VertexAiProperties`
- `model/` — `Case`, `CaseOverview`, `CaseStatus`, `CaseType`, `CaseTypeSuggestion`, `CreatedCase`, `MatchConfidence`
- `store/` — `CaseStore`

## What it uses from the tasks before it

Nothing. It stands on its own.

No task before this one refers to anything in here. That is the rule the workshop runs on —
you can stop after any task and what you have still works — and `TaskDependencyTest` fails if
it is ever broken.
