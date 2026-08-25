# Task 7 — A form that asks, and something that reads what you wrote

You make feedback arrive beside the box a word at a time, while somebody is still writing in it.

## The part

One file, and the `TODO` in it has the steps.

| | File | What it is for |
|---|---|---|
| 1 | [`StreamedHelp.java`](./StreamedHelp.java) | Carry the tokens to the browser |

## Two agents, and why they cannot be one call

The screen does two things at once, and they are two different shapes of answer.

| | returns | who reads it |
|---|---|---|
| [`agent/ClaimIntakeInterviewer.java`](./agent/ClaimIntakeInterviewer.java) | `InterviewTurn`, a record | the application — it renders the questions as form fields, or opens the claim |
| [`agent/ClaimFormHelper.java`](./agent/ClaimFormHelper.java) | `TokenStream` | the person writing, a word at a time |

A method returning a record has no answer until the last token has arrived, because half a JSON
object is not an object. A method returning a `TokenStream` never has a whole answer to give back.
It is not a preference — it is whether the reply is for a program to branch on or for a person to
read while it is still being written.

## Why the streaming half is here and not elsewhere

Streaming is easy to wire up and easy to waste. Most agents answer a question somebody has finished
asking and then sit there being read, so streaming the reply only changes what the waiting looks
like — and on a reasoning model not even that, because it thinks first and emits everything at once.

Here nobody is waiting. They are writing, and the feedback lands 700ms after they stop, while the box
is still in front of them and still easy to change:

> *"my bag went missing"* → This description is a bit thin. The most useful thing missing is when
> your bag went missing.
>
> *"my suitcase never turned up after my flight home on 3 May and I had to buy clothes"* → This is a
> good description for a baggage claim. You have clearly stated what happened, when it happened, and
> to what.

Measured: first word at +0.35s on a warm model. It runs on the cheaper one from task 5 on purpose —
a reasoning model gave its first token after 4.72s in a single chunk, which is not streaming, it is
just arriving late.

## What you write

The join between `TokenStream`, which calls you back as tokens arrive, and Spring's `SseEmitter`,
which holds the response open until you say you are done. Both are push, so there is no loop and
nothing to poll — you say what to do on a token, on the end and on a failure, and then start it.
Each of those three has its own way of going wrong, and the `TODO` names them.
