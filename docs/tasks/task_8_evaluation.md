# Task 8 — How would you know?

Every other task asks you to build something. This one asks whether what you built is any good, and
it is the question that separates a demo from something you would put in front of a case handler.

**Time:** 25 minutes. **You need:** task 1 working.

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

## What this is not

This is a smoke test you can run in a coffee break, not an evaluation suite. What is missing from it
is worth knowing:

- **It scores one field.** Nothing here checks whether the rationale is honest, whether the
  confidence means anything, or whether `LOW` actually correlates with being wrong. That last one is
  the most useful thing you could measure next.
- **It runs once.** Same input twice can give different answers. Ten calls tells you nothing about
  how stable any of them is.
- **It has no baseline.** Would a keyword match do as well? On the unambiguous half, probably. Worth
  knowing before paying for a model.
- **Nobody labelled it but us.** Ten cases labelled by the person who wrote the prompt is the
  weakest possible evidence, and it is what most "we evaluated it" amounts to.

## If you finish early

- **Run it three times** and count how many answers move. Then decide what your temperature should be.
- **Score the confidence.** Of the answers that disagreed with the label, how many said `HIGH`? An
  agent that is confidently wrong is worse than one that is unsure.
- **Write the keyword baseline** — twenty lines of `contains()`. Whatever it scores is the bar the
  model has to clear to be worth its cost.
