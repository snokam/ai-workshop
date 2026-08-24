# AI workshop — document handling

Upload a document, and an agent reads it: what kind of document it is, the facts worth pulling out
of it, and whether the file is legible enough to work with.

The domain language lives in [CONTEXT.md](./CONTEXT.md). The exercises live beside the code they
change — one README per task folder, listed below.

**Nothing is finished, and it runs anyway.** The application starts with every agent unwritten:
each screen works, the controls stay live, and the ones needing a task you have not done say which
file to open. Seven exercises, in order, each one an agent or the thing that keeps an agent honest.

## The tasks

| | | you write |
|---|---|---|
| 1 | [Your first agent](./backend/src/main/java/com/example/aiworkshop/tasks/task_1_first_agent/README.md) | the SDK wiring, an agent that reads a sentence, and the claim its answer opens |
| 2 | [Is this even a claim?](./backend/src/main/java/com/example/aiworkshop/tasks/task_2_guardrails/README.md) | two guardrails, before the call — refusing text with no claim in it, and text addressed to the software |
| 3 | [Give it a file](./backend/src/main/java/com/example/aiworkshop/tasks/task_3_document_agent/README.md) | the output schema's descriptions, and the two lines that hand the model a PDF or a photograph |
| 4 | [How would you know?](./backend/src/main/java/com/example/aiworkshop/tasks/task_4_evaluation/README.md) | two sets, two evaluations — over the classifier from task 1 and the guardrails from task 2 |
| 5 | [Claim: Claim summary](./backend/src/main/java/com/example/aiworkshop/tasks/task_5_claim_summary/README.md) | the expensive agent: every document at once, and what that costs |
| 6 | [Claim: Advisor chat](./backend/src/main/java/com/example/aiworkshop/tasks/task_6_advisor_chat/README.md) | tool descriptions that decide when a tool is called, and the call that hands them over |
| 7 | [Claim: File claim with AI chat](./backend/src/main/java/com/example/aiworkshop/tasks/task_7_create_claim_chat/README.md) | an intake agent that may ask before it commits |

Each task's README sits in its own folder, beside the code, and names the files to open in order.
The `TODO` at the top of each of those files has the steps.

The first four build and check a single agent: write one, put a guardrail in front of it, hand it a
file, then ask whether any of it works. Tasks 5 to 7 are the claim itself — what happens once
documents start arriving, and the agents a handler needs once there is something to handle.

Five comes before six because the advisor chat is shown the summary the claim summary writes. Seven
stands beside task 1 rather than after it: the quick report screen keeps working untouched.

### Three hours

The seven add up to about five hours of writing, which is more than a session holds. They are
ordered so that stopping anywhere leaves something whole:

| | |
|---|---|
| 0:00 | set up, and run both halves with nothing written |
| 0:15 | **task 1** — the model, the agent, and the claim its answer opens |
| 1:15 | **task 2** — two guardrails, before the call, and what they save |
| 2:15 | break |
| 2:25 | **task 3** — the same again with a file |
| 3:00 | where this goes: evaluation, reading across documents, tools, memory |

That is 1 to 3 done properly. Tasks 4 to 7 are read rather than written, and left for afterwards —
the repository is theirs to finish in their own time.

Do not try to fit more in. Tasks 1 and 2 are where the ideas are: an interface that is an agent, a
record that is the schema, a file that goes to the model as itself, and the moment a model's answer
becomes the shape of someone's claim.

## Layout

```
backend/    Spring Boot, Java 25 — the API and every agent, one folder per task
frontend/   Vite and React — the two screens
assets/     files to drag into the app
```

The two halves are built and run separately. Maven does not build the frontend and never will:
Vite proxies `/api` to Spring Boot, so the browser sees a single origin and there is no CORS
configuration anywhere.

## Before the day

Four things, and one of them you may already have.

| | |
|---|---|
| Java 25 | `java -version` should say 25. Most machines are still on 17 or 21. |
| Node 20 or newer | for the frontend |
| A terminal each | the two halves run separately |
| Google Cloud credentials | see below — Storebrand developers already have these |

