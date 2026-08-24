# Task 3 — Give it a file

You write the agent that is handed a file, and the part of its output schema that has a
decision in it.

The brief is `docs/tasks/task_3_document_agent.md`, from the repository root.

## The parts

Do them in this order. Each is one file, and the `TODO` at the top of it has the steps.

| | File | What it is for |
|---|---|---|
| 1 | [`agent/DocumentAnalyzer.java`](./agent/DocumentAnalyzer.java) | Write the agent |
| 2 | [`model/DocumentAnalysis.java`](./model/DocumentAnalysis.java) | Say what three of the fields mean |

## Everything else

Two files sit at the top of the folder, and neither is yours to edit.

- [`DocumentIntake.java`](./DocumentIntake.java) — given, and the first thing to read. One method:
  save the bytes, show them to the agent, write down what it said, announce it. The two lines in the
  middle are the whole idea of the task — the file goes to the model as a file.
- [`DocumentsController.java`](./DocumentsController.java) — the HTTP surface. Upload, list, fetch
  the file back, mark one reviewed.

The rest is grouped by what it is for, so it stays out of the way:

| Folder | What is in it |
|---|---|
| [`agent/`](./agent) | the analyser (part 1) and its wiring |
| [`model/`](./model) | what the agent returns (part 2) and what the screens are sent |
| [`store/`](./store) | where documents and their bytes are kept, what type a file is, and what its bytes hash to |
| [`progress/`](./progress) | what the uploads add up to: whether the case has what it asked for, and a handler's override when the agent calls a document unreadable |
