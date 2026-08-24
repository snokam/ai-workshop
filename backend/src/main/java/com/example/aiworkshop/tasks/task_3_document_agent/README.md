# Task 3 — Give it a file

You write an agent handed a PDF or a photograph, and the record that is its contract.

The brief is `docs/tasks/task_3_document_agent.md`, from the repository root.

## What is yours to write

Each of these has the exercise parked in it as a comment headed `── To set this task again`.

- `DocumentIntake.java`
- `agent/DocumentAnalyzer.java`
- `model/DocumentAnalysis.java`

## What is in this folder

- at the top — `CaseDocuments`, `DocumentIntake`, `DocumentProgress`, `DocumentReview`, `DocumentStored`, `DocumentsController`
- `agent/` — `DocumentAgentConfig`, `DocumentAnalyzer`
- `model/` — `DocumentAnalysis`, `DocumentForClaimant`, `ExtractedField`, `ManipulationAttempt`, `QualityAssessment`, `UploadedDocument`
- `store/` — `DocumentFiles`, `DocumentStore`

## What it uses from the tasks before it

- **task 1, Your first agent** — `Case`, `CaseProgress`, `CaseStatus`, `CaseStore`, `MatchConfidence`

No task before this one refers to anything in here. That is the rule the workshop runs on —
you can stop after any task and what you have still works — and `TaskDependencyTest` fails if
it is ever broken.
