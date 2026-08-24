# Task 5 — Claim: Claim summary

Every agent so far looked at one thing. This one reads every document on a claim at once and says
what they add up to.

**Time:** 40 minutes. **You need:** tasks 1 and 2 working, and a claim with at least three documents.

The parts, in order, are in `backend/src/main/java/com/example/aiworkshop/tasks/task_5_claim_summary/README.md` — each one names the file, what it is for, and what to reach for.

## The expensive one

This is the only agent in the workshop whose cost is worth thinking about. It grows with the claim:
ten documents means ten readings in one prompt, on every screen load, for every handler who opens
it.

Two decisions in this repository follow from that, and both are visible in the code you are about to
write against.

**It is not shown the files.** `DocumentForSummary` is a projection — read it and note what it
leaves out. The summariser gets each document's *reading*, not its bytes. The reading already
happened in task 3; doing it again here would be paying twice for the same answer, and the two could
disagree.

**It is cached against what it was written over.** `ClaimSummaryStore` keys a summary by the
documents it saw. Add a document and it is stale and recomputed; open the claim twice and it is not.
That is why the claim list does not run agents and opening one does.

## What to write

Open `backend/src/main/java/com/example/aiworkshop/tasks/task_5_claim_summary/agent/ClaimSummarizer.java` and
write a system message for an agent that says, across all of them:

- what is **established** — the facts more than one document agrees on
- what **disagrees** — the same fact with two values, which is the whole reason to read them together
- what is still **missing**, in terms of what the claim is waiting for

Not a list of summaries. One document at a time is what the cards already show; if this agent only
restates them it has earned nothing.

`ClaimStatusWriter` sits beside it and is the cheap counterpart: derived facts in, one short
situation report out, no documents at all. Read both and note that they are not the same kind of
thing, though both are "the agent that writes prose".

## What it is shown, and what that costs

`DocumentForSummary` beside the agent is given, and worth reading before you start. The summariser is
handed one of these per document, in one prompt, on every screen load — so every component of it is
paid for once per document per open, and the ones nobody reads are the whole of the cost with none of
the value.

Notice what is deliberately absent: there are no bytes. The file was read in task 3, and reading it
again would pay twice for one answer and risk two descriptions of one document on the same screen.
Compare it with `DocumentInDetail`, which the chat fetches for a single document when somebody
actually asks.

## How you know it worked

```bash
cd backend && ./mvnw test -Dtest=TaskCompletionTest
```

Then put a receipt and a claim form on the same claim with different totals on them, and open it.
"Across the documents" should say the two disagree, and say which is which.

## Try to break it

- One document. Does it say anything a card does not already say?
- Two documents that agree completely. Same question.
- Two that contradict each other on a date. This is the claim it exists for.
- Fifteen documents. Watch how long the screen takes, then look at `ClaimSummaryStore` again.

## If you finish early

- **Give it `DocumentInDetail` instead** of `DocumentForSummary`. Better answers, and now read what
  it costs.
- **Turn the cache off** and open the same claim five times. That is the bill for a screen refresh.
- **Summarise the summary** for a claim with fifty documents. At what point does a summary need its
  own summary, and what have you lost by then?
