# Task 6 — Claim: Advisor chat

You write what makes a tool get called, and the one line that hands the tools over.

## The parts

Do them in this order. Each is one file, and the `TODO` at the top of it has the steps.

| | File | What it is for |
|---|---|---|
| 1 | [`agent/ClaimChatTools.java`](./agent/ClaimChatTools.java) | Describe two of the tools |
| 2 | [`agent/ChatConfig.java`](./agent/ChatConfig.java) | Give the model its tools |

The agent's `@SystemMessage` in [`agent/ClaimChatAgent.java`](./agent/ClaimChatAgent.java) is given.
The prompt is not what decides whether a tool is used — the `@Tool` description is, and that is what
you write.

A tool description is a prompt. It is the only thing the model reads when it decides whether to call
a method, so it has to answer one question for a reader who already has the claim summary in front
of them: **when would I need this instead of what I already have?** Two of the four are written as
worked examples; two are yours.

Then `.tools(...)` in `ChatConfig` is what makes any of it reachable. Until that call is there the
agent is built without them, and the failure is worth seeing once: it does not report that it cannot
check anything. It answers anyway, fluently, from the summary it was given.
