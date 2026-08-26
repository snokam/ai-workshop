# Task 3 — Reading documents

Describe the fields you want back, and send the model a PDF or a photo.

Someone uploads a document to their claim — a receipt, a police report, a photo of a dented door.
The file goes to the model as it is, and the model answers with what kind of document it is, the
facts worth pulling out, which item on the claim's checklist it satisfies, and whether it is legible
enough to use.

You learn two things. A model reads files, not only text, so nothing has to pull the text out first.
And the way to get a usable answer back is to say what form each field should take: "the kind of
document" gets you a paragraph, "a short noun phrase, e.g. 'invoice'" gets you a label.

## The parts

Do them in this order. Each is one file, and the `TODO` at the top of it has the steps.

| | File | What it is for |
|---|---|---|
| 1 | [`model/DocumentAnalysis.java`](./model/DocumentAnalysis.java) | Say what two of the fields mean |
| 2 | [`DocumentIntake.java`](./DocumentIntake.java) | Send the file as itself |

## Everything else

Two files sit at the top of the folder. One of them is part 2.

- [`DocumentIntake.java`](./DocumentIntake.java) — part 2. One method does everything that happens
  to an upload; what you write is `promptFor`, the content list the model is sent.
- [`DocumentsController.java`](./DocumentsController.java) — the HTTP surface. Upload, list, fetch
  the file back, mark one reviewed.

The rest is grouped by what it is for, so it stays out of the way:

| Folder | What is in it |
|---|---|
| [`agent/`](./agent) | the analyser and its wiring. Its `@SystemMessage` is given and short on purpose — nearly everything a longer one would say belongs on the field descriptions instead, and saying it twice is how the two drift apart |
| [`model/`](./model) | what the agent returns (part 1) and what the screens are sent |
| [`store/`](./store) | where documents and their bytes are kept, what type a file is, and what its bytes hash to |
| [`progress/`](./progress) | what the uploads add up to: whether the claim has what it asked for, and a handler's override when the agent calls a document unreadable |
