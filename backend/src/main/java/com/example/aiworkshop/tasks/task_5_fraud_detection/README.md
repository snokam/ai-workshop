# Task 5 — Case: Fraud detection

You write what the model cannot know, in plain Java after the answer.

The brief is `docs/tasks/task_5_fraud_detection.md`, from the repository root.

## The parts

Do them in this order. Each is one file, and the `TODO` at the top of it repeats the steps.

| | File | What it is for |
|---|---|---|
| 1 | [`checks/DuplicateUploadCheck.java`](./checks/DuplicateUploadCheck.java) | The same bytes, seen before |
| 2 | [`checks/ImageMetadataCheck.java`](./checks/ImageMetadataCheck.java) | What the file says about where it came from |
| 3 | [`checks/AddressedTheAgentCheck.java`](./checks/AddressedTheAgentCheck.java) | The report the agent already made |
| 4 | [`checks/FiguresCheck.java`](./checks/FiguresCheck.java) | A check written from nothing |

### Part 1 · `checks/DuplicateUploadCheck.java`

**The same bytes, seen before.**

`upload.contentHash()` is the file's fingerprint. Keep what you have seen and flag anything that
arrives twice.

Weigh it: the same file twice on one case is someone double-clicking, and the same file on a
different case is something else entirely.

### Part 2 · `checks/ImageMetadataCheck.java`

**What the file says about where it came from.**

EXIF, read from the bytes you already have. A photograph that has been through an editor, or whose
date sits oddly against the case, is worth a handler's attention.

No model, no network — this is the task's point.

### Part 3 · `checks/AddressedTheAgentCheck.java`

**The report the agent already made.**

Task 3's agent records text aimed at the software in `manipulationAttempt`. Turn that into an
indicator a handler sees.

Short, and worth thinking about: this check trusts the model to have noticed. What happens when
the document tells it not to?

### Part 4 · `checks/FiguresCheck.java`

**A check written from nothing.**

No scaffolding for this one. The extracted fields include amounts; decide what "the figures do not
add up" means and write it.

Expect false positives before you expect fraud. An early version summed an organisation number
because it looked like money.

## Everything else in the folder

- at the top — `FraudScreener`
- `checks/` — `AddressedTheAgentCheck`, `DuplicateUploadCheck`, `FiguresCheck`, `FraudCheck`, `ImageMetadataCheck`
- `model/` — `FraudScreening`

## What it uses from the tasks before it

- **task 3, Give it a file** — `DocumentAnalysis`, `DocumentStored`, `ExtractedField`, `ManipulationAttempt`

No task before this one refers to anything in here, which is the rule the workshop runs on.
`TaskDependencyTest` fails if it is ever broken.
