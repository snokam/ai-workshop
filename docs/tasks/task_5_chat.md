# Task 5 — Tools and memory

Every agent so far answered one question from what it was handed. This one holds a conversation
about a case, looks things up while answering, and remembers what was already asked.

**Time:** 45 minutes. **You need:** tasks 1 and 2 working, and a case with a document or two on it.

## What changes

Three things, and each is one line of wiring in
`backend/src/main/java/com/example/aiworkshop/tasks/task_5_chat/`:

**Tools.** `CaseChatTools` is four methods carrying `@Tool`. The model decides when to call them
and LangChain4j does the calling. Read them: not one contains any logic. Every one hands straight to
`CaseDesk`, which is the same seam the screen uses. A tool is an entry point, not a place to put
behaviour — anything else and the agent gets a private version of the truth.

**Memory.** `chatMemoryProvider(caseId -> MessageWindowChatMemory.withMaxMessages(20))`. The key is
the case identifier, which is also what every tool receives as its `@ToolMemoryId`. One key doing
both jobs is what makes the conversation resume where it left off and makes each tool answer about
the right case.

**`Result<T>`.** The method returns `Result<Answer>` rather than `Answer`, so the tool calls that
happened on the way are still there afterwards. Return the bare type and the answer survives but
how it was reached does not — and the screen shows what was looked up.

## What to write

A system message for an agent that:

- answers questions about one case, from what its tools tell it
- may **propose** asking the claimant for something, and never does it itself
- says what it does not know rather than filling the gap

That last one matters more here than anywhere else in the workshop. This agent talks to the person
deciding the claim.

Then set `IMPLEMENTED = true`.

## Why it never writes

Look at `Proposal`. It is a sealed interface, and every proposal the agent makes has to be confirmed
by the handler before anything happens. Sealed rather than open on purpose: confirming one is a
pattern switch, and the compiler will not let a new kind of proposal be added without every place
that handles one being updated.

An agent that could write to the case directly would need to be right every time. One that proposes
needs only to be useful.

## How you know it worked

```bash
cd backend && ./mvnw test -Dtest=TaskCompletionTest
```

Then open a case on the handler side and ask the three suggested questions. Watch which tools get
called — the answer should cite what it looked at.

## Try to break it

- Ask "what is the total on the receipt?" about a case with no receipt.
- Ask the same question twice in a row. The second answer should know the first happened.
- Ask it to email the claimant. It has no tool for that. What does it say?
- Ask about a *different* case by reference. The memory id is the boundary; there is no tool that
  crosses it.

## If you finish early

- **Drop `Result<Answer>` to `Answer`** and watch the tool calls disappear from the screen while the
  answers stay correct.
- **Set the window to 2 messages** and have a four-turn conversation. Where exactly does it start
  contradicting itself?
- **Add a tool** that returns something the agent could work out itself. Does it call it? Tools it
  does not need are how a cheap agent becomes an expensive one.
