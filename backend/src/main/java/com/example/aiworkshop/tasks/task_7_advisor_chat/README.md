# Task 7 — Case: Advisor chat

You write an agent with tools and a memory, that suggests and never writes.

The brief is `docs/tasks/task_7_advisor_chat.md`, from the repository root.

## The parts

Do them in this order. Each is one file, and the `TODO` at the top of it repeats the steps.

| | File | What it is for |
|---|---|---|
| 1 | [`agent/CaseChatAgent.java`](./agent/CaseChatAgent.java) | Write the agent |
| 2 | [`agent/CaseChatTools.java`](./agent/CaseChatTools.java) | Write the tools |

### Part 1 · `agent/CaseChatAgent.java`

**Write the agent.**

Tools, a memory per case, and a `Result` so tool calls survive the round trip.

The prompt has to make it answer from what it is given, reach for a tool when it is not, and
propose rather than act — it never writes to a case, it suggests and waits.

### Part 2 · `agent/CaseChatTools.java`

**Write the tools.**

Two of them. The `@Tool` description is what decides whether the tool gets called — it is a
prompt, not documentation, and it is the whole exercise.

The methods themselves hold no logic. Each hands straight to the desk.

## Everything else in the folder

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

No task before this one refers to anything in here, which is the rule the workshop runs on.
`TaskDependencyTest` fails if it is ever broken.
