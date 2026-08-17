# AI workshop — document handling

Upload a document, and an agent reads it: what kind of document it is, the facts worth pulling out
of it, and whether the file is legible enough to work with.

The domain language lives in [CONTEXT.md](./CONTEXT.md), and the workshop exercises in
[tasks/](./tasks).

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

There are three, and each one is an interface, a system message and a return type. LangChain4j
generates the implementation, so the return type *is* the output schema — add a component to
`DocumentAnalysis` and the intake agent starts filling it in. Every agent is one line in
`config/AiServiceConfig.java`.

They all write in English; see [ADR 0002](./docs/adr/0002-agents-write-in-english.md).

Intake — runs once, when a file is uploaded:

| | |
|---|---|
| `document/DocumentAnalyzer.java` | the agent |
| `document/DocumentAnalysis.java` | what it returns, and therefore what it is asked for |
| `document/DocumentIntake.java` | turns an upload into the file content the model reads |
| `document/DocumentStore.java` | in-memory; everything is lost on restart |

Case handling — what the handler's screen runs against one Case:

| | |
|---|---|
| `cases/Case.java` | Case Status, derived in Java rather than by a model |
| `cases/CaseSummarizer.java` | the expensive agent: what the Documents say, across all of them |
| `cases/DocumentForSummary.java` | what that agent is shown — and, deliberately, what it is not |
| `cases/CaseStatusWriter.java` | the cheap agent: derived facts in, one situation report out |
| `cases/CaseSummaryStore.java` | caches a summary against the Documents it was written over |
| `cases/CaseDesk.java` | the seam the screen talks to; the only caller of either agent |

Guardrails and screening — what stops a Document talking the agent round:

| | |
|---|---|
| `guardrail/UploadedFileGuardrail.java` | input guardrail: only our sentence and one file reach the model |
| `guardrail/AnalysisGuardrail.java` | output guardrail: a match must be a Required Document this Case asked for |
| `fraud/FraudCheck.java` | the seam — a new check is a new `@Component` and nothing else |
| `fraud/DuplicateUploadCheck.java` | the same bytes, seen before, on this Case or another |
| `fraud/ImageMetadataCheck.java` | EXIF: editing software, no camera origin, a capture date out of place |
| `fraud/ReverseImageCheck.java` | is this picture already published online |
| `fraud/AddressedTheAgentCheck.java` | the intake agent's own report of a Document that gave it orders |
| `fraud/FraudScreener.java` | runs them all; cannot refuse an upload and cannot throw |

Guardrails are [task 1](./tasks/task_1_guardrails). The three layers are demonstrated in
[the walkthrough](./docs/guardrails-walkthrough.md), with drag-in files in [`assets/`](./assets). Who sees what is [ADR 0003](./docs/adr/0003-fraud-signals-are-handler-side.md).

Reverse image search is off by default — it is the only thing here that sends a file anywhere but the
model provider. To switch it on, once per project:

```bash
gcloud services enable vision.googleapis.com --project "$GOOGLE_CLOUD_PROJECT"
FRAUD_REVERSE_IMAGE=vision AI_PROVIDER=vertex ./mvnw spring-boot:run
```

The first thousand images a month are free, which is more than a workshop will use.

| | |
|---|---|
| `frontend/src/App.tsx` | both screens |
