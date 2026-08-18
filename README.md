# AI workshop — document handling

Upload a document, and an agent reads it: what kind of document it is, the facts worth pulling out
of it, and whether the file is legible enough to work with.

The domain language lives in [CONTEXT.md](./CONTEXT.md), and the workshop exercises in
[docs/tasks/](./docs/tasks).

## Running it

Two terminals. Maven does not build the frontend and never will — Vite proxies `/api` to Spring Boot,
so the browser sees a single origin and there is no CORS configuration anywhere.

```bash
# terminal 1 — backend on :8080
export AZURE_OPENAI_API_KEY=...        # or use the Vertex provider, see below
./mvnw spring-boot:run

# terminal 2 — frontend on :5173
cd frontend && npm install && npm run dev
```

Then open http://localhost:5173.

## Choosing a provider

`aiworkshop.model.provider` picks which `ChatModel` bean is built. Override per run:

```bash
AI_PROVIDER=vertex ./mvnw spring-boot:run    # Gemini on Vertex AI
AI_PROVIDER=foundry ./mvnw spring-boot:run   # a deployment on Azure AI Foundry
```

Vertex authenticates with Application Default Credentials, not an API key:

```bash
gcloud auth application-default login
gcloud auth application-default set-quota-project "$GOOGLE_CLOUD_PROJECT"
```

Foundry needs `AZURE_OPENAI_API_KEY` exported. Both providers accept PDFs and images as inline data,
so uploads are sent to the model as-is — nothing extracts text first.

## Where the agents are

There are five, and each one is an interface, a system message and a return type. LangChain4j
generates the implementation, so the return type *is* the output schema — add a component to
`DocumentAnalysis` and the intake agent starts filling it in. Every agent is one line in
`config/AiServiceConfig.java`, except the Case Chat, which is the only one with tools and a memory.

They all write in English; see [ADR 0002](./docs/adr/0002-agents-write-in-english.md).

Intake — runs once, when a file is uploaded:

| | |
|---|---|
| `document/DocumentAnalyzer.java` | the agent |
| `document/DocumentAnalysis.java` | what it returns, and therefore what it is asked for |
| `document/DocumentIntake.java` | turns an upload into the file content the model reads |
| `document/DocumentStore.java` | in-memory; everything is lost on restart |
| `document/DocumentFiles.java` | the bytes, on disk, so an agent can look again ([ADR 0004](./docs/adr/0004-uploaded-files-are-kept-on-disk.md)) |

Case handling — what the handler's screen runs against one Case:

| | |
|---|---|
| `cases/Case.java` | Case Status, derived in Java rather than by a model |
| `cases/CaseSummarizer.java` | the expensive agent: what the Documents say, across all of them |
| `cases/DocumentForSummary.java` | what that agent is shown — and, deliberately, what it is not |
| `cases/CaseStatusWriter.java` | the cheap agent: derived facts in, one situation report out |
| `cases/CaseSummaryStore.java` | caches a summary against the Documents it was written over |
| `cases/CaseDesk.java` | the seam the screen talks to; the only caller of any handler-side agent |

Case Chat — one conversation per Case, with tools, that suggests and never writes:

| | |
|---|---|
| `cases/CaseChatAgent.java` | the agent: memory id, tools, and a `Result` so tool calls survive |
| `cases/CaseChatTools.java` | four tools, no logic — every one hands straight to `CaseDesk` |
| `cases/CaseAtAGlance.java` | what it starts with; `DocumentForChat` is one line of it |
| `cases/DocumentInDetail.java` | what the detail tool fetches — the half the index leaves out |
| `document/DocumentReader.java` | a second agent, given the file and no Case context at all |
| `cases/Proposal.java` | sealed: confirming one is a pattern switch that must stay exhaustive |
| `cases/DocumentRequest.java` | what a confirmed Proposal produces, and what a Claimant sees |

Guardrails and screening — what stops a Document talking the agent round. Each is a workshop task,
and the code sits under the task it belongs to:

| | |
|---|---|
| `tasks/task_1_guardrails/Guardrails.java` | **entrypoint** — the pair the intake agent is wired with |
| `tasks/task_1_guardrails/guardrails/` | the two guardrails: one on the way out, one on the way back |
| `tasks/task_1_guardrails/model/` | what the agent returns when a Document tries to give it orders |
| `tasks/task_2_postprocessing/FraudScreener.java` | **entrypoint** — runs every check; cannot refuse an upload and cannot throw |
| `tasks/task_2_postprocessing/checks/` | the seam, and three checks: duplicate bytes, image metadata, the agent's own report |
| `tasks/task_2_postprocessing/model/` | what a screening is, and the projection that keeps it off the upload screen |

Each task is one folder with one entrypoint. Nothing outside a task calls past it.

The briefs are [task 1](./docs/tasks/task_1_guardrails.md) and
[task 2](./docs/tasks/task_2_postprocessing.md); the three layers are demonstrated end to end in
[the walkthrough](./docs/guardrails-walkthrough.md), with drag-in files in [`assets/`](./assets).
Who sees what is [ADR 0005](./docs/adr/0005-fraud-signals-are-handler-side.md).

Nothing in either task needs credentials or a network: the checks read the bytes that are already in
hand.

The frontend is a folder per job, and every screen has an address:

| | |
|---|---|
| `frontend/src/App.tsx` | the route table, and nothing else |
| `frontend/src/routes/` | one file per screen: `/`, `/cases`, `/cases/:caseId`, `/casehandler`, `/casehandler/cases/:caseId` |
| `frontend/src/components/` | the pieces a screen is built from |
| `frontend/src/api/` | types, the fetch helpers, and one file per resource |
| `frontend/src/lib/` | labels, and the two questions the screens ask about a Document |
