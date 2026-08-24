# Task 3 — Give it a file

You write the agent that is handed a file, and the record that is its output schema.

The brief is `docs/tasks/task_3_document_agent.md`, from the repository root.

## The parts

Do them in this order. Each is one file, and the `TODO` at the top of it repeats the steps.

| | File | What it is for |
|---|---|---|
| 1 | [`agent/DocumentAnalyzer.java`](./agent/DocumentAnalyzer.java) | Write the agent |
| 2 | [`model/DocumentAnalysis.java`](./model/DocumentAnalysis.java) | Finish the record |
| 3 | [`DocumentIntake.java`](./DocumentIntake.java) | Send the file as itself |

### Part 1 · `agent/DocumentAnalyzer.java`

**Write the agent.**

The same shape as task 1, handed a file. The prompt has to make it say what kind of document this
is, pull out the facts a handler would care about, decide which of the case's required documents
it satisfies, and judge whether the file is legible enough to work with.

It is also where the document gets to argue back, so it has to notice text addressed to *it*
rather than to a person and record it in `manipulationAttempt`.

### Part 2 · `model/DocumentAnalysis.java`

**Finish the record.**

The return type is the output schema. Add a component and the agent starts filling it in; the
`@Description` on each one is part of the prompt, not documentation.

Two components are missing. Work out what they should be from what the screens need and what the
checks in task 5 read.

### Part 3 · `DocumentIntake.java`

**Send the file as itself.**

A list of `Content`: one text and one file. `DocumentFiles.contentOf(bytes, mimeType)` decides
between `PdfFileContent` and `ImageContent` — nothing extracts text first, the model is handed the
document.

The text beside it must be exactly `INTAKE_INSTRUCTION`, and the reason is worth knowing: it is
the only thing that makes the file the user turn rather than a system one. Get it wrong and the
agent quietly reads nothing.

## Everything else in the folder

- at the top — `CaseDocuments`, `DocumentIntake`, `DocumentProgress`, `DocumentReview`, `DocumentStored`, `DocumentsController`
- `agent/` — `DocumentAgentConfig`, `DocumentAnalyzer`
- `model/` — `DocumentAnalysis`, `DocumentForClaimant`, `ExtractedField`, `ManipulationAttempt`, `QualityAssessment`, `UploadedDocument`
- `store/` — `DocumentFiles`, `DocumentStore`

## What it uses from the tasks before it

- **task 1, Your first agent** — `Case`, `CaseProgress`, `CaseStatus`, `CaseStore`, `MatchConfidence`

No task before this one refers to anything in here, which is the rule the workshop runs on.
`TaskDependencyTest` fails if it is ever broken.
