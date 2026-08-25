# Task 7 — Help while you type, streamed

You make help arrive beside the box a word at a time, while somebody is still filling it in.

## The part

One file, and the `TODO` in it has the steps.

| | File | What it is for |
|---|---|---|
| 1 | [`StreamedHelp.java`](./StreamedHelp.java) | Carry the tokens to the browser |

## Why this screen and not the others

Streaming is easy to wire up and easy to waste. Every other agent in this workshop answers a question
somebody has finished asking, so streaming its reply only changes what the waiting looks like — and
on a reasoning model it does not even do that, because it thinks first and then emits everything at
once.

Here nobody is waiting. They are typing. [`agent/ClaimFormHelper.java`](./agent/ClaimFormHelper.java)
reads what is in the box each time the typing settles and says what would be worth adding — a date, a
reference, the receipt they will be asked for anyway. Help that arrives while you work is help. The
same sentence delivered after you submit is too late to be worth anything.

An earlier version of this task ran an interview instead: question, answer, question. It was worse
than the form it replaced. People can see a whole form, fill it in the order things occur to them,
and change their mind — so the form came back, and the agent moved to the side where it belongs.

## The two shapes of answer

| | returns | who reads it |
|---|---|---|
| `ClaimTypeClassifier` (task 1) | a record | the application, which branches on it |
| [`agent/ClaimFormHelper.java`](./agent/ClaimFormHelper.java) | `TokenStream` | a person, a word at a time |

**They cannot be the same call.** A method returning a record has no answer until the last token has
arrived, because half a JSON object is not an object. A method returning a `TokenStream` never has a
whole answer to give back. It is not a preference — it is whether the reply is for a program or for a
person, and the form has one of each: the helper while you write, the classifier when you submit.

What you write is the join between `TokenStream`, which calls you back as tokens arrive, and Spring's
`SseEmitter`, which holds the response open until you say you are done. Both are push, so there is no
loop and nothing to poll — you say what to do on a token, on the end and on a failure, and start it.
Each of those three has its own way of going wrong, and the `TODO` names them.
