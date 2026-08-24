# Task 4 — How would you know?

Three tasks in, you have an agent, a guardrail in front of it, and a second agent that reads files.
None of them has been measured. This is where you find out whether any of it works, and what
"works" would even mean.

The brief is `docs/tasks/task_4_evaluation.md`, from the repository root.

## Four evaluations, one per shape of answer

Every file in this folder is an evaluation set: the examples one technique runs over. Each is paired
with a runner in the matching test folder, and each exists because the technique before it does not
work on the next kind of answer.

| The set | What it evaluates | Why this technique |
|---|---|---|
| `LabelledCase` | task 1's classifier | one value out of a list, so scoring is a comparison |
| `ExtractedFacts` | task 3's document agent | it chooses its own facts, so coverage and invention, counted apart |
| `Attack` | task 2's guardrail | the only set with a right answer, and no partial credit |
| `CandidateModel` | whether any of it runs elsewhere | holds the task still and changes the model |

The runners are `ClassifierEvaluation`, `ExtractionEvaluation`, `GuardrailEvaluation` and
`ModelComparison`, all disabled — they cost model calls and run when you ask for them. The brief has
the commands.

Scoring prose is the technique this task does not cover, because the agent that writes prose is not
written yet. It waits with the summariser, in task 6.

## What is yours to write

Each of these has the exercise parked in it as a comment headed `── To set this task again`.

- `LabelledCase.java` — the descriptions you would argue about
- `ExtractedFacts.java` — what a handler needs from each of the sample files

`Attack` and `CandidateModel` ship filled in. Read them rather than writing them: the first is four
shapes of prompt injection and one honest control, the second is the shortest way to answer "can we
use a different model" before the day rather than during it.

## What it uses from the tasks before it

- **task 1, Your first agent** — `CaseType`

The runners reach further — into the guardrail from task 2 and the document agent from task 3 —
because that is what they measure. Nothing before this task refers to anything in here, which is the
rule the workshop runs on, and `TaskDependencyTest` fails if it is ever broken.
