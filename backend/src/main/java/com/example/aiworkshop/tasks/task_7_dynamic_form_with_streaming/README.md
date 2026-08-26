# Task 7 — A form that helps while you write

Send the model's answer to the browser word by word, while the user is still typing.

The screen does two separate things, and it is worth keeping them apart. **The form is dynamic**: an
agent reads what the user has written and decides which questions to ask, so the fields are not
fixed. **The help is streamed**: a second agent judges the description as it is being written and
answers a word at a time. The first is already built. The second is yours.

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

The feedback runs 700ms after the user stops typing, and says whether the description is good enough
yet:

> *"my bag is gone"* → It looks like your bag is gone. To help you with your claim, could you please
> tell me what happened, when it happened, and roughly what it was worth.
>
> *"my suitcase never turned up after my flight home on 3 May, flight 4121X. I lost some clothes and
> a toothbrush, 500 nok."* → It's great that you've explained what happened, when it happened, and
> what it cost.

The second one turns the bar green.

## Why streaming

Streaming sometimes makes sense: instead of waiting for the whole answer, the user sees it forming
and gets feedback earlier. That is worth it here, because they are still writing and can use the
feedback while the box is in front of them.

## Checking it

Open http://localhost:5173/chat, type a few words, and stop. Feedback should appear a word at a time
rather than all at once.
