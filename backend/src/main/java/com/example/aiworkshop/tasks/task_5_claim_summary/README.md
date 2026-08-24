# Task 5 — Claim: Claim summary

You write the expensive agent: every document on a claim, read together, in one call.

## The part

One file, and the `TODO` at the top of it has the steps.

| | File | What it is for |
|---|---|---|
| 1 | [`agent/ClaimSummarizer.java`](./agent/ClaimSummarizer.java) | Write the expensive agent |

[`DocumentForSummary.java`](./DocumentForSummary.java) beside it is given, and worth reading before
you start: it is what the agent is shown for each document, and every component of it is paid for on
every open.

## If you finish early

- **Give it `DocumentInDetail` instead** of `DocumentForSummary`. Better answers, and now read what
  it costs.
- **Turn the cache off** and open the same claim five times. That is the bill for a screen refresh.
- **Summarise the summary** for a claim with fifty documents. At what point does a summary need its
  own summary, and what have you lost by then?
