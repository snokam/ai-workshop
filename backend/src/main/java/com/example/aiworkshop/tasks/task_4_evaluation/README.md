# Task 4 — How would you know?

You build the sets four evaluations run over, then read what they say.

The brief is `docs/tasks/task_4_evaluation.md`, from the repository root.

## The parts

Do them in this order. Each is one file, and the `TODO` at the top of it repeats the steps.

| | File | What it is for |
|---|---|---|
| 1 | [`LabelledCase.java`](./LabelledCase.java) | Label the cases you would argue about |
| 2 | [`ExtractedFacts.java`](./ExtractedFacts.java) | Write down what a handler needs |

### Part 1 · `LabelledCase.java`

**Label the cases you would argue about.**

One description, and the case type a person thinks it should open.

Do not write ten easy ones. Half the value is in the rows reasonable people disagree about — a
laptop stolen from a car, an injury on holiday treated privately — because those are the ones that
tell you whether a disagreement is the model being wrong or the label being an opinion.

There is no case type for "something else" any more, so `null` is a legitimate label.

### Part 2 · `ExtractedFacts.java`

**Write down what a handler needs.**

Two lists per file, and they are not the same measurement. `mustFind` is what a handler would be
annoyed to have missed. `mustNotSay` is what the document does not contain — an agent that
produces one of those has not misread anything, it has made it up.

Open the files in `assets/` and fill in the empty lists. Doing that by hand is most of what
building an evaluation set actually is, and you will find yourself inventing scoring rules as you
go: is `20 468,75` the same answer as `20468`?

## Everything else in the folder

- at the top — `Attack`, `CandidateModel`, `ExtractedFacts`, `LabelledCase`

## What it uses from the tasks before it

- **task 1, Your first agent** — `CaseType`

No task before this one refers to anything in here, which is the rule the workshop runs on.
`TaskDependencyTest` fails if it is ever broken.
