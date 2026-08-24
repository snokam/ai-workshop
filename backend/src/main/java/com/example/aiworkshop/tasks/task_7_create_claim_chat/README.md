# Task 7 — Claim: File claim with AI chat

You write an interview instead of a form.

## The parts

Do them in this order. Each is one file, and the `TODO` at the top of it has the steps.

| | File | What it is for |
|---|---|---|
| 1 | [`agent/ClaimIntakeInterviewer.java`](./agent/ClaimIntakeInterviewer.java) | Write the interviewer |

## If you finish early

- **Split another type.** `HOME_CONTENTS` is one general scenario today; theft and water damage need
  different papers. Add the scenarios and watch the questions change with no prompt edit.
- **Let it re-open.** Right now a decision is final. What would it take to let the person correct it
  and re-run from the transcript?
- **Give it a budget.** Make "never more than three questions" a rule the code enforces, not a line in
  the prompt, and decide what happens on the fourth turn.
