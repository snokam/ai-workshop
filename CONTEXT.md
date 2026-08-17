# Document Handling

Case handling with AI agents in the loop. Someone uploads documents to a case; agents read them,
say what they are, and judge whether the file is good enough to work with. Insurance is the obvious
setting, but nothing here is specific to it.

## Output language

Agents write their prose in **English**, whatever language the Documents are in. Extraction is the
exception: field names and values are quoted off the Document and stay untranslated. Each agent
states this in its own system message — see
[ADR 0002](./docs/adr/0002-agents-write-in-english.md) for why, and for why the Case Summary agent is
handed a projection rather than the Documents.

## Language

**Document**:
One file uploaded to a case. The file plus everything the agents have worked out about it.
_Avoid_: file, attachment, upload

**Document Type**:
The kind of document a Document is. Four types exist in the domain; they have not been named yet.
_Avoid_: category (used only as the name of the free-text field standing in until the four are fixed)

**Classification**:
The agent's decision about which Document Type a Document is.
_Avoid_: categorisation, tagging

**Extraction**:
The facts lifted out of a Document — the values a human would otherwise read off it and type in.
_Avoid_: metadata, parsing, OCR

**Quality Assessment**:
The agent's verdict on a Document as an artefact: legible, complete, and the document it claims to
be. Says nothing about whether the contents are correct or acceptable. Never a gate on upload — an
upload is always accepted — but a poor verdict holds a Case at `NEEDS_REVIEW` until a Review clears it.
_Avoid_: validation, verification, approval

**Case**:
The unit of work a Document belongs to, and the thing that has a status. Carries its own list of
Required Documents; Cases do not come in kinds.
_Avoid_: claim, application, file

**Case Handler**:
The internal person who reads across a Case and decides it. The audience for everything the agents
produce on the handler side.
_Avoid_: advisor, caseworker

**Claimant**:
The person whose Case it is. They upload Documents to it and read the intake agent's advice; they
never handle a Case. "Claim" stays under `_Avoid_` for the work item itself — the person is the
Claimant, the thing is the Case.
_Avoid_: user, customer, applicant

**Required Document**:
One of the documents a Case needs before it can be decided, written as a plain label — the four
Document Types are not named, so a Required Document is described rather than typed.
_Avoid_: mandatory document, checklist item

**Case Status**:
Where a Case stands, derived from which Required Documents are matched and whether any matched
Document is waiting on a Review: `AWAITING_DOCUMENTS`, `NEEDS_REVIEW`, `READY_FOR_DECISION`.
_Avoid_: state, stage, phase

**Review**:
A Case Handler's confirmation that a Document is good enough to work with despite its Quality
Assessment. A Claimant sending a better Document clears the same block.
_Avoid_: approval, sign-off

**Fraud Screening**:
The checks that run over an uploaded file at intake, asking whether it is what it appears to be. Runs
over the bytes and over what the intake agent already noticed — never over whether the Case deserves
to be paid. Read by a Case Handler and by nobody else; never a gate on anything (see
[ADR 0003](./docs/adr/0003-fraud-signals-are-handler-side.md)).
_Avoid_: fraud detection, risk scoring, verification

**Fraud Indicator**:
One thing a check noticed. An observation a Case Handler can go and check for themselves — "this
image is published on four pages" — and never a conclusion about the person who sent it. Carries a
Weight (`NOTE`, `CONCERN`, `STRONG`) saying how much attention it deserves before a handler has seen
the rest.
_Avoid_: red flag, fraud score, alert

**Manipulation Attempt**:
Text inside a Document addressed to the agent reading it rather than to a person. A component of what
the intake agent returns, and the only part of it a Claimant never sees — it reaches the Case
Handler as a Fraud Indicator instead.
_Avoid_: prompt injection (accurate, but it names the mechanism rather than the thing a handler reads)

**Case Summary**:
The agent's account of what is in a Case's Documents, taken across all of them — what a Case Handler
reads instead of opening each Document in turn. A Document has its own separate summary.
_Avoid_: overview, digest

## Guardrails

Two, both on the intake agent, because it is the only agent an outsider can put anything in front of.
Both are LangChain4j guardrails, so they run inside the call rather than around it — see
[the walkthrough](./docs/guardrails-walkthrough.md).

- **Input guardrail** (`UploadedFileGuardrail`) — one file and one sentence of ours reach the model.
  Nothing a Claimant typed, the filename above all, becomes part of a prompt. It cannot see inside
  the file and is not a defence against what is printed on the page.
- **Output guardrail** (`AnalysisGuardrail`) — a match must name a Required Document this Case
  actually asked for. A label that is not on the list is struck out, whether the agent paraphrased
  it, invented it, or was talked into it by the Document.

## Not settled yet

Terms the domain clearly has, which are deliberately still open.

- **The four Document Types** — not named. The intake agent returns free text until they are, and a
  Case's Required Documents are plain labels rather than types. See
  [ADR 0001](./docs/adr/0001-free-text-required-documents.md).
