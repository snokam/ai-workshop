# Task 4 — What the model cannot know

You write plain Java after the answer, and one check written from nothing.

The brief is `docs/tasks/task_4_postprocessing.md`, from the repository root.

## What is yours to write

Each of these has the exercise parked in it as a comment headed `── To set this task again`.

- `checks/AddressedTheAgentCheck.java`
- `checks/DuplicateUploadCheck.java`
- `checks/FiguresCheck.java`
- `checks/ImageMetadataCheck.java`

## What is in this folder

- at the top — `FraudScreener`
- `checks/` — `AddressedTheAgentCheck`, `DuplicateUploadCheck`, `FiguresCheck`, `FraudCheck`, `ImageMetadataCheck`
- `model/` — `FraudScreening`

## What it uses from the tasks before it

- **task 2, Give it a file** — `DocumentAnalysis`, `DocumentStored`, `ExtractedField`, `ManipulationAttempt`

No task before this one refers to anything in here. That is the rule the workshop runs on —
you can stop after any task and what you have still works — and `TaskDependencyTest` fails if
it is ever broken.
