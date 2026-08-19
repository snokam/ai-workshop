# Task 8 — How would you know?

You write whether any of it is any good.

The brief is `docs/tasks/task_8_evaluation.md`, from the repository root.

## What is yours to write

Each of these has the exercise parked in it as a comment headed `── To set this task again`.

- `ExtractedFacts.java`
- `LabelledCase.java`

## What is in this folder

Four evaluations, and the only thing they have in common is the question. Each agent answers
with a different shape of thing, and the technique has to change with it.

- `LabelledCase` — **categorical.** One value out of a list, so scoring is a comparison. The one you write.
- `ExtractedFacts` — **extraction.** Facts the agent chose itself, so coverage and invention, counted separately.
- `SummaryRubric` — **prose.** Nothing to compare against, so questions a good summary must survive.
- `SummaryJudge` — the model that answers them, and the one thing here to read sceptically.
- `Attack` — **adversarial.** The only set with a right answer, and no partial credit.

The runners are in the matching test folder, all four disabled — they cost model calls and run
when you ask for them. `docs/tasks/task_8_evaluation.md` has the commands.

## What it uses from the tasks before it

- **task 1, Your first agent** — `CaseType`

It is the last task, so it may use all of them. Nothing uses it back.
