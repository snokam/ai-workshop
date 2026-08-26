# Task 6 — Chat with tools and memory

Describe your tools so the model knows when to use them, and give each claim its own conversation.

A chat agent that can look things up is worth more than one that can only talk. You learn how a
tool's description is the thing that makes the model call it, and how memory turns separate
questions into a conversation.

## The parts

Do them in this order. Each is one file, and the `TODO` at the top of it has the steps.

| | File | What it is for |
|---|---|---|
| 1 | [`agent/ClaimChatTools.java`](./agent/ClaimChatTools.java) | Describe two of the tools |
| 2 | [`agent/ChatConfig.java`](./agent/ChatConfig.java) | Give the model its tools |
| 3 | [`agent/ChatConfig.java`](./agent/ChatConfig.java) | Give each claim its own conversation |

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

Part 3 is memory, and this is where it belongs: a handler's follow-up only makes sense given the
previous turn, so there is nothing simpler that would do instead. Two things to get right — **one
conversation per claim**, built inside the lambda or one handler reads another's, which
`ChatConfigTest` checks; and **the window**, which is a trade. Set it to 2 and hold a four-turn
conversation to find where it starts contradicting itself.

`@MemoryId` is not optional: LangChain4j refuses to build an agent that declares one without a
provider, so there is no half-wired version of this chat.
