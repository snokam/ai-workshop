# Tasks

Eight exercises, in order. Each adds one idea to the one before it, and the numbering is the
order to do them in.

| | | you write |
|---|---|---|
| 1 | [Your first agent](./task_1_first_agent.md) | the SDK wiring, and an agent that reads a sentence and decides which claim to open |
| 2 | [Is this even a claim?](./task_2_guardrails.md) | two guardrails, before the call — refusing text with no claim in it, and text addressed to the software |
| 3 | [Give it a file](./task_3_document_agent.md) | an agent handed a PDF or a photograph, and a record that is its output schema |
| 4 | [How would you know?](./task_4_evaluation.md) | two sets, two evaluations — one over the classifier from task 1, one over the guardrails from task 2 |
| 5 | [Claim: Claim summary](./task_5_claim_summary.md) | the expensive agent: every document at once, what it is shown, and what that costs |
| 6 | [Claim: Advisor chat](./task_6_advisor_chat.md) | an agent that looks things up mid-answer, and the tool descriptions that decide when it does |
| 7 | [Claim: File claim with AI chat](./task_7_create_claim_chat.md) | an intake agent that may ask before it commits |


The first four build and check a single agent: write one, put a guardrail in front of it, hand it a
file, then ask whether any of it works. Tasks 5 to 7 are the claim itself — what happens to a claim
once documents start arriving, and the agents a handler needs once there is something to handle.

Five comes before six because the advisor chat is shown the summary the claim summary writes.
Seven stands beside task 1 rather than after it: the quick report screen keeps working untouched.

## Three hours

The seven add up to about five hours of writing, which is more than a session holds. They
are ordered so that stopping anywhere leaves something whole:

| | |
|---|---|
| 0:00 | set up, and run both halves with nothing written |
| 0:15 | **task 1** — the model, the agent, and the claim its answer opens |
| 1:15 | **task 2** — one guardrail, before the call, and what it saves |
| 2:15 | break |
| 2:25 | **task 3** — the same again with a file |
| 3:00 | where this goes: evaluation, reading across documents, tools, memory |

That is 1 to 3 done properly. Tasks 4 to 7 are read rather than written, and left for afterwards —
the repository is theirs to finish in their own time, and the briefs assume no one is standing over
them.

Do not try to fit more in. Task 1 and task 2 are where the ideas are: an interface that is an agent,
a record that is the schema, a file that goes to the model as itself, and the moment a model's
answer becomes the shape of someone's claim. Rushing those to reach the ones that demo better trades
the part that transfers for the part that impresses.

If the room is quick, **task 4** is the better one to add rather than 5 — it is the shortest, and it
makes the point that the interesting work after a model answers is not AI at all.

## How the repository behaves while you work

Nothing is finished, and the application runs anyway.

Start both halves before writing a line. Every screen works, and the ones waiting on a task you have
not done say so — which file to open, and which brief. The controls stay live: type the description,
press the button, and watch it fail with the same answer. Using the thing that is missing is half of
what a task is for.

```bash
cd backend && ./mvnw test -Dtest=TaskCompletionTest
```

One test per exercise, red until you write it. That is the progress bar: run it whenever you
want to know what is left.

Nothing marks a task done. An agent is unwritten while its `@SystemMessage` still holds the
paragraph it shipped with, and everything else is unwritten while it still throws
`TaskNotImplementedException`. Both are the code saying so, so there is no flag to forget and
nothing that can disagree with what you actually wrote.

Each task also has tests of its own next to the code, checking what it does rather than that it
exists. `TaskCompletionTest` going green means you set a flag; those going green means it works.

## Where the code for a task is

Each task folder holds what you write: the agent, its wiring, and the records its agent defines the
shape of. Every folder has a `README.md` saying which files are yours and which packages it reaches
into.

What it reaches into stays outside, and that is deliberate. `Claim` and `UploadedDocument` and their
stores are used by most of the seven tasks each — they are what the application *is*, and they would
exist if there were no workshop. An agent is what a task *is*. Moving the domain inside the tasks
would mean task 5 depending on task 1 for the idea of a claim, which is not what it depends on at
all.

The one rule the layout does enforce: **a task may depend only on earlier tasks.** There are no
exceptions, which is why task 2 contributes its guardrail to task 1's agent rather than task 1
fetching it, and why task 5's summary is handed to task 6's chat rather than the chat reaching back
for documents. Both are better designs than the ones they replaced, and neither was chosen for
tidiness.

## The finish line

Green tests mean you wired it, not that it works. When you think you are done, do this, and do it
with the application running:

1. Open a motor claim: *"Someone reversed into my parked car outside the shop and I paid for the
   repair myself."* You want `Motor insurance claim`, `HIGH`, and a checklist of three.
2. Upload `assets/repair-receipt.pdf` to it. The card should name the amount that is actually on the
   receipt, in the currency it is printed in, and say something specific about the scan.
3. Open a second, unrelated claim — a theft, say — and upload **the same receipt** to it. The claim
   handler's screen must say the same file has been seen on another claim. One expense, two claims,
   which is the oldest trick there is.
4. Upload `assets/document-that-gives-orders.pdf` anywhere. The claimant's screen must not tell them
   which of their tricks was noticed; the handler's must.
5. Ask the chat *"what is the total on the receipt?"* on a claim that has no receipt, and read what
   it says rather than what you hoped it would say.
6. Run [task 4](./task_4_evaluation.md) and sort the disagreements.

Steps 3 and 4 are the ones worth caring about. They are the two places where this application does
something a model cannot do for you, and if either is wrong the rest being right does not help.

## What is not here

Named on purpose, because "AI workshop" sets expectations and it is better to say than to let you
notice halfway through.

**No RAG, and no vector search.** Not an oversight and not a shortcut: the documents in this
application arrive with the request. Someone uploads a file and the agent is handed that file. There
is nothing to retrieve, so a retrieval step would be ceremony added to teach a technique rather than
to solve the problem in front of it. If your documents are already somewhere and have to be found
first, that is a different application and RAG is where you would start.

**No fine-tuning.** Everything here is a prompt and a schema against a general model, which is where
almost every application should start and where a good many should stop.

**No streaming, no async.** Both matter for how an agent feels to use, and neither changes what it
is.

**One provider, one framework.** You will see Vertex and LangChain4j and nothing else, which is the
cost of three hours. [Task 1](./task_1_first_agent.md) has a section on which half of what you are
looking at is LangChain4j and which half is the idea — read it, because that is the part that
transfers.

**Evaluation gets twenty-five minutes**, in [task 4](./task_4_evaluation.md), which is enough to see
why it matters and nowhere near enough to do it properly.

## The answers

A worked version of all six is on the **`solutions`** branch, which is this repository with every
task written.

```bash
git show solutions:backend/src/main/java/com/example/aiworkshop/tasks/task_1_first_agent/agent/ClaimTypeClassifier.java
```

Look after you have tried, not before. An afternoon lost to a compiler error teaches nobody anything
about agents, so if you are stuck on the plumbing rather than the thinking, go and read it.

The domain language every task uses is in [CONTEXT.md](../../CONTEXT.md). The decisions behind the
application, including the rejected ones, are in [docs/adr](../adr).
