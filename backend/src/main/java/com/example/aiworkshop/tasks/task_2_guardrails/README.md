# Task 2 — Guardrails

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
