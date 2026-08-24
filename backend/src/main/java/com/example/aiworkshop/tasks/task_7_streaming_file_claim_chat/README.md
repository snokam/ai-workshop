# Task 7 — File a claim with a streaming chat

You make the answer arrive as it is written, instead of all at once when it is finished.

## The part

One file, and the `TODO` in it has the steps.

| | File | What it is for |
|---|---|---|
| 1 | [`InterviewNarration.java`](./InterviewNarration.java) | Carry the tokens to the browser |

Both prompts are given. There are two agents here and only one of them is ever seen:

| | returns | who reads it |
|---|---|---|
| [`agent/ClaimIntakeInterviewer.java`](./agent/ClaimIntakeInterviewer.java) | `InterviewTurn`, a record | the application, which branches on it |
| [`agent/ClaimIntakeSpeaker.java`](./agent/ClaimIntakeSpeaker.java) | `TokenStream` | the claimant, a word at a time |

**They cannot be one call.** A method returning a record has no answer until the last token has
arrived, because half a JSON object is not an object. A method returning a `TokenStream` never has a
whole answer to give back. That is not a preference — it is whether the reply is for a program or for
a person, and this task is one of each.

So a turn costs two calls. Worth knowing you are paying it: the decision call is small and into a
fixed schema; the one worth streaming is the one a human reads at reading speed.

What you write is the join between `TokenStream`, which calls you back as tokens arrive, and Spring's
`SseEmitter`, which holds the response open until you say you are done. Both are push, so there is no
loop and nothing to poll — you say what to do on a token, on the end and on a failure, and then start
it. Each of those three has its own way of going wrong, and the `TODO` names them.
