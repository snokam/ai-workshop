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
| 5 | [Claim summary, and which model each job needs](./backend/src/main/java/com/example/aiworkshop/tasks/task_5_claim_summary_choosing_models/README.md) | which of two models each of its two agents needs, decided by reading what the calls cost |
| 6 | [Advisor chat with tools and memory](./backend/src/main/java/com/example/aiworkshop/tasks/task_6_advisor_chat_with_tools_and_memory/README.md) | tool descriptions that decide when a tool is called, the call that hands them over, and one conversation per claim |
| 7 | [File a claim with a streaming chat](./backend/src/main/java/com/example/aiworkshop/tasks/task_7_streaming_file_claim_chat/README.md) | an intake agent that may ask before it commits, and its answer arriving as it is written |

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

```
backend/src/main/java/com/example/aiworkshop/
  tasks/task_*/     one folder per task: the agent, the records it answers in, what is kept,
                    and the endpoints the screen calls. The test tree mirrors it.
  workshop/         works out which tasks are done by probing your code, not by reading a flag
  config/           the Foundry provider, for Snokam staff who use it instead of Vertex

frontend/src/
  pages/            one folder per person: file-claim/ and claim-handler/
  components/       task_* folders group by the task that brings them to life; the rest is scaffolding
  api/              one file per group of endpoints, and the types they return
```

Each task folder has its own README naming the files to open, in order. They are linked from
[The tasks](#the-tasks) above, and that is where the detail lives — this file does not repeat it.
