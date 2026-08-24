# Task 7 — Case: Advisor chat

You write an agent that looks things up mid-answer and remembers the conversation.

The brief is `docs/tasks/task_7_advisor_chat.md`, from the repository root.

## What is yours to write

Each of these has the exercise parked in it as a comment headed `── To set this task again`.

- `agent/CaseChatAgent.java`
- `agent/CaseChatTools.java`

## What is in this folder

- at the top — `CaseChatController`, `CaseFile`, `ChatDesk`
- `agent/` — `CaseChatAgent`, `CaseChatTools`, `ChatConfig`, `DocumentReader`
- `model/` — `CaseAtAGlance`, `CaseDetail`, `ChatAnswer`, `ChatTurn`, `DocumentForChat`, `DocumentInDetail`, `ToolCall`
- `proposals/` — `DocumentRequest`, `DocumentRequestProposal`, `Proposal`, `ProposalCard`, `ProposalKind`, `ProposalState`, `ReviewProposal`
- `store/` — `CaseChatStore`, `DocumentRequestStore`, `ProposalStore`

## What it uses from the tasks before it

- **task 1, Your first agent** — `Case`, `CaseDesk`, `CaseOverview`, `CaseStatus`, `CaseStore`
- **task 3, Give it a file** — `CaseDocuments`, `DocumentFiles`, `DocumentReview`, `DocumentStore`, `ExtractedField`, `Quality` and 1 more
- **task 5, Case: Fraud detection** — `FraudScreener`, `FraudScreening`
- **task 6, Case: Claim summary** — `SummaryDesk`

No task before this one refers to anything in here. That is the rule the workshop runs on —
you can stop after any task and what you have still works — and `TaskDependencyTest` fails if
it is ever broken.
