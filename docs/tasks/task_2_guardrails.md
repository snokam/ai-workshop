# Task 2 — Is this even a claim?

You have an agent. It will answer anything you give it, including things nobody should have paid a
model to read.

**Time:** 30 minutes. **You need:** task 1 working.

## The problem

Type `hi` into the box on the report screen and press the button. A case opens. The classifier
returns `OTHER` with `LOW` confidence and a courteous sentence explaining that there is not much to
go on, which is a perfectly good answer to a question nobody should have asked.

You paid for that. So did the case handler, who now has a case to close.

The agent is not wrong. Nothing told it that some inputs are not worth an answer, and nothing could
— by the time the agent sees the text, the call has already been made.

## Where a guardrail goes

LangChain4j gives you two places to stand:

| | when it runs | what it can see | what it costs when it refuses |
|---|---|---|---|
| **Input guardrail** | before the request is sent | the message about to go | nothing |
| **Output guardrail** | after the reply comes back | the reply, and the request | a full call |

This task is the first kind, and the difference is worth sitting with. An input guardrail is the
only check in this workshop that is free when it says no. It is also the only one that cannot be
talked round by what it is reading, because it is not reading for meaning — a document full of
instructions aimed at the software gets nowhere against a rule that counts characters.

## Write it

**Write:** `tasks/task_2_guardrails/guardrails/ClaimDescriptionGuardrail.java`, implementing
`dev.langchain4j.guardrail.InputGuardrail`.

`message.singleText()` is what the person typed. Return `fatal(...)` with something they can act on
when there is nothing to work with, and `success()` otherwise. Two rules are enough:

- **enough to go on** — a handful of characters cannot describe anything that happened
- **more than a greeting** — `hei`, `hello there`, `test`. How a message opens, not what it says

Then hand it back from `Guardrails.beforeTheCall()`. `GuardrailConfig` publishes it as a bean and
task 1's agent picks it up — task 1 knows nothing about task 2, which is what lets the workshop be
done in order.

`GuardrailTest` is red until both rules are in. None of it needs a model, credentials or a network,
which is the property that makes this kind of check worth having.

## What not to do

Do not try to decide whether the situation is insurable. It is tempting — "is this really a claim?"
is the obvious reading of the task title — and it is the wrong instinct.

A guardrail that judged the subject would be a second, worse classifier standing in front of the
first, disagreeing with it occasionally, and refusing people whose claims are unusual rather than
absent. Deciding what kind of case this is has an agent already, and it has `OTHER` for exactly the
situations that fit nothing else.

The guardrail decides whether there is anything to read. The agent decides what it means.

## If you finish early

- **Refuse the same text twice in a row.** Someone pressing the button again because nothing seemed
  to happen is not a new case.
- **Count what it saves.** Log every refusal for an afternoon and multiply by what a call costs. The
  argument for input guardrails is usually financial before it is anything else.
- **Try to get something past it that should not be.** Then decide whether the rule you would add to
  stop it is worth what it would refuse by mistake.