**If you are at Storebrand and use Storecode**, sign in to Claude Code the way you always do. That
sign-in leaves Application Default Credentials on your machine, which is exactly what this workshop
authenticates with — so there is nothing else to set up and nothing to paste. Clone, run, and it
works.

**If you are not**, one command, once:

```bash
gcloud auth application-default login
```

Google asks for this again every day or two. When it expires the application says
`UNAUTHENTICATED: Failed computing credential metadata` — run the command again and **restart the
backend**, because credentials are read once at startup.

## Running it

Two terminals.

```bash
# terminal 1 — backend on :8080
cd backend
./mvnw spring-boot:run

# terminal 2 — frontend on :5173
cd frontend
npm install && npm run dev
```

Then open http://localhost:5173. No environment variables, no API keys: Vertex AI is the default
and it finds your credentials on its own.

## Choosing a provider

`aiworkshop.model.provider` picks which `ChatModel` bean is built. Vertex is the default, so the
only reason to touch this is if you are Snokam staff on Foundry:

```bash
cd backend
AI_PROVIDER=foundry AZURE_OPENAI_API_KEY=... ./mvnw spring-boot:run
```

Vertex authenticates with Application Default Credentials rather than an API key, and takes the
project from those same credentials — which is why nothing has to be exported. To bill a different
project than the one you signed in against, say so:

```bash
GOOGLE_CLOUD_PROJECT=your-project ./mvnw spring-boot:run
```

Both providers accept PDFs and images as inline data, so uploads are sent to the model as-is —
nothing extracts text first.

## Where everything is

The backend is eight folders, one per task, under
`backend/src/main/java/com/example/aiworkshop/tasks/`. Each holds everything that task is: the
agent you write, the records it answers in, what is kept, and the endpoints the screen calls.

| | |
|---|---|
| `task_1_first_agent/` | the model itself, a classifier, and the claim its answer opens |
| `task_2_guardrails/` | one guardrail, before the call — refusing text nobody could open a claim from |
| `task_3_document_agent/` | the agent that reads an uploaded PDF or photo |
| `task_4_evaluation/` | the two sets the evaluations run over: `./mvnw test -Pevaluate` |
| `task_5_claim_summary/` | the expensive agent, across every document on a claim |
| `task_6_advisor_chat/` | tools and memory — the agent that suggests and never writes |
| `task_7_create_claim_chat/` | an interview instead of a form |

Inside a task the shape is always the same:

```
task_3_document_agent/
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
earlier one behaves it contributes rather than calls: task 2 answers task 1's `ClaimProgress`, task
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
[CONTEXT.md](./CONTEXT.md).

| | |
|---|---|
| `task_1_first_agent/agent/ClaimTypeClassifier.java` | a sentence in, a claim type out |
| `task_3_document_agent/agent/DocumentAnalyzer.java` | the file itself, sent as inline data |
| `task_5_claim_summary/agent/ClaimSummarizer.java` | the expensive one: every document, in one prompt |
| `task_5_claim_summary/agent/ClaimStatusWriter.java` | the cheap one: derived facts in, one situation report out |
| `task_6_advisor_chat/agent/ClaimChatAgent.java` | memory id, tools, and a `Result` so tool calls survive |
| `task_6_advisor_chat/agent/DocumentReader.java` | a second agent, given the file and no claim context at all |
| `task_7_create_claim_chat/agent/ClaimIntakeInterviewer.java` | asks until it has enough to open a claim |

An eighth lives in task 5, `SummaryJudge`, and is not part of the application. It is a model asked
whether another model's answer holds up, which is the only way to score prose at any volume and the
technique in here most easily used badly.

Nothing in tasks 3 or 4 calls a model. Guardrails run around the call and the checks run after it,
on bytes already in hand — no credentials, no network. The three layers are shown end to end in
[`assets/`](./assets).

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
rather than a task: the handler's claim screen shows the work of six of them at once.

A screen that needs an unwritten task keeps working. The controls stay live, the explanation sits
under them, and using them fails with the file to open — being told what to write is not the same
as watching the thing you tried come back empty.
