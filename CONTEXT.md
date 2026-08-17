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
Required Documents. A Case is opened from a Case Type, but afterwards it is just its list — the type
is not stored on it.
_Avoid_: claim, application, file

**Case Type**:
The kind of Case a Claimant opens, chosen by an agent from what they typed. A fixed, hardcoded set
grounded in Storebrand products (travel, home contents, disability, health treatment, motor), each
carrying the Required Documents that kind of Case needs. `OTHER` is the fallback when nothing fits.
The type decides the checklist at creation and is kept on the Case: it frames how the handler-side
agents read across it, so a travel claim is summarised as a travel claim. See
[ADR 0003](./docs/adr/0003-hardcoded-case-types.md).
_Avoid_: category, claim kind

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

**Case Summary**:
The agent's account of what is in a Case's Documents, taken across all of them — what a Case Handler
reads instead of opening each Document in turn. A Document has its own separate summary.
_Avoid_: overview, digest

**Case Chat**:
The conversation a Case Handler can have about one Case, beside the Case's contents. Belongs to the
Case rather than to a person — there is no authentication, so two Case Handlers with the same Case
open share one.
_Avoid_: assistant, copilot, chatbot

**Proposal**:
Something suggested in a Case Chat and not done. It performs nothing; a Case Handler confirms or
declines it, and only a confirmation makes it real. Two kinds exist: a Review, and a Document
Request. A declined Proposal is kept, not deleted.
_Avoid_: suggestion, recommendation, action

**Document Request**:
Something a Case Handler has asked the Claimant for, in plain language, shown on the Claimant's
upload screen. Not a Required Document: Case Status is derived from that list, so a Document Request
sits beside it and moves nothing.
_Avoid_: chase, reminder, task

## Not settled yet

Terms the domain clearly has, which are deliberately still open.

- **The four Document Types** — not named. The intake agent returns free text until they are, and a
  Case's Required Documents are plain labels rather than types. See
  [ADR 0001](./docs/adr/0001-free-text-required-documents.md).
