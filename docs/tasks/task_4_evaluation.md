# Task 4 — How would you know?

Every other task asks you to build something. This one asks whether what you built is any good, and
it is the question that separates a demo from something you would put in front of a case handler.

**Time:** 30 minutes. **You need:** tasks 1 and 2 working — they are what you are evaluating.

The parts, in order, are in `backend/src/main/java/com/example/aiworkshop/tasks/task_4_evaluation/README.md` — each one names the file, what it is for, and what to reach for.

## The problem with "it works"

You have run the classifier a dozen times by now and it has been right every time, so it works.

That is not evidence. You wrote the descriptions, and you wrote them the way you were already
thinking about the categories. The cases that decide whether this is deployable are the ones you
would not have thought to type.

The same goes double for the guardrails, because both of them are models too. A rule you can read
cannot surprise you. A check that asks a model can hold on every example you thought of, fail on the
first one you did not, and change its mind about the same text next week.

## Run it

```bash
cd backend && ./mvnw test -Pevaluate
```

One command, both evaluations, nothing else. They are kept out of the ordinary `./mvnw test` because
they call a real model — a normal test run stays free and needs no credentials.

| Evaluation | Asks about | Set you build |
|---|---|---|
| `ClassifierEvaluation` | the classifier from task 1 | `LabelledCase` |
| `GuardrailEvaluation` | both guardrails from task 2 | `GuardrailProbe` |

Both print a table and then the rows that disagreed, in full. Note what neither does: **assert.**
There is no threshold to go green, and that is deliberate. A test that failed at 7/10 would teach
you to change the prompt until it passed, which is how an agent gets better at the test and worse at
the job.

## Part 1 — label the cases you would argue about

`tasks/task_4_evaluation/LabelledCase.java`. Three rows are written for you, and they are the three
kinds worth having: one plain, one where reasonable people disagree, one with no right answer among
the five. Add seven or eight of your own to `yours()`.

The rules to label against are the classifier's own: it picks exactly one of the five case types, or
nothing when none of them fit, and says how sure it is. `expected` may be null — "my neighbour parks
across my drive" has no right answer, and expecting null asks whether the agent will admit that or
force the nearest match to make the question go away.

Do not write eight easy ones. A suite of unambiguous examples tells you the model can do the job you
already knew it could do.

## Part 2 — write what should and should not get past the door

`tasks/task_4_evaluation/GuardrailProbe.java`. Three rows are written; add nine or ten.

Each probe is a piece of text and which of three things should happen to it. They are also the order
the guardrails run in, so a probe that trips the injection check never reaches the claim check:

| | means |
|---|---|
| `REACHES_THE_MODEL` | both let it through, and the classifier is asked |
| `NOTHING_TO_WORK_WITH` | the claim check refused: there is no situation in it |
| `ADDRESSED_TO_THE_SYSTEM` | the injection check refused: it is written to the software |

`GuardrailEvaluation` runs the real guardrails in the real order, nothing mocked, so what comes back
is what the person at the keyboard would have got. It reports which guardrail moved, which is the
part that matters — the two mistakes are not the same mistake:

- **refused something real** — a person is turned away at the door, and the injection guardrail
  deliberately tells them nothing about why. This is the expensive one.
- **let something through** — attacker-controlled text reached a model, or you paid for a call on
  `asdf`.

Do not average them. Nine out of ten is not ninety per cent of a guardrail: an attacker only needs
the tenth, and will send it a thousand times.

The rows worth your time are in the `TODO`, and the hardest is the one that is a claim *about*
instructions — "my broker told me to ignore the first letter and file again". A keyword filter
refuses it. Yours should not.

## Reading a disagreement

For every row that did not behave, decide which of these it is **before** touching a prompt:

| | |
|---|---|
| the label is wrong | on reflection you would not refuse that either |
| the prompt is wrong | the rule you meant is not the rule you wrote |
| the model is wrong | the prompt says it plainly and the answer is still not it |

Only the third is a reason to reach for a different model, and it is the rarest of the three.

## Will it run on a different model?

A separate question, and not an exercise — it is the one a facilitator wants answered the day
before, when somebody says they only have access to a different model.

```bash
cd backend && ./mvnw test -Pcheck-models
```

`ModelComparison` lives in `workshop/` rather than in this task, because it is not evaluating
anything anybody wrote. It probes the three things the workshop cannot do without: an answer that
parses into a record, a file the model will look at, and a tool it will actually call. A model that
classifies a little worse costs you an argument about labels. A model that cannot return an answer
in the shape it was asked for costs you the afternoon.

## What this is not

This is a smoke test you can run in a coffee break, not an evaluation suite. What is missing from it
is worth knowing:

- **It scores one field.** The classifier evaluation checks the type and nothing else — not whether
  the rationale is honest, not whether the confidence means anything, not whether `LOW` correlates
  with being wrong. That last one is the most useful thing you could measure next.
- **Four agents are still unmeasured.** `CaseStatusWriter`, `DocumentReader`, `CaseChatAgent` and
  `CaseIntakeInterviewer` have nothing pointed at them. The first two would take the rubric you have
  already seen; the chat needs a different technique again, because what you would be scoring is
  which tool it called, and the interviewer a different one after that, because the thing being
  judged is a conversation and not an answer.
- **It runs once.** Same input twice can give different answers. Ten calls tells you nothing about
  how stable any of them is.
- **It has no baseline.** Would a keyword match do as well? On the unambiguous half, probably. Worth
  knowing before paying for a model.
- **Nobody labelled it but us.** Ten cases labelled by the person who wrote the prompt is the
  weakest possible evidence, and it is what most "we evaluated it" amounts to.

## If you finish early

- **Run it three times** and count how many answers move. Then decide what your temperature should be.
- **Write a fifth attack.** The set that matters is not the one that shipped, it is the one you
  thought of that is not in it — and the four there were written by the same person who wrote the
  guardrails, which is the weakest possible test and worse here than anywhere, because an attacker
  is trying.
- **Score the chat by its tools.** Ask it five questions whose answers need a specific tool, and
  check which it called. That is a fourth technique again, and the one closest to how agents are
  evaluated in practice.
- **Score the confidence.** Of the answers that disagreed with the label, how many said `HIGH`? An
  agent that is confidently wrong is worse than one that is unsure.
- **Write the keyword baseline** — twenty lines of `contains()`. Whatever it scores is the bar the
  model has to clear to be worth its cost.
