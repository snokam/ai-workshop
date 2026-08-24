# Document Handling

Claim handling with AI agents in the loop. Someone uploads documents to a claim; agents read them,
say what they are, and judge whether the file is good enough to work with. Insurance is the obvious
setting, but nothing here is specific to it.

## Output language

Agents write their prose in **English**, whatever language the Documents are in. Extraction is the
exception: field names and values are quoted off the Document and stay untranslated. Each agent
states this in its own system message — see
the decisions at the end of this file for why, and for why the Claim Summary agent is
handed a projection rather than the Documents.

## Language

**Document**:
One file uploaded to a claim. The file plus everything the agents have worked out about it.
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
upload is always accepted — but a poor verdict holds a Claim at `NEEDS_REVIEW` until a Review clears it.
_Avoid_: validation, verification, approval

**Claim**:
The unit of work a Document belongs to, and the thing that has a status. Carries its own list of
Required Documents. A Claim is opened from a Claim Type, but afterwards it is just its list — the type
is not stored on it.
_Avoid_: claim, application, file

**Claim Type**:
The kind of Claim a Claimant opens, chosen by an agent from what they typed. A fixed, hardcoded set
grounded in Storebrand products (travel, home contents, disability, health treatment, motor), each
carrying the Required Documents that kind of Claim needs. `OTHER` is the fallback when nothing fits.
The type decides the checklist at creation and is kept on the Claim: it frames how the handler-side
agents read across it, so a travel claim is summarised as a travel claim. See
the decisions at the end of this file.
_Avoid_: category, claim kind

**Claim Handler**:
The internal person who reads across a Claim and decides it. The audience for everything the agents
produce on the handler side.
_Avoid_: advisor, caseworker

**Claimant**:
The person whose Claim it is. They upload Documents to it and read the intake agent's advice; they
never handle a Claim. "Claim" stays under `_Avoid_` for the work item itself — the person is the
Claimant, the thing is the Claim.
_Avoid_: user, customer, applicant

**Required Document**:
One of the documents a Claim needs before it can be decided, written as a plain label — the four
Document Types are not named, so a Required Document is described rather than typed.
_Avoid_: mandatory document, checklist item

**Claim Status**:
Where a Claim stands, derived from which Required Documents are matched and whether any matched
Document is waiting on a Review: `AWAITING_DOCUMENTS`, `NEEDS_REVIEW`, `READY_FOR_DECISION`.
_Avoid_: state, stage, phase

**Review**:
A Claim Handler's confirmation that a Document is good enough to work with despite its Quality
Assessment. A Claimant sending a better Document clears the same block.
_Avoid_: approval, sign-off

**Manipulation Attempt**:
Text inside a Document addressed to the agent reading it rather than to a person. A component of what
the intake agent returns, and the only part of it a Claimant never sees — it reaches the Claim
_Avoid_: prompt injection (accurate, but it names the mechanism rather than the thing a handler reads)

**Claim Summary**:
The agent's account of what is in a Claim's Documents, taken across all of them — what a Claim Handler
reads instead of opening each Document in turn. A Document has its own separate summary.
_Avoid_: overview, digest

**Claim Chat**:
The conversation a Claim Handler can have about one Claim, beside the Claim's contents. Belongs to the
Claim rather than to a person — there is no authentication, so two Claim Handlers with the same Claim
open share one.
_Avoid_: assistant, copilot, chatbot

**Proposal**:
Something suggested in a Claim Chat and not done. It performs nothing; a Claim Handler confirms or
declines it, and only a confirmation makes it real. Two kinds exist: a Review, and a Document
Request. A declined Proposal is kept, not deleted.
_Avoid_: suggestion, recommendation, action

**Document Request**:
Something a Claim Handler has asked the Claimant for, in plain language, shown on the Claimant's
upload screen. Not a Required Document: Claim Status is derived from that list, so a Document Request
sits beside it and moves nothing.
_Avoid_: chase, reminder, task

## Guardrails

Two, both on the intake agent, because it is the only agent an outsider can put anything in front of.
Both are LangChain4j input guardrails, so they run inside the call rather than around it, and both
ask a model rather than applying a rule — whether text has a situation in it, and whether text is
addressed to the software, are questions about meaning.

- **Prompt injection** (`tasks/task_2_guardrails/prompt_injection/`) — refuses text that is
  instructing whatever reads it next rather than describing something that happened. Runs first, so
  manipulated text never reaches the second check, which is itself a model. The person is told
  nothing about why: the refusal is a constant, and what was found goes to the log instead.
- **Claim description** (`tasks/task_2_guardrails/claim_description/`) — refuses text nobody could
  open a Claim from: an empty box, a greeting, a few characters of nonsense. Biased towards saying
  yes, because refusing an unusual Claim is worse than opening a Claim somebody closes.

Neither can see inside an uploaded Document. What is printed on the page reaches the agent in task 3
untouched, and the only thing that catches it there is the model noticing and saying so.

## Not settled yet

Terms the domain clearly has, which are deliberately still open.

- **The four Document Types** — not named. The intake agent returns free text until they are, and a
  Claim's Required Documents are plain labels rather than types. See
  the decisions at the end of this file.

## Decisions worth knowing

Four choices that shaped the domain, and would be asked about by anyone reading the code cold.

**Required Documents are free-text labels, matched by an agent.** The domain has document types, but
they are not named yet, and a Claim Status is a checklist over required documents — so waiting for
those names would have blocked the whole handler side. The agent is asked to copy a label back
exactly from the list it was given, which is a weaker contract than an enum and the reason
`matchedRequiredDocument` can come back null.

**Claim Types are a hardcoded enum, with their required documents on them.** The claimant side opens
with a free-text box, so something has to turn "my suitcase never arrived" into a checklist. Five
types, each carrying its own list. A type nobody can classify into is worse than no type, which is
why there is no "other".

**Agents write in English, whatever language the documents are in.** Each agent says so in its own
system message rather than inheriting it from anywhere: `AiServices.create` builds an agent from its
interface alone, so the interface has to be the whole of the definition. Extracted field *names*
stay in the document's own words — those are quoted, not written.

**Uploaded files are kept on disk**, named by document id, in a configured directory emptied on
startup. Everything else here is in memory. The bytes are kept because more than one agent needs the
file itself: intake reads it once, and the advisor chat's `DocumentReader` opens it again later to
answer a specific question.
