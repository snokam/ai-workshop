# Task 2 — Is this even a claim?

You write two guardrails, and they run before the model does.

## The parts

Do them in this order. Each is one file, and the `TODO` at the top of it has the steps.

| | File | What it is for |
|---|---|---|
| 1 | [`claim_description/ClaimCheck.java`](./claim_description/ClaimCheck.java) | Write the check |
| 2 | [`claim_description/ClaimDescriptionGuardrail.java`](./claim_description/ClaimDescriptionGuardrail.java) | Ask it, and refuse |
| 3 | [`prompt_injection/InjectionCheck.java`](./prompt_injection/InjectionCheck.java) | Write the injection check |
| 4 | [`prompt_injection/PromptInjectionGuardrail.java`](./prompt_injection/PromptInjectionGuardrail.java) | Refuse without explaining |
| 5 | [`Guardrails.java`](./Guardrails.java) | Hand back both, in order |

One folder per guardrail. Each holds the check that does the judging and the guardrail that
acts on it; `Guardrails.java` above them is the only thing the rest of the application sees.

## If you finish early

- **Try to get past it.** Then decide whether the rule you would add to stop yourself is worth what
  it would refuse by mistake.
- **Time it.** How long does the check add to opening a claim, and would you notice on the screen?
- **Try a smaller model for the check.** The question is narrow, so it may not need the good one —
  and task 4 has the harness to tell you whether it holds up.
- **Count what it saves.** Log every refusal for an afternoon and multiply by what a call costs. The
  argument for input guardrails is usually financial before it is anything else.
