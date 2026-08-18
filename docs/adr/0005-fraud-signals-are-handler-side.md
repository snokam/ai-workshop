# Fraud signals go to the Case Handler, and never to the Claimant

A Fraud Screening runs at intake, over the bytes of the upload and what the intake agent made of it.
What it finds is shown to a Case Handler and to nobody else: it never appears on the upload screen,
it never reaches the Claimant's endpoints, and it never changes a Case Status.

Three consequences follow, and each is load-bearing rather than incidental:

- A Screening is **not a component of `UploadedDocument`**. It lives inside `FraudScreener`, keyed by
  Document id, and reaches the API only through `CaseDetail`.
- The Claimant's endpoints answer with **`DocumentForClaimant`**, a projection that leaves out the
  agent's `ManipulationAttempt` — the one part of the analysis written for somebody else.
- `Case.status` is **untouched**. A Case is held at `NEEDS_REVIEW` by a Quality Assessment a Review
  can clear, and by nothing in `task_2_postprocessing`.

## What prompted it

The obvious implementation is a `screening` component on `UploadedDocument`, next to `analysis`. It
is one field, the plumbing is free, and every endpoint that already returns a Document returns the
Screening with it.

Which is the problem. `POST /api/documents` and `GET /api/documents` are the upload screen — the
Claimant's side — so that one field publishes, to the person who uploaded the file, a list of which
tricks were noticed. Keeping the two apart would then be a matter of remembering to, on every
endpoint, in every future change, forever.

There is a worse version of the same mistake. The intake agent is asked to report a Document that
tried to give it orders. Return that to the uploader and the application answers "I saw the text you
hid at the foot of page two" — which is not a deterrent. It is a free lesson in what to try next, and
the people who most want that lesson are exactly the people it would be sent to.

## Considered options

**Show everything to everyone.** Honest, and it lets a Claimant correct a genuine mix-up: the wrong
file, a receipt that really was uploaded to two Cases because two Cases really do share an expense.
Rejected on the balance of who is affected. Almost every Claimant is honest, and an honest one gains
nothing from a paragraph about manipulation they did not attempt — while the small number who did
attempt it gain a great deal from being told which check caught them.

**Let a strong Indicator hold the Case at `NEEDS_REVIEW`.** Mirrors how a POOR Quality Assessment
behaves, and it guarantees a handler looks. Rejected: the two are not alike. A POOR verdict says the
Document cannot be worked with, which is a fact about the artefact and one the Claimant can fix by
sending a better copy. An Indicator says something about the *person* — and every check here has an
innocent explanation. EXIF is stripped by every messaging app; a screenshot never had camera
metadata; the same file legitimately belongs to two Cases when two Cases share an expense. A heuristic that
stops someone's insurance claim while they are given no way to see it, let alone answer it, is not a
check. It is a penalty with no appeal, and this is the wrong application to build one in.

**Hide it with `@JsonIgnore` on the record component.** One annotation instead of a projection.
Rejected on the mechanics: LangChain4j derives the model's output schema from the same record and
parses the reply back into it, so a Jackson annotation there is a bet on which of two frameworks
reads it. A projection is a few more lines and no bet at all.

## Consequences

A Screening is made once, at intake, and not recomputed. That was originally forced: the bytes were
not kept. [ADR 0004](./0004-uploaded-files-are-kept-on-disk.md) has since changed that, so it is now a
choice rather than a constraint — a check added later *could* be run over everything already
uploaded. It still is not, for a reason worth stating: a Screening a handler has already read should
not quietly change underneath them, and a check that reaches a different verdict on a Document
tomorrow than it did today is worse than one that never ran. Re-screening, if it is ever wanted,
should be something someone asks for and can see the result of.

`CaseDetail` grows a component, and the handler screen is now the only screen that can render one.

**The wall is in the shape of the API, not in an authorisation boundary.** This application has no
login: `GET /api/cases/{id}` is open to anyone who can reach the port, and the Claimant/Case Handler
distinction is a vocabulary in the domain rather than a permission model. What this decision buys is
that the two audiences cannot be conflated *by accident* — no endpoint serves a Claimant something
written for a handler, and no future change makes that happen by adding a field in an obvious place.
It does not keep out anyone who wants in. The day this application has users, it needs authorisation,
and this ADR is not it.
