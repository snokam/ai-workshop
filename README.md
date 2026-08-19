# AI workshop — document handling

Upload a document, and an agent reads it: what kind of document it is, the facts worth pulling out
of it, and whether the file is legible enough to work with.

The domain language lives in [CONTEXT.md](./CONTEXT.md), and the workshop exercises in
[docs/tasks/](./docs/tasks).

**Nothing is finished, and it runs anyway.** The application starts with every agent unwritten:
each screen works, the controls stay live, and the ones needing a task you have not done say which
file to open. Eight exercises, in order, each one an agent or the thing that keeps an agent honest.

## Layout

```
backend/    Spring Boot, Java 25 — the API and every agent, one folder per task
frontend/   Vite and React — the two screens
docs/       the task briefs, the walkthrough, and the ADRs
assets/     files to drag into the app
```

The two halves are built and run separately. Maven does not build the frontend and never will:
Vite proxies `/api` to Spring Boot, so the browser sees a single origin and there is no CORS
configuration anywhere.

## Running it

Two terminals.

```bash
# terminal 1 — backend on :8080
cd backend
export AZURE_OPENAI_API_KEY=...        # or use the Vertex provider, see below
./mvnw spring-boot:run

# terminal 2 — frontend on :5173
cd frontend
npm install && npm run dev
```

Then open http://localhost:5173.

## Choosing a provider

`aiworkshop.model.provider` picks which `ChatModel` bean is built. Override per run:

```bash
cd backend
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

## Where everything is

The backend is eight folders, one per task, under
`backend/src/main/java/com/example/aiworkshop/tasks/`. Each holds everything that task is: the
agent you write, the records it answers in, what is kept, and the endpoints the screen calls.

| | |
|---|---|
| `task_1_first_agent/` | the model itself, a classifier, and the case its answer opens |
| `task_2_document_agent/` | the agent that reads an uploaded PDF or photo |
| `task_3_guardrails/` | one guardrail on the way out, one on the way back |
| `task_4_postprocessing/` | plain Java after the answer: what the model cannot know |
| `task_5_summary/` | the expensive agent, across every document on a case |
| `task_6_chat/` | tools and memory — the agent that suggests and never writes |
| `task_7_create_case_chat/` | an interview instead of a form |
| `task_8_evaluation/` | how you would know any of it works |

Inside a task the shape is always the same:

```
task_2_document_agent/
  DocumentsController.java     the endpoints
  DocumentIntake.java          the code that calls the agent
  agent/                       the agent and its wiring — this is what you write
  model/                       the records it answers in
  store/                       what is kept
```

Each task has its own README naming what is yours to write and what it uses from the tasks before
it. The test tree mirrors the same folders.

**A task may use the tasks before it and never the ones after it.** That is what lets you stop
after any exercise and still have something that runs. When a later task needs to change how an
earlier one behaves it contributes rather than calls: task 2 answers task 1's `CaseProgress`, task
3 hands its guardrails to task 2 as beans, task 4 listens for task 2's event. `TaskDependencyTest`
fails if the rule is ever broken, and `WorkshopTaskTest` fails if a brief points at a file that has
moved.

Outside the tasks there is only `workshop/`, which works out which exercises are done by probing
your code rather than reading a flag, and `config/`, which is the Foundry provider Snokam staff
use instead of Vertex.

### The agents

Seven, and each one is an interface, a system message and a return type. LangChain4j generates the
implementation, so the return type *is* the output schema — add a component to `DocumentAnalysis`
and the agent starts filling it in. They all write in English; see
[ADR 0002](./docs/adr/0002-agents-write-in-english.md).

| | |
|---|---|
| `task_1_first_agent/agent/CaseTypeClassifier.java` | a sentence in, a case type out |
| `task_2_document_agent/agent/DocumentAnalyzer.java` | the file itself, sent as inline data |
| `task_5_summary/agent/CaseSummarizer.java` | the expensive one: every document, in one prompt |
| `task_5_summary/agent/CaseStatusWriter.java` | the cheap one: derived facts in, one situation report out |
| `task_6_chat/agent/CaseChatAgent.java` | memory id, tools, and a `Result` so tool calls survive |
| `task_6_chat/agent/DocumentReader.java` | a second agent, given the file and no case context at all |
| `task_7_create_case_chat/agent/CaseIntakeInterviewer.java` | asks until it has enough to open a case |

Nothing in tasks 3 or 4 calls a model. Guardrails run around the call and the checks run after it,
on bytes already in hand — no credentials, no network. The three layers are shown end to end in
[the walkthrough](./docs/guardrails-walkthrough.md), with files to drag in under
[`assets/`](./assets). Who is allowed to see a fraud signal is
[ADR 0005](./docs/adr/0005-fraud-signals-are-handler-side.md).

### The frontend

```
frontend/src/
  pages/                    one folder per person: file-claim/ and claim-handler/
  components/task_*/        grouped by the task that brings them to life
  components/{feedback,layout,workshop}/   scaffolding, belonging to no task
  api/                      one file per group of endpoints, and the types they return
  lib/                      labels, and the task state every screen reads
```

Components follow the tasks the way the backend does. Pages do not, because a page is a screen
rather than a task: the handler's case screen shows the work of six of them at once.

A screen that needs an unwritten task keeps working. The controls stay live, the explanation sits
under them, and using them fails with the file to open — being told what to write is not the same
as watching the thing you tried come back empty.
