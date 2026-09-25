# AI workshop

You build the AI parts of an insurance claim system.

A customer writes what happened to them in their own words. Your agents read that text, decide what
kind of claim it is, check the documents the customer uploads, write a summary for the claims
handler, and answer the handler's questions about the claim.

Along the way you learn what matters when an LLM goes into a real application: guardrails,
evaluation, picking the right model for the job, tools, memory and streaming.

The code is mostly written. Your job is to fill in the missing pieces, in seven tasks. The
application runs from the start — every screen works, and anything that needs a task you have not
done yet tells you which file to open.

## The tasks

| | | what you do |
|---|---|---|
| 1 | [Your first agent](./backend/src/main/java/com/example/aiworkshop/tasks/task_1_first_agent/README.md) | Send the user's description to an LLM with a prompt and the five insurance types, and open a claim of the type it picks. |
| 2 | [Guardrails](./backend/src/main/java/com/example/aiworkshop/tasks/task_2_guardrails/README.md) | Block text that is not a claim, and text that tries to give the system instructions. |
| 3 | [Reading documents](./backend/src/main/java/com/example/aiworkshop/tasks/task_3_document_agent/README.md) | Describe the fields you want back, and send the model a PDF or a photo. |
| 4 | [Evaluation](./backend/src/main/java/com/example/aiworkshop/tasks/task_4_evaluation/README.md) | Build test sets and measure the agents you wrote in tasks 1 and 2. |
| 5 | [Summaries, and choosing a model](./backend/src/main/java/com/example/aiworkshop/tasks/task_5_claim_summary_choosing_models/README.md) | Pick which model each of two agents should use, after seeing what the calls cost. |
| 6 | [Chat with tools and memory](./backend/src/main/java/com/example/aiworkshop/tasks/task_6_advisor_chat_with_tools_and_memory/README.md) | Describe your tools so the model knows when to use them, and give each claim its own conversation. |
| 7 | [A form that helps while you write](./backend/src/main/java/com/example/aiworkshop/tasks/task_7_dynamic_form_with_streaming/README.md) | Send the model's answer to the browser word by word, while the user is still typing. |

## Layout

```
backend/    Spring Boot, Java 25 — the API and every agent, one folder per task
frontend/   Vite and React — the two screens
assets/     files to drag into the app
```

## Before the day

Four things, and one of them you may already have.

| | |
|---|---|
| Java 25 | `java -version` should say 25. Most machines are still on 17 or 21. |
| Node 20 or newer | for the frontend |
| A terminal each | the two halves run separately |
| An Azure AI Foundry key | handed out at the start of the workshop |

The workshop runs on Azure AI Foundry, against the `ai-wshp-p` resource. One environment variable,
and it is read once at startup — so if you change it, restart the backend:

```bash
export AZURE_OPENAI_API_KEY=...        # the key you were given
```

Nothing else needs setting. The endpoint and the deployment have defaults that point at the right
resource, and every task picks them up from there.

## Running it

Two terminals.

```bash
# terminal 1 — backend on :8080
cd backend
./mvnw spring-boot:run

# terminal 2 — frontend on :5173
cd frontend
pnpm install && pnpm dev
```

Then open http://localhost:5173. If the backend starts but every call comes back a 502, the key is
missing or wrong — that is the only thing that has to be set.

## Choosing a provider

`aiworkshop.model.provider` picks which `ChatModel` bean is built. Foundry is the default and is
what the tasks are written against. Vertex AI is wired up too, already written, as the demonstration
that none of the code above the model cares which one it is:

```bash
cd backend
AI_PROVIDER=vertex ./mvnw spring-boot:run
```

Vertex authenticates with Application Default Credentials rather than a key — `gcloud auth
application-default login` — and takes the project from those same credentials, so nothing has to be
exported. To bill a different project than the one you signed in against, say so:

```bash
AI_PROVIDER=vertex GOOGLE_CLOUD_PROJECT=your-project ./mvnw spring-boot:run
```

Both providers accept PDFs and images as inline data, so uploads are sent to the model as-is —
nothing extracts text first. Model names differ between them, and task 5 is where that shows up:
naming one provider's model while running the other is an error, not a substitution.

## Where everything is

```
backend/src/main/java/com/example/aiworkshop/
  tasks/task_*/     one folder per task: the agent, the records it answers in, what is kept,
                    and the endpoints the screen calls. The test tree mirrors it.
  workshop/         works out which tasks are done by probing your code, not by reading a flag

frontend/src/
  pages/            one folder per person: file-claim/ and claim-handler/
  components/       task_* folders group by the task that brings them to life; the rest is scaffolding
  api/              one file per group of endpoints, and the types they return
```

Each task folder has its own README naming the files to open, in order. They are linked from
[The tasks](#the-tasks) above, and that is where the detail lives — this file does not repeat it.
