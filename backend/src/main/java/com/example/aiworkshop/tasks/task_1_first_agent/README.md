# Task 1 — Your first agent

You write the model itself, an agent that reads a sentence, and the case its answer opens.

The brief is `docs/tasks/task_1_first_agent.md`, from the repository root.

## The parts

Do them in this order. Each is one file, and the `TODO` at the top of it repeats the steps.

| | File | What it is for |
|---|---|---|
| 1 | [`agent/VertexAiConfig.java`](./agent/VertexAiConfig.java) | Build the model |
| 2 | [`agent/CaseTypeClassifier.java`](./agent/CaseTypeClassifier.java) | Write the agent |
| 3 | [`CaseIntake.java`](./CaseIntake.java) | Turn the answer into a case |

### Part 1 · `agent/VertexAiConfig.java`

**Build the model.**

The connection every agent in this workshop runs on: which provider, which model, which
credentials. `VertexAiGeminiChatModel.builder()` takes the values already bound in
`VertexAiProperties` — read that record to see what is configurable, and `application.properties`
to see where it comes from.

Nothing works until this returns a model, so it is first.

### Part 2 · `agent/CaseTypeClassifier.java`

**Write the agent.**

An interface, a system message and a return type — LangChain4j writes the implementation, so this
interface *is* the agent.

The prompt has to make it read a sentence someone typed and choose one of the case types it is
shown through `{{caseTypes}}`. It has to say how sure it is, and give one plain sentence of
reasoning, because that is the shape of `CaseTypeSuggestion`, the record it returns.

Two things are easy to miss. There is no case type for "something else", so when nothing fits it
must name no type at all rather than force the closest one. And the rationale has two readers:
name a type and a handler reads it, name none and the claimant does, because it is the only thing
they will see.

### Part 3 · `CaseIntake.java`

**Turn the answer into a case.**

`classifier.classify(CaseType.catalog(), description)` is the call, and what comes back is a
`CaseTypeSuggestion`: a type, a confidence, and a sentence of reasoning.

The type decides the checklist — `CaseType.requiredDocuments()` — so this is where the model's
answer stops being a suggestion and becomes the shape of someone's case. Give it a reference,
save it, and return the `CreatedCase` the screen shows.

When the suggestion names no type, throw `NothingWeCoverException` with the rationale. The
controller turns that into a 422 the claimant reads.

## Everything else in the folder

- at the top — `CaseDesk`, `CaseIntake`, `CaseProgress`, `CasesController`
- `agent/` — `CaseTypeClassifier`, `FirstAgentConfig`, `VertexAiConfig`, `VertexAiProperties`
- `model/` — `Case`, `CaseOverview`, `CaseStatus`, `CaseType`, `CaseTypeSuggestion`, `CreatedCase`, `MatchConfidence`
- `store/` — `CaseStore`

## What it uses from the tasks before it

Nothing. It stands on its own.

No task before this one refers to anything in here, which is the rule the workshop runs on.
`TaskDependencyTest` fails if it is ever broken.
