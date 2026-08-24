# Task 2 — Is this even a claim?

You write one guardrail, and it runs before the model does.

The brief is `docs/tasks/task_2_guardrails.md`, from the repository root.

## The parts

Do them in this order. Each is one file, and the `TODO` at the top of it repeats the steps.

| | File | What it is for |
|---|---|---|
| 1 | [`guardrails/ClaimCheck.java`](./guardrails/ClaimCheck.java) | Write the check |
| 2 | [`guardrails/ClaimDescriptionGuardrail.java`](./guardrails/ClaimDescriptionGuardrail.java) | Ask it, and refuse |
| 3 | [`guardrails/Guardrails.java`](./guardrails/Guardrails.java) | Hand it back |

### Part 1 · `guardrails/ClaimCheck.java`

**Write the check.**

A second agent, in front of the first, asked one closed question: is there anything here to open a
case from?

Say yes to anything a person might contact an insurer about — a question, a complaint, something
that has gone wrong. Say no only when there is nothing to work with: an empty box, a greeting, a
few characters of nonsense.

**When in doubt, say yes.** Refusing someone with an unusual claim is far worse than opening a
case somebody closes: the second wastes a minute, the first turns a person away.

When you say no, write one sentence *to them*, in their language — and in English when the text is
too short or garbled to have one. An early version answered `asdf asdf` in Spanish.

### Part 2 · `guardrails/ClaimDescriptionGuardrail.java`

**Ask it, and refuse.**

`message.singleText()` is what the person typed. Hand it to `check.couldOpenACaseFrom(...)` and
return `fatal(...)` with the sentence it wrote when the answer is no.

No length rule underneath and no list of greetings. Mixing a deterministic gate with a judgement
about meaning muddies what this task is for — and a rule about length refuses
"Bilen ble stjålet" while letting "asdf asdf asdf asdf" through.

### Part 3 · `guardrails/Guardrails.java`

**Hand it back.**

Return the guardrail so `GuardrailConfig` can publish it as a bean. Task 1's agent takes whichever
guardrails exist and knows nothing about who wrote them.

Until this returns one, anything typed into the box reaches the model, including an empty one.

## Everything else in the folder

- `guardrails/` — `ClaimCheck`, `ClaimDescriptionGuardrail`, `GuardrailConfig`, `Guardrails`

## What it uses from the tasks before it

Nothing. It stands on its own.

No task before this one refers to anything in here, which is the rule the workshop runs on.
`TaskDependencyTest` fails if it is ever broken.
