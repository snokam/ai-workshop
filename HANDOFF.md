# Handoff — two open domain questions

Both were raised on 2026-08-15 and left open on purpose. Neither blocks the current demo: the intake
agent works end to end without either being answered. Both block the case-handler side of the
workshop, so they want settling before that gets built.

Vocabulary in [CONTEXT.md](./CONTEXT.md).

---

## 1. "Advisor" or "case handler"?

### What was said

The workshop notes contain one line about the internal user:

> As an advisor, I want to know what is in the documents and what the status of a case is

and the same person was later described as "case handler side, an agent can assist in finding and
summarizing document content".

### Why it is still open

Two words, one apparent role, and nobody has picked. The word is not cosmetic — it lands in class
names, API paths, UI labels and the glossary at the same time, and changing it later is a rename
across all four.

### The question underneath the question

These may not be two names for one role. They are different jobs in most case-handling organisations:

- an **advisor** advises the *client* — helps them get their case in order, talks to them
- a **case handler** *decides* the case — reads the evidence and rules on it

If both exist here, picking one word doesn't resolve anything; it hides a second role that the
document-search agent may need to serve differently. An advisor wants "what does my client still
need to send?". A case handler wants "is there enough here to decide?". Same documents, different
question.

### How to settle it

Three scenarios. If any answer is "different people", there are two roles:

1. The person who reads the uploaded documents — do they also decide the case outcome?
2. Does this person ever speak to the client, or only read their file?
3. Is there someone who helps a client assemble their documents but has no say in the outcome?

### Worth checking first

The notes read like a translation. If the original is Norwegian, *saksbehandler* is unambiguously
**case handler**, and *rådgiver* is **advisor** — two established, distinct job titles. Checking
which word the source used probably answers this outright.

### Recommendation

**Case Handler**, unless scenario 3 is a real person in the target organisation. It names the job
(handling a case) rather than a relationship to the client, and it survives the client-facing side
being added later. Then add `_Avoid_: advisor, caseworker` to CONTEXT.md.

### Blocks

The case-handler-side agent, and any API path or screen that names the role.

---

## 2. What is Case Status?

### What was said

The same notes line: *"…and what the status of a case is"*. That is all there is.

### Why it is still open

"Status" carries two different meanings and the line supports both:

- a **workflow state** — `NEW`, `AWAITING DOCUMENTS`, `READY FOR DECISION`, `DECIDED`
- a **situation report** — a sentence telling a human where things stand

They imply different implementations and different failure modes. A workflow state that an LLM
invents is a bug. A situation report that Java assembles from enum values is unreadable.

### The hard dependency

**Case Status cannot be derived until the four Document Types are named.** A derived status is a
checklist — *which required documents are present, which are flagged* — and there is no checklist
without knowing what is required. This question is downstream of that one; answering it first means
guessing.

A second, unasked question comes with it: **does a Case have a type?** A claim and a policy
application need different document sets. If every Case requires the same four documents, the
checklist is a constant. If not, required-documents-per-case-type is a real piece of domain.

### Scenarios that force precision

Worth putting to whoever owns the domain — each has a defensible answer both ways:

1. All four required documents are uploaded, but one is `POOR` quality. Ready for decision, or not?
2. The same document is uploaded twice, the second one better. Two documents, or one replaced?
3. A document arrives that fits none of the four types. Does it affect status at all?
4. A required document is present but the agent classified it with low confidence. Present or missing?

Scenario 1 is the sharp one: it decides whether Quality Assessment stays pure advice (as it is on the
upload side today) or acquires teeth on the handler side. Those can legitimately differ — advice to
the client, signal to the handler — but that should be a decision, not an accident.

### Options

| | Approach | Cost | Risk |
|---|---|---|---|
| a | Derived only — checklist in Java | Low | Unimpressive at an AI workshop |
| b | LLM judgment only | Low | Invents states; not reproducible on stage |
| c | Derived facts, LLM writes the sentence over them | Medium | None material |

### Recommendation

**(c).** The status itself stays deterministic and testable; the agent turns it into the sentence a
human wants to read. It is also the better teaching point — the honest boundary between what belongs
in code and what belongs in a model, demonstrated rather than asserted.

### Blocks

The case-handler dashboard, and the "what is the status of this case" agent query.

---

## Order to take these in

1. Name the four Document Types — everything else is guesswork until this is done
2. Case Status (needs 1)
3. The role name (independent; settle it before any handler-side code is written)
