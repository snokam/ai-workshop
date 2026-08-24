# Task 7 — Claim: File claim with AI chat

You write an interview instead of a form, and the bound that stops it interrogating people.

## The parts

Do them in this order. Each is one file, and the `TODO` at the top of it has the steps.

| | File | What it is for |
|---|---|---|
| 1 | [`agent/ClaimIntakeInterviewer.java`](./agent/ClaimIntakeInterviewer.java) | Write the interviewer |
| 2 | [`InterviewBudget.java`](./InterviewBudget.java) | Stop it asking forever |

Part 2 is the first loop in the workshop. Every agent before this one answered a single call; this
one asks, reads the answer, and decides whether to ask again — and nothing in that loop stops it. The
prompt asks for two or three questions, and a prompt is a request.

What makes it interesting is not the counter but what happens when it runs out, and the honest
options are all bad in different ways. The class comment lays them out. Read it before writing the
two lines, because the code is obvious and the choice it encodes is not.
