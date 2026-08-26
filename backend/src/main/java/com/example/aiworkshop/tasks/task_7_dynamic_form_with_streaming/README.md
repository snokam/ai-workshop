# Task 7 — A form that helps while you write

Send the model's answer to the browser word by word, while the user is still typing.

## The part

One file, and the `TODO` in it has the steps.

| | File | What it is for |
|---|---|---|
| 1 | [`StreamedHelp.java`](./StreamedHelp.java) | Carry the tokens to the browser |

You join two things. `TokenStream` calls you back as each token arrives. Spring's `SseEmitter` holds
the HTTP response open until you close it. Both push, so there is no loop and nothing to poll: you
say what to do on a token, at the end, and on a failure, then start it. Each of those three has its
own way of going wrong, and the `TODO` names them.

## What the screen does with it

The form's questions are chosen by an agent that is already written, so there is nothing to do there.
Your half is the feedback next to the box. It runs 700ms after the user stops typing, and says
whether the description is good enough yet:

> *"my bag is gone"* → It looks like your bag is gone. To help you with your claim, could you please
> tell me what happened, when it happened, and roughly what it was worth.
>
> *"my suitcase never turned up after my flight home on 3 May, flight 4121X. I lost some clothes and
> a toothbrush, 500 nok."* → It's great that you've explained what happened, when it happened, and
> what it cost.

The second one turns the bar green.

## Why streaming belongs here and not earlier

Streaming only helps when someone is waiting and can still act on what arrives. Most agents answer a
question that has already been asked, so streaming the reply only changes what the waiting looks
like. Here the user is still writing, and the feedback lands while the box is in front of them.

Two things that were measured while building this, both worth knowing:

- The first word arrives at +0.35s on the cheap model from task 5. A reasoning model gave its first
  token after 4.72s, in a single chunk — that is not streaming, it is arriving late.
- The prompt in [`agent/ClaimFormHelper.java`](./agent/ClaimFormHelper.java) makes the model write
  down what it found before it is allowed to give a verdict. Asked for the verdict first, it called
  a complete description incomplete six times out of six, naming different missing details each
  time — all of them present in the text.

## Checking it

Open http://localhost:5173/chat, type a few words, and stop. Feedback should appear a word at a time
rather than all at once.
