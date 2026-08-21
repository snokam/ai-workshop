# The frontend

Two people use this application, and there is a screen for each. A claimant opens a case and
sends in documents. A case handler reads what came in, asks questions about it, and asks for
what is missing.

You do not write any of this during the workshop. It is here so that the agents you write have
somewhere to show up, and so that the parts that do not work yet say why.

## Where things are

```
src/
  pages/              one folder per person: file-claim/ and claim-handler/
  components/
    task_1_first_agent/       Checklist
    task_2_document_agent/    DocumentCard, standing
    task_4_fraud_detection/    Screening
    task_6_advisor_chat/              CaseChat, Turn, ProposalCard
    feedback/                 Loader, Failure, TaskNotDone
    layout/                   Layout, framework
    workshop/                 TaskGate, TasksProvider
  api/                one file per group of endpoints, and the types they return
  lib/                labels and the task state the gates read
```

Components are grouped by the task that brings them to life, the same way the backend is: the
screening panel appears when task 4 is written, the chat when task 6 is. Pages are not, because a
page is a screen rather than a task — the handler's case screen shows the work of six of them at
once, and splitting it six ways would make it harder to read, not easier.

Anything under `feedback/`, `layout/` or `workshop/` belongs to no task. It is the scaffolding
that holds the workshop together.

## What happens before you write anything

The application runs from the first minute, with every agent unwritten. Type into the boxes,
press the buttons, upload a file. Each thing you try fails in the place it would work, and says
which task makes it work and which file to open.

That is `TaskGate`. It asks the backend which tasks are done — derived from your code, not from
a flag anyone remembers to set — and puts the instruction next to the control rather than in
place of it. The controls stay live on purpose. Being told what to write is not the same as
watching the thing you tried come back empty.

## Running it

```
npm install
npm run dev
```

It expects the backend on port 8080. `npm run build` typechecks and bundles.
