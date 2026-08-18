# Task 2 — Post-processing

The model has answered. Now what?

Task 1 was about not being talked into things. This one is about the work that happens *after* the
answer is accepted — in plain Java, with no model involved, no network, and no credentials. It is the
cheapest code in the application and it catches things the agent structurally cannot.

**Time:** 30 minutes. **You need:** the app running, and task 1 read (this uses what it produced).

## The idea

An agent reads one document at a time and sees only what is on the page. So there are whole classes
of problem it cannot notice, however good it gets:

| The agent cannot know | Java trivially can |
|---|---|
| that this same receipt is already on another case | hash the bytes, look it up |
| that the photo was last written by Photoshop | read the EXIF |
| that the capture date is a year before the incident | compare two dates |

None of that is a language problem, so none of it should cost a model call.

## What to build

```
task_2_postprocessing/
  FraudScreener.java              the entrypoint: runs the checks, keeps what they found,
                                  and defines the Upload a check is given
  checks/
    FraudCheck.java               the seam: one method
    DuplicateUploadCheck.java     the same bytes, seen before
    ImageMetadataCheck.java       what EXIF says about where the image came from
    AddressedTheAgentCheck.java   the report task 1's agent already made
  model/
    FraudScreening.java           the result: Indicators, each with a Kind and a Weight
    DocumentForClaimant.java      the projection that decides who sees any of it
```

One entrypoint per task: `DocumentIntake` and `CaseDesk` talk to `FraudScreener` and nothing else.

**Write a `FraudCheck`.** It is one method. Every `@Component` implementing it is injected into
`FraudScreener` and run, so adding a check is adding a class and nothing else — that is the only
piece of structure in this task, and it is the piece worth having.

Start with `DuplicateUploadCheck`, because it is the one you can demo in ten seconds. Open two cases
first — a Claimant describes what happened and a classifier opens the Case, so there are no seeded
ones to borrow:

```bash
curl -s -X POST localhost:8080/api/cases -H 'Content-Type: application/json' \
  -d '{"description":"Someone reversed into my parked car and I paid for the repair."}'
curl -s -X POST localhost:8080/api/cases -H 'Content-Type: application/json' \
  -d '{"description":"My laptop was stolen from my flat during a break-in."}'

curl -s -X POST localhost:8080/api/documents -F caseId=1001 -F file=@assets/repair-receipt.pdf
curl -s -X POST localhost:8080/api/documents -F caseId=1002 -F file=@assets/repair-receipt.pdf
curl -s localhost:8080/api/cases/1002 | jq '.screenings'
```

The same expense on a motor claim and a theft claim — which is the point.

```
[STRONG] ALREADY_UPLOADED
The same file, byte for byte, has already been uploaded to a different case.
```

A SHA-256 and a map. One expense, two claims — the oldest trick there is.

## Two rules a check must hold to

Both live in `FraudScreener`, and both matter more than anything a check finds:

- **A check cannot refuse an upload.** An upload is always accepted. Screening happens after the
  Document is stored, and a heuristic must never stand between someone and their own case.
- **A check cannot take the others down.** One throws, it is logged and skipped, the rest still run.
  There is a test for this; delete the try/catch and it fails.

## Weights, and why there are three

`NOTE`, `CONCERN`, `STRONG`. Every check here has an innocent explanation:

- messaging apps strip EXIF from everything that passes through them
- a screenshot never had camera metadata
- cropping a bystander out of a photo writes an editor's name into it
- the same file legitimately belongs to two cases when two cases share an expense

So most of these are `NOTE` by design. They earn their keep by *accumulating*: no camera origin, plus
Photoshop, plus a capture date a year before the incident, is a different proposition from any one of
them alone. A screen where every honest document shows a red panel is a screen handlers learn to
scroll past.

## The question that matters more than the code

**Who is allowed to see this?**

The answer here is the Case Handler and nobody else. Not the person who uploaded the file. Look at
`DocumentForClaimant` — the upload endpoints answer with that projection, and a Screening is not held
on a Document at all, so there is no route by which one reaches the claimant's screen.

Upload the poisoned file from task 1 and compare the two sides:

```bash
curl -s -X POST localhost:8080/api/documents \
  -F caseId=1001 -F file=@assets/document-that-gives-orders.pdf | jq '.analysis'
curl -s localhost:8080/api/cases/1001 | jq '.screenings'
```

Telling someone which of their tricks was noticed is free coaching in the ones that were not. Read
[ADR 0005](../adr/0005-fraud-signals-are-handler-side.md) and decide whether you agree — including
the part where it admits there is no login here, so this is the shape of the API rather than an
authorisation boundary.

## How you know it worked

```bash
./mvnw test -Dtest=FraudScreeningTest
```

Six tests, no credentials, no network.

## What is deliberately missing

**Reverse image search** — "has this photo been published online?" — is the obvious next check, and
it is the classic one for a damage photo lifted from a used-car listing. It is not here because every
version of it needs an API key or cloud credentials, and a workshop exercise that depends on
someone's Vision API being enabled is an exercise that fails on the day.

If you want it, the credential-free version is the same shape as `DuplicateUploadCheck`: hash a small
bundled set of known stock images at startup and match uploads against it. Same lesson, no account
required.

## If you finish early

- **A perceptual hash instead of SHA-256.** The duplicate check is defeated completely by re-saving
  the image. What would it take to catch that, and what would it start getting wrong?
- **A check that reads the extraction.** The agent pulled fields off the document. Do the numbers on
  a receipt add up? Java can do arithmetic; the model is only sort of good at it.
- **Delete the try/catch in `FraudScreener`** and watch a failing check take an upload down with it.
