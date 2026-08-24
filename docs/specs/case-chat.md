# Claim Chat

A per-Claim conversation with a tool-using agent that proposes writes.

Tracked as [issue #17](https://github.com/snokam/ai-workshop-document-handling/issues/17)
(`ready-for-agent`). The issue is canonical; this copy exists to be read and presented.

## Problem Statement

A Claim Handler opening a Claim gets two pieces of agent prose — the situation report and the Claim
Summary — and then the conversation stops. Everything else they might want to know requires reading
the Documents themselves, which is the exact work the agents were introduced to remove.

The gap is sharpest for questions that are specific rather than general. "What is the total on the
repair invoice?" is answerable from the Extraction already on screen. "Does the date on the medical
certificate cover the whole absence?" needs two Documents compared. "The scan is dark — can you make
out the amount at the bottom?" needs the original file re-read, and the file is gone the moment the
Claimant's browser tab closes.

There is also nothing a Claim Handler can do from inside that reading. Confirming a Review means
scrolling to the right Document and finding its button. Chasing a Claimant for a missing document
has no mechanism at all — the situation report tells the handler to do it, and the system offers no
way to.

## Solution

A **Claim Chat**: a conversation pinned to the one Claim a Claim Handler has open, in a sticky column
beside the Claim's contents, so the answer and the evidence are on screen together.

The agent behind it starts with the Claim at a glance — Claim Status, Required Documents, what is
outstanding, the Claim Summary, and a one-line index of every attached Document. Anything deeper it
fetches on demand with tools: the full Extraction and Quality Assessment for one Document, or a
re-read of the original file by a second agent that can actually see it.

It can also **propose** two writes: a Review, and a Document Request asking the Claimant for a named
document. A Proposal performs nothing. It appears as a card in the conversation with Confirm and
Decline, and only a Claim Handler's click makes it real. A confirmed Document Request appears on the
Claimant's upload screen.

Every tool the agent used is shown under its answer, so a Claim Handler can see what the answer was
built from.

## User Stories

1. As a Claim Handler, I want to ask questions about the Claim I have open, so that I do not have to read every Document to answer something specific.
2. As a Claim Handler, I want the chat to already know which Claim I am looking at, so that I never have to name or identify the Claim in my question.
3. As a Claim Handler, I want the chat to sit beside the Claim contents rather than covering them, so that I can read an answer and check it against the Document in the same glance.
4. As a Claim Handler, I want the agent to already know the Claim Status, the Required Documents and what is outstanding, so that "what is this waiting on?" is answered instantly.
5. As a Claim Handler, I want the agent to already know the Claim Summary, so that its answers are consistent with the prose I have just read above it.
6. As a Claim Handler, I want the agent to know what Documents are attached and what each one counts as, so that I can refer to "the repair invoice" and be understood.
7. As a Claim Handler, I want to ask what a specific Document says, so that I get its Extraction without opening it.
8. As a Claim Handler, I want to ask why a Document was judged poor quality, so that I can decide whether to Review it without hunting for the reason on screen.
9. As a Claim Handler, I want to ask the agent to go back and read the original file, so that I can get at something no Extraction captured.
10. As a Claim Handler, I want the agent to tell me when the original file genuinely does not show what I asked about, so that I stop looking and chase the Claimant instead.
11. As a Claim Handler, I want to ask whether the Documents disagree with each other, so that I catch a contradiction before deciding the Claim.
12. As a Claim Handler, I want to ask a follow-up that refers back to the previous answer, so that I can dig into something without restating it.
13. As a Claim Handler, I want the conversation to still be there when I come back to the Claim, so that I do not repeat work I already did.
14. As a Claim Handler, I want to see which tools the agent used to answer, so that I can tell a looked-up fact from a guess.
15. As a Claim Handler, I want to see which Document a looked-up fact came from, so that I can go and check it myself.
16. As a Claim Handler, I want an empty chat with a few suggested questions when I open a Claim, so that I know what is worth asking without waiting for a greeting.
17. As a Claim Handler, I want opening a Claim to cost no more than it does today, so that browsing Claims does not get slower because a chat exists.
18. As a Claim Handler, I want the agent to suggest a Review when it thinks a Document is workable despite a poor Quality Assessment, so that I am not the one who has to notice.
19. As a Claim Handler, I want a suggested Review to change nothing until I click Confirm, so that the Claim Status never moves because a model decided it should.
20. As a Claim Handler, I want to Confirm a suggested Review from inside the conversation, so that I do not have to find the Document's own button.
21. As a Claim Handler, I want confirming a Review to move the Claim exactly as the Document's own Review button does, so that there is one behaviour and not two.
22. As a Claim Handler, I want to Decline a Proposal I disagree with, so that I can say no without ignoring it.
23. As a Claim Handler, I want the agent to know I declined a Proposal, so that it does not propose the same thing again two questions later.
24. As a Claim Handler, I want the agent to know which Proposals are still outstanding, so that it does not repeat one I have not got to yet.
25. As a Claim Handler, I want Proposals to still be there when I reopen the Claim, so that an unanswered suggestion is not silently lost.
26. As a Claim Handler, I want the agent to propose asking the Claimant for a named document, so that the chase the situation report keeps telling me to do is something I can actually do.
27. As a Claim Handler, I want a Document Request to reach the Claimant only after I confirm it, so that a model cannot contact a Claimant on my behalf.
28. As a Claim Handler, I want confirming a Document Request not to change the Claim's Required Documents, so that asking a question cannot move a Claim backwards out of READY_FOR_DECISION.
29. As a Claimant, I want to see what my Claim Handler has asked me for, so that I know what to upload next.
30. As a Claimant, I want to see a Document Request alongside the Required Documents I already have, so that the two are not in different places.
31. As a Claimant, I want a Document Request to be phrased in plain language, so that I understand what is being asked of me.
32. As a Claim Handler, I want the agent to be unable to perform any write on its own, so that I can trust that nothing changed while I was reading.
33. As a Claim Handler, I want the agent to answer in English regardless of what language the Documents are in, so that its answers match the rest of the handler side.
34. As a Claim Handler, I want quoted field names and values to stay in the Document's own language, so that I can match them against the artefact.
35. As a Claim Handler, I want a failed model call to tell me what actually went wrong, so that I am not staring at a blank panel.
36. As a Claim Handler, I want the chat to work the same whichever model provider is configured, so that the demo is not tied to one vendor.
37. As a developer, I want the tools to hold no logic of their own, so that the whole feature is testable without driving a model.
38. As a developer, I want files kept on disk so an agent can re-read them, so that the Extraction is not the only surviving record of what a Document said.
39. As a developer, I want files named by Document identifier rather than by the name the browser sent, so that a Claimant cannot choose a path on the server.
40. As a developer, I want files removed on startup, so that bytes never outlive the in-memory records that point at them.
41. As a workshop presenter, I want the agent's tool calls visible on screen, so that an audience sees it doing something rather than appearing to be a chatbot with a large prompt.
42. As a workshop presenter, I want a common question to provoke a real tool call, so that the demo shows the mechanism rather than prompt stuffing.
43. As a workshop presenter, I want a confirmed Document Request to appear live on the Claimant screen, so that an audience sees one agent's suggestion cross between two screens.

## Implementation Decisions

### Scope and shape

- The Claim Chat is pinned to one Claim. There is no cross-Claim reach and no desk-level assistant.
- Calls are blocking, consistent with every other model call in the application. Streaming is not
  part of this work.
- There is no authentication, so a Claim Chat belongs to the Claim, not to a person. Two Claim Handlers
  looking at the same Claim share one conversation. This is the intended model, not a limitation.

### The seam

- `ClaimDesk` remains the single seam. It grows `chat`, `proposeReview`, `proposeDocumentRequest`,
  `confirm` and `decline` alongside `list`, `open` and `review`.
- The rules about what a Proposal is and what confirming one means live on the Proposal types, the
  way the rules about Claim Status live on `Claim`. `ClaimDesk` continues to fetch, delegate and hand
  back.
- The tools class holds no logic. Every tool method delegates straight to `ClaimDesk`. This is a hard
  constraint: logic inside a tool is reachable only by driving a model, and therefore untestable.

### Agents

Two new agents, each an interface, a system message and a return type, each one line in the AI
service configuration — the existing pattern.

- **The Claim Chat agent.** Takes the Claim Handler's question plus the Claim snapshot as template
  variables, keyed by a memory identifier. Returns `Result<String>` rather than `String`, because
  `Result` is what carries `toolExecutions()`, and the tool calls are shown to the Claim Handler.
- **The document reader.** One-shot, given a file's contents and a question, returning free text. It
  is given no Claim context at all — a reader that knows nothing about the Claim cannot be led by it.

Per ADR 0002 both agents state the English rule in their own system message rather than inheriting
it from anywhere.

### What the agent starts with, and what it must fetch

The dividing line: **the agent sees the Claim at a glance and looks closer on demand.**

In the system message:

- Claim reference, Claim Status, Required Documents, and which of them are outstanding.
- The Claim Summary, read from its existing cache. Chat reuses the cached summary and writes it only
  if absent, so a Claim Handler who has already opened the Claim pays nothing extra.
- A one-line index per attached Document: filename, Classification, which Required Document it
  counts as, and the bare Quality Assessment verdict.
- Outstanding and declined Proposals, so the agent does not repeat itself.

Reached only by a tool: a Document's own summary, its full Extraction, the Quality Assessment's
reasoning and issues, and the contents of the original file.

### Tools

Four. Each receives the Claim identifier through the memory-identifier binding rather than as a model
supplied argument, so the model cannot address another Claim.

| tool | behaviour |
|---|---|
| document detail | one Document's summary, full Extraction, Quality Assessment reason and issues |
| read document | loads the file from disk, calls the reader agent with a question, returns its text |
| propose review | records a Proposal. Writes nothing |
| propose document request | records a Proposal. Writes nothing |

- Tools address Documents by **filename**, not by identifier. Identifiers are unspeakable and the
  index the agent holds is by filename. Filenames resolve within the Claim; where two Documents share
  one, the most recently uploaded wins — the rule `Claim` already uses to pick the counting Document.
- `returnBehavior` is left at its default. A Proposal must not halt the tool-calling loop: the agent
  has to be able to propose two things, or explain itself after proposing one.
- Multimodal tool results are deliberately not used. LangChain4j supports returning image content
  from a tool, but the documented provider list covers neither Vertex AI Gemini nor the
  OpenAI-compatible Foundry path, and PDF content is not a supported tool return type at all. The
  reader agent exists because of this, and it is a better shape regardless.

### Chat memory

- Keyed by Claim identifier, which doubles as the tools' Claim binding.
- In-memory store, message-window memory of roughly twenty messages.
- Lost on restart, like every other store in the application.

### Proposals

- A sealed type with two permitted forms: a review Proposal naming a Document, and a document
  request Proposal naming a plain-text label. Confirmation switches over them by pattern, so a third
  form will not compile until it is handled.
- State is `PROPOSED`, `CONFIRMED` or `DECLINED`. Proposals persist on the Claim rather than expiring
  with the conversation, and both outstanding and declined ones are fed back into the system message.
- Held in an in-memory store keyed by Claim, the same shape as the existing stores.
- Confirming a review Proposal calls the existing Review path, so there is exactly one behaviour.
- Confirming a document request Proposal records a **Document Request** — a distinct thing from the
  Proposal that produced it. A Proposal is what the agent suggested; a Document Request is what
  exists in the world and what a Claimant sees.
- A Document Request does **not** append to a Claim's Required Documents. Per ADR 0001 that list is
  what Claim Status is derived from, and letting an agent extend it would let a model move a Claim
  backwards out of `READY_FOR_DECISION`.
- Neither confirming nor declining a Proposal invalidates the cached Claim Summary, for the same
  reason a Review does not: no Document's contents changed.

### Files on disk

- Document intake writes the uploaded bytes to a configured directory, defaulting to a temporary
  directory, at the point it already reads them for the intake agent.
- Files are named by Document identifier. The uploaded filename is never used as a path — intake
  already documents it as untrusted.
- The directory is cleared on startup. Files outliving the in-memory records that point at them is
  worse than losing both together.
- The Document record gains **no** new component. The path is derivable from the identifier and the
  directory, and the record already carries the content type needed to choose between PDF and image
  content.
- The Claimant's upload screen keeps rendering previews from the file the browser already holds.
  Nothing about the existing preview path changes.
- This partially reopens a storage decision previously recorded as deferred, and gets ADR 0003.

### API

- One endpoint to send a chat turn for a Claim, returning the answer, the tool calls made, and any
  Proposals raised.
- Endpoints to confirm and to decline a Proposal.
- Document Requests are added to the existing Claim overview rather than given an endpoint of their
  own — the Claimant screen already fetches the Claim list, so this costs no new call.
- Failures surface the real cause, as the existing controllers do.

### Frontend

- A sticky chat column on the Claim Handler's Claim screen. The Claim list is untouched.
- Empty on open, with three suggested questions as clickable chips. No greeting, so opening a Claim
  still costs exactly two model calls.
- A compact strip under each answer listing the tool calls made.
- Proposal cards with Confirm and Decline.
- Document Requests shown on the Claimant screen beside the existing Required Documents checklist.

### Unchanged

The intake agent, the Claim Summary agent and the situation-report agent all keep running exactly as
they do now. The Claim screen must be useful before anyone types a word, and the cached Claim Summary
is what grounds the chat.

## Testing Decisions

A good test here asserts external behaviour: what an agent was handed, and whether a Claim actually
moved. It does not assert prompt wording. ADR 0002 already states the reasoning — asserting that a
system message contains a particular word only restates the code.

**At the `ClaimDesk` seam**, with both new agents mocked exactly as the Claim Summary and
situation-report agents already are, and argument captors used to inspect what they were handed:

- Proposing a Review and confirming it moves the Claim; declining it leaves the Claim where it was.
- A Proposal on its own, unconfirmed, changes nothing.
- A confirmed document request Proposal produces a Document Request visible on the Claim overview,
  and does not alter the Claim's Required Documents or its Claim Status.
- Declined and outstanding Proposals reach the chat agent.
- The chat reuses a cached Claim Summary rather than rewriting it.
- The reader agent is called with a file only when the read tool is exercised.

Prior art: the existing Claim desk test, which mocks both handler-side agents, captures what each was
handed, and reads Claim Status back through the Claim list.

**At the document index projection**, as its own test: the rendered text is pinned, including what is
absent from it — the full Extraction values and the Quality Assessment's reasoning must not appear,
because those are what the detail tool exists to fetch.

Prior art: the existing Claim Summary projection test, whose javadoc gives the rule this follows — a
change to a projection's rendering is a change to a prompt, and it should fail under a name that says
so.

**Not tested:** system message contents, the tools class (it holds no logic by construction), and
anything requiring a live model call.

## Out of Scope

- **Streaming.** Every model call in this application blocks, and this one does too. It is the change
  most likely to be wanted next.
- **Prompt injection.** A Document is Claimant-supplied text that reaches a model, and the
  reader agent relays it. The mitigation here is structural — the agent cannot write, only propose,
  and a Claim Handler confirms everything — and that is the whole of what this spec covers. Injection
  handling of it is worked separately.
- **Guardrails.** No input or output guardrail is added.
- **Cross-Claim reach.** No tool spans Claims, and there is no desk-level assistant.
- **Recording a decision.** Deciding a Claim stays entirely a Claim Handler's act, with no agent
  involvement of any kind.
- **Delivering a Document Request.** It appears on the Claimant's screen. There is no email,
  notification or contact detail anywhere in the system.
- **Persistence.** Everything except the files themselves stays in memory and is lost on restart.
- **Authentication.** The two roles remain a vocabulary distinction, not a permission model.
- **Naming the four Document Types.** Still deferred, per ADR 0001.
- **The workshop subtraction.** Which parts are removed for an audience to rebuild is decided
  separately, after this lands.

## Further Notes

- **ADR 0003** to be written: files kept on disk. It is hard to reverse, surprising without context,
  and it partly reopens a decision explicitly recorded as deferred.
- **Three glossary terms** to add: Claim Chat, Proposal, Document Request. No term for "agent" — the
  glossary names what agents produce, never the producer, and adding one would put the first
  implementation noun into a file deliberately free of them.
- **The provider finding is worth keeping.** Tools returning image content are documented as working
  on Anthropic, Amazon Bedrock and Google AI Gemini — not on Vertex AI Gemini's module, and not on
  the OpenAI-compatible path. Anyone who later tries to simplify the reader agent away will hit this,
  and the reason should be findable.
- **The demo beat.** A confirmed Document Request crossing to the Claimant screen is the strongest
  moment available: two screens, one agent, and a visible consequence. It is worth building the
  Claimant-side rendering properly rather than treating it as an afterthought.
