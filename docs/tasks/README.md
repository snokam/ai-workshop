# Tasks

Six exercises, in order. Each one is an agent, and each adds exactly one idea to the one before it.

| | | you write |
|---|---|---|
| 1 | [Your first agent](./task_1_first_agent.md) | the SDK wiring, and an agent that reads a sentence and decides which case to open |
| 2 | [Give it a file](./task_2_document_agent.md) | an agent handed a PDF or a photograph, and a record that is its output schema |
| 3 | [Guardrails](./task_3_guardrails.md) | two checks on the way out and back, now there is an agent worth guarding |
| 4 | [Post-processing](./task_4_postprocessing.md) | what the model cannot know, in plain Java after the answer |
| 5 | [Tools and memory](./task_5_chat.md) | an agent that looks things up mid-answer and remembers the conversation |
| 6 | [Across documents](./task_6_summary.md) | the expensive agent: every document at once, and what that costs |

1 and 2 build an agent. 3 and 4 contain one. 5 and 6 grow one.

## Three hours

The six add up to about four and a quarter hours of writing, which is more than a session holds.
They are ordered so that stopping anywhere leaves something whole:

| | |
|---|---|
| 0:00 | set up, and run both halves with nothing written |
| 0:15 | **task 1** — the model, the agent, and the case its answer opens |
| 1:05 | **task 2** — the same again with a file, which is where it gets interesting |
| 1:55 | break |
| 2:05 | **task 3** — what happens when a document argues back |
| 2:40 | **task 4** — what the model cannot know |
| 3:05 | where this goes: tools, memory, and reading across documents |

That is 1 to 4 done properly, with 5 and 6 read rather than written and left for afterwards. Do not
try to fit six in: task 1 is where the ideas are, and rushing it to reach task 6 trades the part
that transfers for the part that impresses.

If the room is quick, 5 is the better of the two to add — it is the one that changes what an agent
*is*, from something you call to something that decides what to look at.

## How the repository behaves while you work

Nothing is finished, and the application runs anyway.

Start both halves before writing a line. Every screen works, and the ones waiting on a task you have
not done say so — which file to open, and which brief. The controls stay live: type the description,
press the button, and watch it fail with the same answer. Using the thing that is missing is half of
what a task is for.

```bash
cd backend && ./mvnw test -Dtest=TaskCompletionTest
```

Six tests, one per exercise, red until you write it. That is the progress bar: run it whenever you
want to know what is left.

Each task also has tests of its own next to the code, checking what it does rather than that it
exists. `TaskCompletionTest` going green means you set a flag; those going green means it works.

## The answers

A worked version of all six is on the **`solutions`** branch, which is this repository with every
task written.

```bash
git show solutions:backend/src/main/java/com/example/aiworkshop/tasks/task_1_first_agent/CaseTypeClassifier.java
```

Look after you have tried, not before. An afternoon lost to a compiler error teaches nobody anything
about agents, so if you are stuck on the plumbing rather than the thinking, go and read it.

The domain language every task uses is in [CONTEXT.md](../../CONTEXT.md). The decisions behind the
application, including the rejected ones, are in [docs/adr](../adr).
