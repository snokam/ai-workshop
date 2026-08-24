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

| | when it runs | what it can see |
|---|---|---|
| **Input guardrail** | before the request is sent | the message about to go |
| **Output guardrail** | after the reply comes back | the reply, and the request |

This task is the first kind. A check in front of a model decides on the request rather than on an
answer the request has already shaped, which makes it much harder to talk round — and it can refuse
before the expensive thing happens.

## The check is a model too

The obvious first instinct is a rule: refuse anything under fifteen characters, refuse a list of
greetings. Write that and it refuses `Bilen ble stjålet` while letting `asdf asdf asdf asdf`
through. Both of those are the wrong way round, and no amount of tuning fixes it, because whether
there is a situation in a piece of text is a question about meaning.

So the guardrail asks a model. Not the classifier — a second, narrower one, with a single closed
question and no catalogue to choose from: **is there anything here to open a case from?**

That is `ClaimCheck`, and it is an agent exactly like the one in task 1: an interface, a system
message, a return type. The difference is the shape of the question. Open questions get answers
whatever you ask; closed ones are much harder to argue with.

**This costs a call to save a call.** Worth saying out loud, because "put a guardrail in front of
it" is often heard as "make it free". It is worth it here because the call it saves is the more
expensive of the two, and because nothing cheaper can answer the question at all — but it is a
trade, not a win.

## Write it

**Write:** `tasks/task_2_guardrails/guardrails/ClaimCheck.java` — the system message for a check
that says yes to anything a person might contact an insurer about, and no only when there is nothing
to work with.

**Write:** `tasks/task_2_guardrails/guardrails/ClaimDescriptionGuardrail.java` — ask the check, and
turn a no into `fatal(...)` carrying the sentence it wrote.

Then hand it back from `Guardrails.beforeTheCall(...)`. `GuardrailConfig` publishes it as a bean and
task 1's agent picks it up. Task 1 knows nothing about task 2, which is what lets the workshop be
done in order.

Two things the prompt has to get right, and both are easier to see once it is wrong:

- **When in doubt, say yes.** Refusing someone with an unusual claim is far worse than opening a
  case somebody has to close. The second wastes a minute; the first turns a person away.
- **Write the refusal to the claimant**, in their language — and in English when the text is too
  short or too garbled to have one. The first version of this answered `asdf asdf` in Spanish.

## What not to do

Do not decide whether the situation is insurable. It is tempting — "is this really a claim?" is the
obvious reading of the title — and it is the wrong instinct.

A guardrail that judged the subject would be a second, worse classifier standing in front of the
first, disagreeing with it occasionally, and refusing people whose claims are unusual rather than
absent. Deciding what kind of case this is has an agent already, and it has `OTHER` for exactly the
situations that fit nothing else.

The guardrail decides whether there is anything to read. The agent decides what it means.

## If you finish early

- **Try to get past it.** Then decide whether the rule you would add to stop yourself is worth what
  it would refuse by mistake.
- **Time it.** How long does the check add to opening a case, and would you notice on the screen?
- **Try a smaller model for the check.** The question is narrow, so it may not need the good one —
  and task 4 has the harness to tell you whether it holds up.
- **Count what it saves.** Log every refusal for an afternoon and multiply by what a call costs. The
  argument for input guardrails is usually financial before it is anything else.
