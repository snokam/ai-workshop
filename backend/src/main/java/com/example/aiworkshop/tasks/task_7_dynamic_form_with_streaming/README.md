# Task 7 — A form that asks, and something that reads what you wrote

You make feedback arrive beside the box a word at a time, while somebody is still writing in it.

## The part

One file, and the `TODO` in it has the steps.

| | File | What it is for |
|---|---|---|
| 1 | [`StreamedHelp.java`](./StreamedHelp.java) | Carry the tokens to the browser |

The questions on the form are not fixed — a given agent picks them from what you have written, and
returns them as a record, the same shape as the classifier you wrote in task 1. There is nothing to
write there. What you write is the other half: the feedback that arrives beside the box while you
are still typing.

## Why the streaming half is here and not elsewhere

Streaming is easy to wire up and easy to waste. Most agents answer a question somebody has finished
asking and then sit there being read, so streaming the reply only changes what the waiting looks
like — and on a reasoning model not even that, because it thinks first and emits everything at once.

Here nobody is waiting. They are writing, and the feedback lands 700ms after they stop, while the box
is still in front of them and still easy to change:

> *"my bag is gone"* → It looks like your bag is gone. To help you with your claim, could you
> please tell me what happened, when it happened, and roughly what it was worth.
>
> *"my suitcase never turned up after my flight home on 3 May, flight 4121X. I lost some clothes and
> a toothbrush, 500 nok."* → It's great that you've explained what happened, when it happened, and
> what it cost.

The second one turns the bar green, and that verdict is the interesting part of the prompt rather
than the prose. Ask a model for the verdict first and it answers before it has read: that same
description came back "something is still missing" six times out of six, each run naming different
missing details, every one of them present in the text. The fix is in `ClaimFormHelper` — a first
line the reader never sees, where it writes down what it found before it is allowed to conclude
anything.

Measured: first word at +0.35s on a warm model. It runs on the cheaper one from task 5 on purpose —
a reasoning model gave its first token after 4.72s in a single chunk, which is not streaming, it is
just arriving late.

## What you write

The join between `TokenStream`, which calls you back as tokens arrive, and Spring's `SseEmitter`,
which holds the response open until you say you are done. Both are push, so there is no loop and
nothing to poll — you say what to do on a token, on the end and on a failure, and then start it.
Each of those three has its own way of going wrong, and the `TODO` names them.
