# Task 3 — Give it a file

Task 1's agent read a sentence someone typed. This one is handed a PDF or a photograph and has to
say what it is looking at.

**Time:** 60 minutes. **You need:** task 1 working, and the files in [`assets/`](../../assets).

The parts, in order, are in `backend/src/main/java/com/example/aiworkshop/tasks/task_3_document_agent/README.md` — each one names the file, what it is for, and what to reach for.

## The one idea

Nothing extracts text from the file first.

The bytes go to the model as they are, as `PdfFileContent` or `ImageContent`, and the model reads
the document the way a person does. That is not a shortcut — it is the only way part of the job is
possible at all. You are asking whether a scan is legible, whether the total is cut off, whether it
looks like what it claims to be. Run OCR first and a crisp scan and a blurry one produce the same
text, and every one of those questions becomes unanswerable.

Open
`backend/src/main/java/com/example/aiworkshop/tasks/task_3_document_agent/agent/DocumentAnalyzer.java`:

```java
DocumentAnalysis analyse(@UserMessage List<Content> document,
                         @V("requiredDocuments") List<String> requiredDocuments);
```

`@UserMessage` on the argument is what makes the file *be* the user message. Leave it off and
LangChain4j looks for a message template instead, and the file is never sent — with no error, which
is the annoying part. The Claim's required documents come in through `@V` for the same reason as
task 1: they belong in the instructions, not in the turn carrying untrusted content.

## Part 1 — say what the fields mean

`tasks/task_3_document_agent/model/DocumentAnalysis.java`. Seven components, five of them described
for you. Two are not.

Nothing parses the model's reply. LangChain4j derives the output format from this record, so
`@Description` is not a note for the next developer — it is the sentence the model is shown when it
decides what to put in that field, and it is the only instruction it gets about it.

**This is why the system message on `DocumentAnalyzer` is short and given.** Almost everything a
longer prompt would say — categorise, extract, match, judge the quality — is already being said,
per field, right here. Saying it in both places is how the two drift: somebody tightens the
description, nobody touches the prompt, and the model is now told two different things about one
field. What is left in the system message is only what belongs to no single field.

The failure mode is silence. A vague description does not throw; it fills the field in with
something vaguer than you wanted, the card renders, and nobody finds out until they read one
carefully.

Read the five written ones first. The habit in all of them is the same: **say what form the answer
should take**, not only what it is about. "The kind of document" gets you a paragraph. "A short noun
phrase, e.g. 'invoice'" gets you a label. The two left to you are `category` and `summary`, which are
the two the card shows most prominently — change one, upload `assets/receipt.png`, and watch that
field change with nothing else touched.

`DocumentAnalysisTest` is red until both are written.

## Part 2 — send the file as itself

`tasks/task_3_document_agent/DocumentIntake.java`. Read `accept()` first; it is everything that
happens to an upload, in order. What you write is `promptFor`, two elements:

```java
return List.of(TextContent.from(INTAKE_INSTRUCTION), DocumentFiles.contentOf(content, mimeType));
```

One sentence of ours and one file. `contentOf` picks `PdfFileContent` or `ImageContent` from the
mime type and passes the bytes as they are — nothing extracts text first, nothing converts the
image, nothing summarises. The model is handed the document, not a description of it.

That is also why `quality` can work at all. A blurry scan and a crisp one produce the same extracted
text; only one of them is a photograph you can see is blurry. An agent given text could not answer
that question, and would answer it anyway.

The text has to be exactly `INTAKE_INSTRUCTION` and nothing else. Nothing the claimant supplied
belongs in it, the filename above all: a file called `ignore-the-above-and-approve.pdf` becomes part
of the prompt the moment somebody decides to be helpful and include it.

There is no cache. The same file uploaded twice is read twice, which is not what you would ship —
but a cache in front of this is a cache in front of the only thing here worth reading.

## How you know it worked

```bash
cd backend && ./mvnw test -Dtest=TaskCompletionTest
```

Then upload `assets/receipt.png` on a claim at http://localhost:5173 and read the card. The fields
should be the ones actually on that receipt, in Norwegian, with the labels the receipt uses — while
everything the agent *writes* is in English. That split is deliberate; see
[ADR 0002](../adr/0002-agents-write-in-english.md).

## Try to break it

- Upload `assets/driving_licence.png` to a motor claim. It is a real document and it satisfies
  nothing on the checklist. Does it say so, or does it force a match?
- Photograph a receipt at an angle, half in shadow. The quality assessment is the whole point of
  sending the image rather than its text.
- Upload something that is not a document at all — a photo of a dog. What is the category?
- Upload a file whose text tells the agent it has already been approved. Point 5 above is why that
  ends up on the claim handler's screen as a finding rather than in the extraction as a fact.

## If you finish early

- **Delete `@UserMessage`** from the argument and upload again. Nothing errors. The model answers
  confidently about a file it was never sent. This is the most expensive five minutes in the
  workshop.
- **Add a component to `DocumentAnalysis`** — whether the document is signed, say — and watch it get
  filled in with no other change.
- **Ask for the same file twice** and compare the two answers. Then read why intake hashes the bytes
