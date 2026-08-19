# Task 8 — How would you know?

Every other task asks you to build something. This one asks whether what you built is any good, and
it is the question that separates a demo from something you would put in front of a case handler.

**Time:** 25 minutes for the exercise, plus about 15 to run and read the other three.
**You need:** task 1 working. The last three want tasks 2, 3 and 5 as well.

## The problem with "it works"

You have run the classifier a dozen times by now and it has been right every time, so it works.

That is not evidence. You wrote the descriptions, and you wrote them the way you were already
thinking about the categories. The cases that decide whether this is deployable are the ones you
would not have thought to type.

## Run it

`backend/src/test/java/com/example/aiworkshop/tasks/task_8_evaluation/ClassifierEvaluation.java` is
disabled, because it costs ten model calls. Take the `@Disabled` off, or:

```bash
cd backend
./mvnw test -Dtest=ClassifierEvaluation -Dsurefire.failIfNoSpecifiedTests=false
```

It prints a table and a count, and then the disagreements in full.

Note what it does **not** do: it does not assert. There is no threshold to go green, and that is
deliberate — a number decides nothing here, and a test that fails at 7/10 would teach you to change
the prompt until it passed.

## The actual exercise

Read the disagreements and sort each one into two piles.

**The model is wrong.** It matched a word instead of reading the sentence — "my neighbour parks
across my drive" answered as MOTOR. That is a bug, and the prompt is where you fix it.

**The label is an opinion.** "I broke my ankle on holiday and paid a private clinic" is a travel
claim and a health treatment claim, and which one you open is a policy decision your company has
made and the model has not been told about. The fix is not in the prompt; it is either in the
catalogue or in accepting that a human decides this one.

Half of the ten were chosen to land in the second pile. Sorting them is the whole task, because
getting this wrong in the other direction — treating every disagreement as a bug — is how an agent
gets steadily worse at the job while getting better at the test.

## Then write your own

Add cases to `LabelledCase.all()` until you have some you genuinely cannot call. Those are worth
more than the ten that shipped.

Things worth trying: a description in Norwegian; two claims in one sentence; something written
angrily; something so short it says nothing; a description that names a category outright and is
about a different one.

## Three more, already written

The classifier is the easiest agent in this workshop to evaluate, and that is why it is the one you
do by hand: it answers with one value out of a list, so scoring it is a comparison. Every other
agent answers with something you cannot compare. Run these three and read what comes out — the
point is not the numbers, it is that each needed a different technique and none of them is the one
you just used.

```bash
cd backend
./mvnw test -Dtest=ExtractionEvaluation -Dsurefire.failIfNoSpecifiedTests=false
./mvnw test -Dtest=SummaryEvaluation   -Dsurefire.failIfNoSpecifiedTests=false
./mvnw test -Dtest=GuardrailEvaluation -Dsurefire.failIfNoSpecifiedTests=false
```

**Extraction — the document agent.** It chooses its own facts and its own wording, so there is no
answer to match. What you can ask is coverage, and separately whether anything appeared that was
never in the document. Never add those two together: an agent that finds everything and invents one
figure is dangerous, and one number hides it. `ExtractedFacts` has two lists filled in and two left
empty — filling them is part two of this task, and doing it by hand is most of what building an
evaluation set actually is.

Expect an argument with your own scoring. Is `20468` the same answer as `20 468,75`? The first
version of that file said it was not, and scored a miss against a document that plainly contains
the number.

**Prose — the summariser.** Nobody can write down the correct summary, so instead write down what a
good one must be true of: four yes-or-no questions in `SummaryRubric`, each one specific enough that
a reader could settle it without arguing. Then something has to answer them, and at a hundred
summaries that something is a model.

Read `SummaryJudge` before you trust it. It is the technique here most easily used badly — ask a
model whether some text is good and it will mostly say yes, and you will have measured its
agreeableness. Answer the four questions yourself before you look at what it said. Where you and it
disagree is the finding. And note what it cannot catch: the judge and the summariser are the same
model, so anything they are both wrong about is invisible to this.

**An attack set — the guardrails.** The only one here with a right answer. Each document in `Attack`
asks for something it must not get, and one asks for nothing and must be left alone. Scored as a
count, deliberately, unlike the other three: nine out of ten is not ninety per cent of a guardrail.

Two things in that file are worth reading for how they had to be built. The attacks are printed on a
parking notice rather than a receipt, because the case asks for a receipt and an attack printed on a
real one proves nothing — the match would have been correct anyway. And the outcome is reported per
layer rather than pass or fail, because the first version was not that precise and scored a
confident five out of five while every reply was in fact unusable. An evaluation can be wrong in
exactly the way the thing it is testing can.

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
