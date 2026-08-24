# Task 6 — Case: Claim summary

You write the expensive agent, and decide what it is shown.

The brief is `docs/tasks/task_6_case_summary.md`, from the repository root.

## The parts

Do them in this order. Each is one file, and the `TODO` at the top of it repeats the steps.

| | File | What it is for |
|---|---|---|
| 1 | [`agent/CaseSummarizer.java`](./agent/CaseSummarizer.java) | Write the expensive agent |
| 2 | [`DocumentForSummary.java`](./DocumentForSummary.java) | Decide what it is shown |

### Part 1 · `agent/CaseSummarizer.java`

**Write the expensive agent.**

Every document on a case, read together, in one prompt on every screen load.

The prompt has to make it describe the documents rather than address anyone, avoid deciding
whether the claim should be paid, and never state a figure that is not in what it was shown.

### Part 2 · `DocumentForSummary.java`

**Decide what it is shown.**

This record is what the summariser sees, once per document per screen load. Every component is
paid for every time, and the ones nobody reads are the whole of the cost with none of the value.

Look at what is deliberately missing before adding it back. There are no bytes here: the file was
read in task 3, and reading it again would pay twice for one answer.

## Everything else in the folder

- at the top — `DocumentForSummary`, `SummaryDesk`
- `agent/` — `CaseStatusWriter`, `CaseSummarizer`, `SummaryConfig`
- `evaluation/` — `SummaryJudge`, `SummaryRubric`
- `store/` — `CaseSummaryStore`

## What it uses from the tasks before it

- **task 1, Your first agent** — `Case`, `CaseStatus`
- **task 3, Give it a file** — `ExtractedField`, `Quality`, `UploadedDocument`
- **task 5, Case: Fraud detection** — `FraudScreening`

No task before this one refers to anything in here, which is the rule the workshop runs on.
`TaskDependencyTest` fails if it is ever broken.
